package com.famistry.famistry_personnel.service;

import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import com.famistry.famistry_personnel.dto.AnalyticsEventDto;

@Service
public class AnalyticsProducer {

    private final KafkaTemplate<String, AnalyticsEventDto> kafkaTemplate;

    public AnalyticsProducer(KafkaTemplate<String, AnalyticsEventDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(AnalyticsEventDto event) {
        kafkaTemplate.send("user-activity", event); 
    }
}
