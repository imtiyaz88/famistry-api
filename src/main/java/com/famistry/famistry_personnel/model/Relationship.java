package com.famistry.famistry_personnel.model;

import jakarta.validation.constraints.NotNull;

public class Relationship {
    private String targetId;

    @NotNull
    private RelationshipType type;

    public Relationship() {}
    public Relationship(String targetId, RelationshipType type) {
        this.targetId = targetId;
        this.type = type;
    }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public RelationshipType getType() { return type; }
    public void setType(RelationshipType type) { this.type = type; }
}
