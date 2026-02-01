package com.famistry.famistry_personnel.dto;

import java.util.Map;

public class AnalyticsEventDto {
    private String eventType;
    private String userId;
    private String entityId;
    private String entityType;
    private Map<String, Object> metadata;
    private long timestamp;

    public AnalyticsEventDto() {}
    public AnalyticsEventDto(String eventType, String userId, String entityId, String entityType, Map<String, Object> metadata, long timestamp) {
        this.eventType = eventType;
        this.userId = userId;
        this.entityId = entityId;
        this.entityType = entityType;       
        this.metadata = metadata;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}