package com.wanderbee.chatservice.controller;

import com.wanderbee.chatservice.dto.CreateRoomRequest;
import com.wanderbee.chatservice.dto.DeleteMessageRequest;
import com.wanderbee.chatservice.dto.SendMessageRequest;
import com.wanderbee.chatservice.model.ChatMessage;
import com.wanderbee.chatservice.model.ChatRoom;
import com.wanderbee.chatservice.model.MessageType;
import com.wanderbee.chatservice.service.ChatMessageService;
import com.wanderbee.chatservice.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getRoomsForUser(@RequestParam String userId) {
        return ResponseEntity.ok(chatRoomService.getRoomsForUser(userId));
    }

    @PostMapping("/rooms/private")
    public ResponseEntity<ChatRoom> createPrivateRoom(
            @RequestParam String userId1,
            @RequestParam String userId2) {
        return ResponseEntity.ok(chatRoomService.createPrivateRoom(userId1, userId2));
    }

    @PostMapping("/rooms/group")
    public ResponseEntity<ChatRoom> createGroupRoom(@RequestBody CreateRoomRequest request) {
        request.setGroup(true);
        return ResponseEntity.ok(chatRoomService.createGroupRoom(request));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        chatRoomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ChatRoom> leaveRoom(
            @PathVariable String roomId,
            @RequestParam String userId) {
        ChatRoom updated = chatRoomService.leaveRoom(roomId, userId);
        return updated == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(updated);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(chatMessageService.getMessagesForRoom(roomId));
    }

    // ── REST endpoints for sending/deleting messages (test without WebSocket) ──

    /**
     * POST /api/v1/chat/messages/send
     *
     * Sends a message via REST. Saves to MongoDB, publishes to Kafka,
     * and Kafka consumer broadcasts to WebSocket subscribers.
     */
    @PostMapping("/messages/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody SendMessageRequest request) {
        ChatMessage message = ChatMessage.builder()
                .roomId(request.getRoomId())
                .senderId(request.getSenderId())
                .recipientId(request.getRecipientId())
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : MessageType.TEXT)
                .build();
        return ResponseEntity.ok(chatMessageService.sendMessage(message));
    }

    /**
     * POST /api/v1/chat/messages/delete
     *
     * Deletes a message via REST. Removes from MongoDB and publishes
     * a DELETE event to Kafka.
     */
    @PostMapping("/messages/delete")
    public ResponseEntity<Void> deleteMessage(@RequestBody DeleteMessageRequest request) {
        chatMessageService.deleteMessage(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Debug endpoint: show connected WebSocket users and their subscriptions.
     */
    @GetMapping("/ws/debug")
    public ResponseEntity<String> wsDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Connected users: ").append(userRegistry.getUserCount()).append("\n");
        userRegistry.getUsers().forEach(user -> {
            sb.append("  User: ").append(user.getName()).append("\n");
            user.getSessions().forEach(session -> {
                sb.append("    Session: ").append(session.getId()).append("\n");
                session.getSubscriptions().forEach(sub -> {
                    sb.append("      Sub: ").append(sub.getId())
                      .append(" -> ").append(sub.getDestination()).append("\n");
                });
            });
        });
        return ResponseEntity.ok(sb.toString());
    }

    /**
     * Debug endpoint: send a test message directly via WebSocket (bypasses Kafka).
     * Use this to verify WebSocket delivery works independently.
     */
    @PostMapping("/ws/test-send")
    public ResponseEntity<String> wsTestSend(
            @RequestParam String toUser,
            @RequestParam(defaultValue = "Hello from test!") String msg) {
        log.info(">>> WS TEST: sending to user='{}' msg='{}'", toUser, msg);
        java.util.Map<String, String> payload = java.util.Map.of(
                "content", msg, "senderId", "server-test", "type", "TEXT");
        messagingTemplate.convertAndSendToUser(toUser, "/queue/messages", payload);
        return ResponseEntity.ok("Sent to " + toUser);
    }
}
