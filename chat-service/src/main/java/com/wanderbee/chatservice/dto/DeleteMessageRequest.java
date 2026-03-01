package com.wanderbee.chatservice.dto;

import lombok.Data;

@Data
public class DeleteMessageRequest {

    /** ID of the message to delete */
    private String messageId;

    /** Room the message lives in – used for routing the DELETE event */
    private String roomId;

    /** User performing the deletion */
    private String requesterId;
}
