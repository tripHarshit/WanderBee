package com.example.wanderbee.data.remote

import android.util.Log
import com.example.wanderbee.data.remote.models.chat.ChatMessage
import com.example.wanderbee.data.remote.models.chat.MessageType
import com.example.wanderbee.data.remote.models.chat.SendMessageRequest
import com.example.wanderbee.utils.AppPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lightweight STOMP 1.2 client over OkHttp WebSocket.
 *
 * Connects to the chat-service via the API gateway's `/ws` route
 * (which has no AuthenticationFilter – auth is at the STOMP level).
 *
 * Usage:
 * ```
 * stompClient.connect(userEmail)
 * stompClient.subscribe("/topic/room/$roomId") { message -> ... }
 * stompClient.send("/app/chat.send", sendMessageRequest)
 * stompClient.unsubscribe(subscriptionId)
 * stompClient.disconnect()
 * ```
 */
class StompClient(private val appPreferences: AppPreferences) {

    companion object {
        private const val TAG = "StompClient"
        private const val WS_URL = "ws://10.0.2.2:8082/ws"
        private const val NULL_CHAR = "\u0000"
        private const val HEART_BEAT_INTERVAL = 10000L // 10 seconds
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson: Gson = GsonBuilder().create()

    private var webSocket: WebSocket? = null
    private var userId: String? = null
    private var jwtToken: String? = null
    private val subIdCounter = AtomicInteger(0)

    // subscriptionId -> destination
    private val activeSubscriptions = ConcurrentHashMap<String, String>()
    // subscriptionId -> callback
    private val subscriptionCallbacks = ConcurrentHashMap<String, (ChatMessage) -> Unit>()
    // destination -> subscriptionId (for unsubscribing by destination)
    private val destinationToSubId = ConcurrentHashMap<String, String>()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Flow for incoming messages (alternative to callbacks)
    private val _incomingMessages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<ChatMessage> = _incomingMessages

    private var reconnectAttempt = 0
    private val maxReconnectAttempts = 5

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Open a WebSocket and send a STOMP CONNECT frame.
     * @param userEmail The user's email, used as the STOMP `userId` header.
     */
    fun connect(userEmail: String) {
        if (_connectionState.value == ConnectionState.CONNECTED ||
            _connectionState.value == ConnectionState.CONNECTING) {
            Log.d(TAG, "Already connected or connecting")
            return
        }

        userId = userEmail
        jwtToken = runBlocking { appPreferences.getJwtTokenOnce() }
        _connectionState.value = ConnectionState.CONNECTING

        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)   // no timeout for WebSocket
            .pingInterval(HEART_BEAT_INTERVAL, TimeUnit.MILLISECONDS)
            .build()

        // Include Authorization in the HTTP Upgrade request so Spring's
        // WebSocketAuthInterceptorConfig can validate the handshake.
        val requestBuilder = Request.Builder().url(WS_URL)
        jwtToken?.let { requestBuilder.header("Authorization", "Bearer $it") }
        val request = requestBuilder.build()

        webSocket = client.newWebSocket(request, StompWebSocketListener())
    }

    /**
     * Subscribe to a STOMP destination and receive [ChatMessage]s via callback.
     * @return The subscription ID (use to unsubscribe later).
     */
    fun subscribe(destination: String, onMessage: (ChatMessage) -> Unit): String {
        val subId = "sub-${subIdCounter.getAndIncrement()}"
        activeSubscriptions[subId] = destination
        subscriptionCallbacks[subId] = onMessage
        destinationToSubId[destination] = subId

        if (_connectionState.value == ConnectionState.CONNECTED) {
            sendSubscribeFrame(subId, destination)
        }
        // If not yet connected, subscriptions will be sent after CONNECTED frame.
        return subId
    }

    /** Unsubscribe from a destination by subscription ID. */
    fun unsubscribe(subscriptionId: String) {
        val destination = activeSubscriptions.remove(subscriptionId)
        subscriptionCallbacks.remove(subscriptionId)
        if (destination != null) destinationToSubId.remove(destination)

        if (_connectionState.value == ConnectionState.CONNECTED) {
            sendUnsubscribeFrame(subscriptionId)
        }
    }

    /** Unsubscribe from a destination by destination path. */
    fun unsubscribeByDestination(destination: String) {
        val subId = destinationToSubId[destination] ?: return
        unsubscribe(subId)
    }

    /** Send a STOMP message (e.g. to /app/chat.send). */
    fun send(destination: String, body: Any) {
        val json = gson.toJson(body)
        val frame = buildFrame("SEND", mapOf(
            "destination" to destination,
            "content-type" to "application/json"
        ), json)
        webSocket?.send(frame)
    }

