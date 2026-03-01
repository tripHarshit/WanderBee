package com.wanderbee.chatservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateRoomRequest {

    /** Required. For private chats pass null or omit; supply exactly 2 participantIds. */
    private String name;

    private boolean isGroup;

    /** User IDs that will be part of this room */
    private List<String> participantIds;
}
