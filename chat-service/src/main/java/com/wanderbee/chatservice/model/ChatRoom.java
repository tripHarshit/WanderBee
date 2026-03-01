package com.wanderbee.chatservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_rooms")
public class ChatRoom {

    @Id
    private String id;

    private String name;

    private boolean isGroup;

    @Builder.Default
    @Indexed
    private List<String> participantIds = new ArrayList<>();

    /** Snapshot of the most recent message – populated after each send */
    private ChatMessage lastMessage;
}
