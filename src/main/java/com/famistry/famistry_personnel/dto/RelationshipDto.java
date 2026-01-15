package com.famistry.famistry_personnel.dto;

public class RelationshipDto {
    private String sourceId;
    private String targetId;
    private String type;

    public RelationshipDto() {}
    public RelationshipDto(String sourceId, String targetId, String type) {
        this.sourceId = sourceId; this.targetId = targetId; this.type = type;
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
