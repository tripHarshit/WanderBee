package com.wanderbee.chatservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${chat.kafka.topic}")
    private String chatTopic;

    /**
     * Auto-create the topic on startup if it does not already exist.
     * 3 partitions allows parallel consumption; 1 replica is fine for dev.
     */
    @Bean
    public NewTopic chatMessagesTopic() {
        return TopicBuilder.name(chatTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
