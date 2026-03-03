package com.wanderbee.chatservice.service;

import com.wanderbee.chatservice.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatKafkaProducerService {

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    @Value("${chat.kafka.topic}")
    private String chatTopic;

    /**
     * Publish a message to the chat-messages Kafka topic.
     * The roomId is used as the partition key so all messages for a given
     * room land on the same partition (ordering guarantee per room).
     */
    public void publish(ChatMessage message) {
        CompletableFuture<SendResult<String, ChatMessage>> future =
                kafkaTemplate.send(chatTopic, message.getRoomId(), message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish message [roomId={}] to Kafka: {}",
                        message.getRoomId(), ex.getMessage());
            } else {
                log.debug("Published message [roomId={}, type={}] to partition {}",
                        message.getRoomId(),
                        message.getType(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}
