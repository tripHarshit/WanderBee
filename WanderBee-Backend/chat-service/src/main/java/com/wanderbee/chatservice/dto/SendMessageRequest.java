package com.wanderbee.chatservice.dto;

import com.wanderbee.chatservice.model.MessageType;
import lombok.Data;

@Data
public class SendMessageRequest {

    /** ID of the chat room this message belongs to */
    private String roomId;

    private String senderId;

    /**
     * Only required for private (non-group) messages.
     * Null / absent for group messages.
     */
    private String recipientId;

    private String content;

    /** Defaults to TEXT when omitted by the client */
    private MessageType type = MessageType.TEXT;
}
