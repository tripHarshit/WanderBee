package com.wanderbee.chatservice.service;

import com.wanderbee.chatservice.dto.CreateRoomRequest;
import com.wanderbee.chatservice.model.ChatRoom;
import com.wanderbee.chatservice.repository.ChatMessageRepository;
import com.wanderbee.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    /** All rooms a user participates in */
    public List<ChatRoom> getRoomsForUser(String userId) {
        return chatRoomRepository.findByParticipantIdsContaining(userId);
    }

    /**
     * Create a new private (1-to-1) room between two users.
     * If a room already exists for the pair it is returned instead of
     * creating a duplicate.
     */
    public ChatRoom createPrivateRoom(String userId1, String userId2) {
        return chatRoomRepository
                .findPrivateRoomBetween(userId1, userId2)
                .orElseGet(() -> {
                    ChatRoom room = ChatRoom.builder()
                            .isGroup(false)
                            .participantIds(List.of(userId1, userId2))
                            .build();
                    ChatRoom saved = chatRoomRepository.save(room);
                    log.debug("Created private room [id={}] for {} ↔ {}",
                            saved.getId(), userId1, userId2);
                    return saved;
                });
    }

    /**
     * Create a named group room with the supplied participants.
     */
    public ChatRoom createGroupRoom(CreateRoomRequest request) {
        ChatRoom room = ChatRoom.builder()
                .name(request.getName())
                .isGroup(true)
                .participantIds(request.getParticipantIds())
                .build();
        ChatRoom saved = chatRoomRepository.save(room);
        log.debug("Created group room [id={}, name={}]", saved.getId(), saved.getName());
        return saved;
    }

    /**
     * Delete a room and all its messages.
     * For private rooms this is a hard delete; consider using leaveRoom() for groups.
     */
    @Transactional
    public void deleteRoom(String roomId) {
        chatMessageRepository.deleteByRoomId(roomId);
        chatRoomRepository.deleteById(roomId);
        log.debug("Deleted room [id={}] and its messages", roomId);
    }

    /**
     * Remove a user from a group room's participant list.
     * If the room becomes empty it is cleaned up entirely.
     */
    @Transactional
    public ChatRoom leaveRoom(String roomId, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        room.getParticipantIds().remove(userId);

        if (room.getParticipantIds().isEmpty()) {
            chatMessageRepository.deleteByRoomId(roomId);
            chatRoomRepository.deleteById(roomId);
            log.debug("Room [id={}] is empty – deleted", roomId);
            return null;
        }

        ChatRoom updated = chatRoomRepository.save(room);
        log.debug("User {} left room [id={}]", userId, roomId);
        return updated;
    }
}
