package com.famistry.famistry_personnel.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.famistry.famistry_personnel.dto.AnalyticsEventDto;
import com.famistry.famistry_personnel.service.AnalyticsProducer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsProducer producer;

    public AnalyticsController(AnalyticsProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/event") 
    public ResponseEntity<Void> track(@RequestBody AnalyticsEventDto event) {
        event.setTimestamp(System.currentTimeMillis());
        producer.send(event);
        return ResponseEntity.ok().build();
    }
}
