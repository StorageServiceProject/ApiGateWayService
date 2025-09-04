package com.brodep.apigatewayservice.configuration.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic resourceInfoRequestedTopic() {
        return TopicBuilder.name("events-resource-info-requested").partitions(3).replicas(2).build();
    }

    @Bean
    public NewTopic directoryResourcesInfoRequestedTopic() {
        return TopicBuilder.name("events-directory-resources-info-requested").partitions(2).replicas(1).build();
    }

    @Bean
    public NewTopic resourceDeletedTopic() {
        return TopicBuilder.name("events-resource-deleted").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic resourceUploadedTopic() {
        return TopicBuilder.name("events-resource-uploaded").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic resourceDownloadedTopic() {
        return TopicBuilder.name("events-resource-downloaded").partitions(3).replicas(1).build();
    }

}