    /** Gracefully disconnect. */
    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        activeSubscriptions.clear()
        subscriptionCallbacks.clear()
        destinationToSubId.clear()
        jwtToken = null
        webSocket?.send(buildFrame("DISCONNECT", emptyMap()))
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }

    // ── STOMP Frame Builders ──────────────────────────────────────────────

    private fun buildFrame(
        command: String,
        headers: Map<String, String>,
        body: String = ""
    ): String {
        val sb = StringBuilder()
        sb.append(command).append("\n")
        headers.forEach { (k, v) -> sb.append("$k:$v\n") }
        sb.append("\n")
        sb.append(body)
        sb.append(NULL_CHAR)
        return sb.toString()
    }

    private fun sendConnectFrame() {
        val headers = mutableMapOf(
            "accept-version" to "1.2,1.1",
            "heart-beat" to "10000,10000"
        )
        // Spring's WebSocketAuthInterceptorConfig validates the JWT from the
        // Authorization header in the STOMP CONNECT frame as a fallback when
        // the HTTP Upgrade request headers are not accessible at the STOMP layer.
        jwtToken?.let { headers["Authorization"] = "Bearer $it" }
        // userId (email) lets the server map the STOMP session to the principal.
        userId?.let { headers["userId"] = it }
        val frame = buildFrame("CONNECT", headers)
        Log.d(TAG, "Sending CONNECT frame: userId=$userId, hasToken=${jwtToken != null}")
        webSocket?.send(frame)
    }

    private fun sendSubscribeFrame(subId: String, destination: String) {
        val frame = buildFrame("SUBSCRIBE", mapOf(
            "id" to subId,
            "destination" to destination
        ))
        Log.d(TAG, "Subscribing: $subId -> $destination")
        webSocket?.send(frame)
    }

    private fun sendUnsubscribeFrame(subId: String) {
        val frame = buildFrame("UNSUBSCRIBE", mapOf("id" to subId))
        webSocket?.send(frame)
    }

    // ── Re-subscribe after reconnect ──────────────────────────────────────

    private fun resubscribeAll() {
        activeSubscriptions.forEach { (subId, destination) ->
            sendSubscribeFrame(subId, destination)
        }
    }

    // ── Reconnect Logic ───────────────────────────────────────────────────

    private fun attemptReconnect() {
        if (reconnectAttempt >= maxReconnectAttempts) {
            Log.e(TAG, "Max reconnect attempts ($maxReconnectAttempts) reached – giving up.")
            _connectionState.value = ConnectionState.ERROR
            return
        }
        reconnectAttempt++
        // Fixed 5-second interval so the backend's WebSocket session can recover predictably.
        val delayMs = 5_000L
        Log.d(TAG, "Reconnecting in ${delayMs}ms (attempt $reconnectAttempt / $maxReconnectAttempts)")

        scope.launch {
            delay(delayMs)
            userId?.let { connect(it) }
        }
    }

    // ── STOMP Frame Parser ────────────────────────────────────────────────

    private data class StompFrame(
        val command: String,
        val headers: Map<String, String>,
        val body: String
    )

    private fun parseFrame(raw: String): StompFrame? {
        // STOMP frames end with NULL char
        val text = raw.trimEnd('\u0000')
        val lines = text.split("\n")
        if (lines.isEmpty()) return null

        val command = lines[0].trim()
        val headers = mutableMapOf<String, String>()
        var bodyStartIndex = 1

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isEmpty()) {
                bodyStartIndex = i + 1
                break
            }
            val colonIdx = line.indexOf(':')
            if (colonIdx > 0) {
                headers[line.substring(0, colonIdx)] = line.substring(colonIdx + 1)
            }
        }

        val body = if (bodyStartIndex < lines.size) {
            lines.subList(bodyStartIndex, lines.size).joinToString("\n")
        } else ""

        return StompFrame(command, headers, body)
    }

    // ── WebSocket Listener ────────────────────────────────────────────────

    private inner class StompWebSocketListener : WebSocketListener() {

        override fun onOpen(ws: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened")
            sendConnectFrame()
        }

        override fun onMessage(ws: WebSocket, text: String) {
            // Handle heart-beat (single newline)
            if (text.trim().isEmpty() || text == "\n") return

            val frame = parseFrame(text)
            if (frame == null) {
                Log.w(TAG, "Failed to parse STOMP frame: $text")
                return
            }

            when (frame.command) {
                "CONNECTED" -> {
                    Log.d(TAG, "STOMP CONNECTED")
                    _connectionState.value = ConnectionState.CONNECTED
                    reconnectAttempt = 0
                    resubscribeAll()
                }
                "MESSAGE" -> {
                    val subId = frame.headers["subscription"]
                    Log.d(TAG, "MESSAGE received for subscription=$subId")
                    try {
                        val message = gson.fromJson(frame.body, ChatMessage::class.java)
                        // Deliver via callback
                        subId?.let { subscriptionCallbacks[it]?.invoke(message) }
                        // Also deliver via SharedFlow
                        scope.launch { _incomingMessages.emit(message) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse ChatMessage: ${frame.body}", e)
                    }
                }
                "ERROR" -> {
                    Log.e(TAG, "STOMP ERROR: ${frame.body}")
                    _connectionState.value = ConnectionState.ERROR
                }
                "RECEIPT" -> {
                    Log.d(TAG, "STOMP RECEIPT: ${frame.headers["receipt-id"]}")
                }
                else -> {
                    Log.d(TAG, "Unhandled STOMP command: ${frame.command}")
                }
            }
        }

        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}", t)
            _connectionState.value = ConnectionState.DISCONNECTED
            attemptReconnect()
        }

        override fun onClosed(ws: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: code=$code reason=$reason")
            if (_connectionState.value != ConnectionState.DISCONNECTED) {
                // Unexpected close → try to reconnect
                _connectionState.value = ConnectionState.DISCONNECTED
                attemptReconnect()
            }
        }
    }
}
