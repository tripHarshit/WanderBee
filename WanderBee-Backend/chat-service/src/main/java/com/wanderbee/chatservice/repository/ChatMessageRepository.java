package com.wanderbee.chatservice.repository;

import com.wanderbee.chatservice.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

    void deleteByRoomId(String roomId);
}
