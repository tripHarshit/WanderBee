package com.wanderbee.chatservice.service;

import com.wanderbee.chatservice.dto.DeleteMessageRequest;
import com.wanderbee.chatservice.model.ChatMessage;
import com.wanderbee.chatservice.model.ChatRoom;
import com.wanderbee.chatservice.model.MessageType;
import com.wanderbee.chatservice.repository.ChatMessageRepository;
import com.wanderbee.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatKafkaProducerService kafkaProducerService;

    /**
     * Persist a new message, update the room's lastMessage snapshot,
     * then publish it to Kafka for real-time delivery.
     */
    public ChatMessage sendMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(message);

        // Keep the room's lastMessage up to date
        chatRoomRepository.findById(saved.getRoomId()).ifPresent(room -> {
            room.setLastMessage(saved);
            chatRoomRepository.save(room);
        });

        kafkaProducerService.publish(saved);
        return saved;
    }

    /**
     * Fetch full message history for a room (ordered oldest → newest).
     */
    public List<ChatMessage> getMessagesForRoom(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }

    /**
     * Soft-delete: remove the document from MongoDB then publish a DELETE
     * event to Kafka so all connected WebSocket clients can update their UI.
     */
    public void deleteMessage(DeleteMessageRequest request) {
        ChatMessage original = chatMessageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Message not found: " + request.getMessageId()));

        chatMessageRepository.deleteById(request.getMessageId());

        // Build a lightweight DELETE event that carries just enough info for clients
        ChatMessage deleteEvent = ChatMessage.builder()
                .id(original.getId())
                .roomId(original.getRoomId())
                .senderId(request.getRequesterId())
                .recipientId(original.getRecipientId())
                .type(MessageType.DELETE)
                .timestamp(LocalDateTime.now())
                .content("") // no content for delete events
                .build();

        // Update lastMessage if we deleted the most recent one
        chatRoomRepository.findById(original.getRoomId()).ifPresent(room -> {
            if (room.getLastMessage() != null
                    && original.getId().equals(room.getLastMessage().getId())) {
                List<ChatMessage> remaining =
                        chatMessageRepository.findByRoomIdOrderByTimestampAsc(original.getRoomId());
                room.setLastMessage(remaining.isEmpty()
                        ? null
                        : remaining.get(remaining.size() - 1));
                chatRoomRepository.save(room);
            }
        });

        kafkaProducerService.publish(deleteEvent);
        log.debug("Published DELETE event for message [id={}] in room [id={}]",
                original.getId(), original.getRoomId());
    }
}
