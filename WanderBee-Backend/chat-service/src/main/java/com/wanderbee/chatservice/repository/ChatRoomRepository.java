package com.wanderbee.chatservice.repository;

import com.wanderbee.chatservice.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    /** Rooms where a specific user is a participant */
    List<ChatRoom> findByParticipantIdsContaining(String userId);

    /**
     * Locate an existing private (1-to-1) room that contains BOTH participants.
     * Uses a raw MongoDB query with $all to avoid the duplicate-key limitation.
     */
    @Query("{ 'isGroup': false, 'participantIds': { $all: [?0, ?1] } }")
    Optional<ChatRoom> findPrivateRoomBetween(String userId1, String userId2);
}
