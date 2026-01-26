package com.famistry.famistry_personnel.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "person")
public class Person {
    @Id
    private String id;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String fatherId;
    private String motherId;
    private String spouseId;
    private boolean isAlive = true;
    private String imageUrl;
    private Map<String, String> attributes = new HashMap<>();
    private List<Relationship> relationships = new ArrayList<>();
    private String comment;

    public Person() {}
    public Person(String name) { this.name = name; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

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

    public List<Relationship> getRelationships() { return relationships; }
    public void setRelationships(List<Relationship> relationships) { this.relationships = relationships; }

    public String getComment(){return comment;}
    public void setComment(String comment){this.comment = comment;}
}
