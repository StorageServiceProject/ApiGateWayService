package com.brodep.apigatewayservice.service;

import com.brodep.apigatewayservice.dto.event.MinioEvent;
import com.brodep.apigatewayservice.exeption.SendFailedKafkaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, MinioEvent> kafkaTemplate;

    public void sendResourceInfoRequestedEvent(String key, MinioEvent event){
        kafkaTemplate.send("events-resource-info-requested", key, event)
                .thenAccept(kafkaSuccessLogger())
                .exceptionally(kafkaProducingFailedLogger());
    }

    public void sendDirectoryResourcesInfoRequestedEvent(String key, MinioEvent event){
        kafkaTemplate.send("events-directory-resources-info-requested", key, event)
                .thenAccept(kafkaSuccessLogger())
                .exceptionally(kafkaProducingFailedLogger());
    }

    public void sendResourceDeletedEvent(String key, MinioEvent event){
        kafkaTemplate.send("events-resource-deleted", key, event)
                .thenAccept(kafkaSuccessLogger())
                .exceptionally(kafkaProducingFailedLogger());
    }

    public void sendResourceUploadedEvent(String key, MinioEvent event){
        kafkaTemplate.send("events-resource-uploaded", key, event)
                .thenAccept(kafkaSuccessLogger())
                .exceptionally(kafkaProducingFailedLogger());
    }

    public void sendResourceDownloadedEvent(String key, MinioEvent event){
        kafkaTemplate.send("events-resource-downloaded", key, event)
                .thenAccept(kafkaSuccessLogger())
                .exceptionally(kafkaProducingFailedLogger());
    }

    private Consumer<SendResult<String, MinioEvent>> kafkaSuccessLogger(){
        return result -> log.info("Event {} with key {} has been successfully sent to topic={} partition={} offset={}",
                result.getProducerRecord().value(),
                result.getProducerRecord().key(),
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }

    private Function<Throwable, ? extends Void> kafkaProducingFailedLogger(){
        return ex -> {
            log.error("Send failed: {}", ex.getMessage(), ex);
            throw new SendFailedKafkaException("Send failed: %s".formatted(ex.getMessage()));
        };
    }
}
