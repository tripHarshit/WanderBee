package com.wanderbee.chatservice.model;

/**
 * Type of a chat message.
 * DELETE is a special event published to Kafka so all connected clients
 * can remove the message from their UI.
 */
public enum MessageType {
    TEXT,
    IMAGE,
    SYSTEM,
    DELETE
}
