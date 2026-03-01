package com.wanderbee.chatservice.service;

import com.wanderbee.chatservice.model.ChatMessage;
import com.wanderbee.chatservice.model.ChatRoom;
import com.wanderbee.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatKafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * Consume every event from the chat-messages topic and broadcast it to
     * the correct WebSocket destination.
     *
     * Routing rules:
     *  - Group chat  → /topic/room/{roomId}         (all subscribers)
     *  - Private chat → /user/{recipientId}/queue/messages  (targeted)
     *
     * DELETE events are delivered to the same destination so clients can
     * remove the message from their local state.
     */
    @KafkaListener(
            topics = "${chat.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ChatMessage message) {
        log.info(">>> Consumed Kafka event [roomId={}, type={}, sender={}]",
                message.getRoomId(), message.getType(), message.getSenderId());

        // Determine room type from the stored room document
        boolean isGroup = chatRoomRepository.findById(message.getRoomId())
                .map(ChatRoom::isGroup)
                .orElse(false);

        log.info(">>> Room isGroup={}", isGroup);

        if (isGroup) {
            // Broadcast to everyone subscribed to this room
            String destination = "/topic/room/" + message.getRoomId();
            messagingTemplate.convertAndSend(destination, message);
            log.info(">>> Sent to group destination: {}", destination);
        } else {
            // Send only to the intended recipient
            if (message.getRecipientId() != null) {
                messagingTemplate.convertAndSendToUser(
                        message.getRecipientId(),
                        "/queue/messages",
                        message
                );
                log.info(">>> Sent private message to user: {}", message.getRecipientId());
            }
            // Also echo back to sender so their own UI updates immediately
            if (message.getSenderId() != null
                    && !message.getSenderId().equals(message.getRecipientId())) {
                messagingTemplate.convertAndSendToUser(
                        message.getSenderId(),
                        "/queue/messages",
                        message
                );
                log.info(">>> Echoed message to sender: {}", message.getSenderId());
            }
        }
    }
}
