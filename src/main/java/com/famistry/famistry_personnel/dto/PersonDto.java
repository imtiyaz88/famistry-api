package com.famistry.famistry_personnel.dto;

import java.time.LocalDate;
import java.util.Map;

public class PersonDto {
    private String id;
    private String name;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String fatherId;
    private String motherId;
    private String spouseId;
    private boolean isAlive;
    private String imageUrl;
    private Map<String, String> attributes;
    private String comment;

    public PersonDto() {}
    public PersonDto(String id, String name, LocalDate birthDate, LocalDate deathDate, String fatherId, String motherId, String spouseId, boolean isAlive, String imageUrl, Map<String, String> attributes, String comment) {
        this.id = id; this.name = name; this.birthDate = birthDate; this.deathDate=deathDate; this.fatherId = fatherId; this.motherId = motherId; this.spouseId = spouseId; this.isAlive = isAlive; this.imageUrl = imageUrl; this.attributes = attributes; this.comment = comment;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDate getDeathDate() { return deathDate; }
    public void setDeathDate(LocalDate deathDate) { this.deathDate = deathDate; }

    public String getFatherId() { return fatherId; }
    public void setFatherId(String fatherId) { this.fatherId = fatherId; }

    public String getMotherId() { return motherId; }
    public void setMotherId(String motherId) { this.motherId = motherId; }

    public String getSpouseId() { return spouseId; }
    public void setSpouseId(String spouseId) { this.spouseId = spouseId; }

    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public String getComment(){return comment;}
    public void setComment(String comment){this.comment = comment;}
}
