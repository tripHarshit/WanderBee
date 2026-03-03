package com.wanderbee.chatservice.controller;

import com.wanderbee.chatservice.dto.DeleteMessageRequest;
import com.wanderbee.chatservice.dto.SendMessageRequest;
import com.wanderbee.chatservice.model.ChatMessage;
import com.wanderbee.chatservice.model.MessageType;
import com.wanderbee.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * WebSocket STOMP controller.
 *
 * Clients send to:
 *   /app/chat.send   → to send a new message
 *   /app/chat.delete  → to delete a message
 *
 * The Kafka consumer (ChatKafkaConsumerService) handles the routing
 * of messages to the correct WebSocket destinations after they are
 * published to the chat-messages topic.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    /**
     * Handle incoming chat messages sent via WebSocket STOMP.
     * The message is saved, published to Kafka, and the consumer
     * broadcasts it to the correct recipients.
     */
    @MessageMapping("chat.send")
    public void sendMessage(@Payload SendMessageRequest request) {
        log.debug("WS chat.send — roomId={}, sender={}", request.getRoomId(), request.getSenderId());

        ChatMessage message = new ChatMessage();
        message.setRoomId(request.getRoomId());
        message.setSenderId(request.getSenderId());
        message.setRecipientId(request.getRecipientId());
        message.setContent(request.getContent());
        message.setType(request.getType() != null ? request.getType() : MessageType.TEXT);

        chatMessageService.sendMessage(message);
    }

    /**
     * Handle message deletion requests sent via WebSocket STOMP.
     */
    @MessageMapping("chat.delete")
    public void deleteMessage(@Payload DeleteMessageRequest request) {
        log.debug("WS chat.delete — messageId={}, roomId={}", request.getMessageId(), request.getRoomId());
        chatMessageService.deleteMessage(request);
    }
}
