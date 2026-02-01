package com.famistry.famistry_personnel.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import com.famistry.famistry_personnel.dto.AnalyticsEventDto;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestKafkaConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, AnalyticsEventDto> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
}
