package com.famistry.famistry_personnel.service;

import java.util.*;
import java.lang.reflect.Field;
import java.time.LocalDate;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.famistry.famistry_personnel.model.Person;
import com.famistry.famistry_personnel.model.Relationship;
import com.famistry.famistry_personnel.repository.PersonRepository;
import com.famistry.famistry_personnel.dto.AnalyticsEventDto;
import com.famistry.famistry_personnel.dto.PersonDto;

@Service
public class PersonService {
    private final PersonRepository repo;
    private final KafkaTemplate<String, AnalyticsEventDto> kafkaTemplate;

    public PersonService(PersonRepository repo, KafkaTemplate<String, AnalyticsEventDto> kafkaTemplate) { 
        this.repo = repo; 
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Utility method to convert object fields to metadata map
     * Excludes null values and complex objects (lists, nested objects)
     */
    private Map<String, Object> createMetadataFromObject(Object obj) {
        Map<String, Object> metadata = new HashMap<>();
        if (obj == null) return metadata;
        
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                
                // Skip null values
                if (value == null) continue;
                
                // Skip complex objects that shouldn't be in metadata
                if (value instanceof List || value instanceof Map) continue;
                
                // Convert LocalDate to string for JSON serialization
                if (value instanceof LocalDate) {
                    value = value.toString();
                }
                
                metadata.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                // Skip fields that can't be accessed
                continue;
            }
        }
        return metadata;
    }

    public Person create(Person p) { 
        Person created = repo.save(p);
        AnalyticsEventDto event = new AnalyticsEventDto();
        event.setEventType("PERSON_CREATED");  
        event.setUserId("system");
        event.setEntityId(created.getId());
        event.setEntityType("PERSON");
        event.setMetadata(createMetadataFromObject(created));
        event.setTimestamp(System.currentTimeMillis());
        // In a real application, you would inject KafkaTemplate and send the event to Kafka here
        kafkaTemplate.send("person-events", event);
        return created; }
    public Optional<Person> findById(String id) { return repo.findById(id); }
    public List<Person> findAll() { return repo.findAll(); }
    public Person update(String id, Person updated) {
        return repo.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setGender(updated.getGender());
            existing.setBirthDate(updated.getBirthDate());
            existing.setDeathDate(updated.getDeathDate());
            existing.setFatherId(updated.getFatherId());
            existing.setMotherId(updated.getMotherId());
            existing.setSpouseId(updated.getSpouseId());
            existing.setAlive(updated.isAlive());
            existing.setImageUrl(updated.getImageUrl());
            existing.setAttributes(updated.getAttributes());
            existing.setRelationships(updated.getRelationships());
            existing.setComment(updated.getComment());
            Person saved = repo.save(existing);
            
            AnalyticsEventDto event = new AnalyticsEventDto();
            event.setEventType("PERSON_UPDATED");
            event.setUserId("system");
            event.setEntityId(saved.getId());
            event.setEntityType("PERSON");
            event.setMetadata(createMetadataFromObject(saved));
            event.setTimestamp(System.currentTimeMillis());
            kafkaTemplate.send("person-events", event);
            
            return saved;
        }).orElseGet(() -> {
            updated.setId(id);
            Person saved = repo.save(updated);
            
            AnalyticsEventDto event = new AnalyticsEventDto();
            event.setEventType("PERSON_UPDATED");
            event.setUserId("system");
            event.setEntityId(saved.getId());
            event.setEntityType("PERSON");
            event.setMetadata(createMetadataFromObject(saved));
            event.setTimestamp(System.currentTimeMillis());
            kafkaTemplate.send("person-events", event);
            
            return saved;
        });
    }
    public void delete(String id) throws IllegalArgumentException {
        repo.findById(id).ifPresent(p -> {
            if (!p.getRelationships().isEmpty()) {
                throw new IllegalArgumentException("Cannot delete person with existing relationships. Please remove all relationships first.");
            }
            
            // Store person info for event before deletion
            String personId = p.getId();
            
            repo.deleteById(id);
            
            AnalyticsEventDto event = new AnalyticsEventDto();
            event.setEventType("PERSON_DELETED");
            event.setUserId("system");
            event.setEntityId(personId);
            event.setEntityType("PERSON");
            event.setMetadata(createMetadataFromObject(p));
            event.setTimestamp(System.currentTimeMillis());
            kafkaTemplate.send("person-events", event);
        });
    }

    public Optional<Person> addRelationship(String sourceId, Relationship rel) {
        return repo.findById(sourceId).map(p -> {
            p.getRelationships().removeIf(r -> r.getTargetId().equals(rel.getTargetId()) && r.getType().equals(rel.getType()));
            p.getRelationships().add(rel);
            // Sync fatherId/motherId/spouseId with relationship types
            if (rel.getType() == com.famistry.famistry_personnel.model.RelationshipType.FATHER) {
                p.setFatherId(rel.getTargetId());
            } else if (rel.getType() == com.famistry.famistry_personnel.model.RelationshipType.MOTHER) {
                p.setMotherId(rel.getTargetId());
            } else if (rel.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARENT) {
                // For generic "parent" type, set as mother if target is female, otherwise father
                repo.findById(rel.getTargetId()).ifPresent(target -> {
                    if ("female".equalsIgnoreCase(target.getGender())) {
                        p.setMotherId(rel.getTargetId());
                    } else {
                        p.setFatherId(rel.getTargetId());
                    }
                });
            } else if (rel.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARTNER) {
                p.setSpouseId(rel.getTargetId());
                // Make partner relationship bi-directional
                repo.findById(rel.getTargetId()).ifPresent(target -> {
                    target.getRelationships().removeIf(r -> r.getTargetId().equals(sourceId) && r.getType().equals(com.famistry.famistry_personnel.model.RelationshipType.PARTNER));
                    target.getRelationships().add(new Relationship(sourceId, com.famistry.famistry_personnel.model.RelationshipType.PARTNER));
                    target.setSpouseId(sourceId);
                    repo.save(target);
                });
            }
            Person saved = repo.save(p);
            
            AnalyticsEventDto event = new AnalyticsEventDto();
            event.setEventType("RELATIONSHIP_ADDED");
            event.setUserId("system");
            event.setEntityId(saved.getId());
            event.setEntityType("PERSON");
            event.setMetadata(Map.of(
                "sourceId", sourceId,
                "targetId", rel.getTargetId(),
                "relationshipType", rel.getType().toString()
            ));
            event.setTimestamp(System.currentTimeMillis());
            kafkaTemplate.send("person-events", event);
            
            return saved;
        });
    }

    public Optional<Person> removeRelationship(String sourceId, String targetId) {
        return repo.findById(sourceId).map(p -> {
            // Find the relationship type before removing
            com.famistry.famistry_personnel.model.RelationshipType relType = null;
            for (Relationship r : p.getRelationships()) {
                if (r.getTargetId().equals(targetId)) {
                    relType = r.getType();
                    break;
                }
            }
            
            // Clear fatherId/motherId/spouseId if the removed relationship was a parent or spouse
            for (Relationship r : p.getRelationships()) {
                if (r.getTargetId().equals(targetId)) {
                    if (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.FATHER || 
                        (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARENT && targetId.equals(p.getFatherId()))) {
                        p.setFatherId(null);
                    } else if (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.MOTHER ||
                               (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARENT && targetId.equals(p.getMotherId()))) {
                        p.setMotherId(null);
                    } else if (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARTNER) {
                        p.setSpouseId(null);
                    }
                    break;
                }
            }
            p.getRelationships().removeIf(r -> r.getTargetId().equals(targetId));
            
            // If it was a partner relationship, remove the reverse relationship from target
            if (relType == com.famistry.famistry_personnel.model.RelationshipType.PARTNER) {
                repo.findById(targetId).ifPresent(target -> {
                    target.getRelationships().removeIf(r -> r.getTargetId().equals(sourceId) && r.getType().equals(com.famistry.famistry_personnel.model.RelationshipType.PARTNER));
                    target.setSpouseId(null);
                    repo.save(target);
                });
            }
            
            Person saved = repo.save(p);
            
            AnalyticsEventDto event = new AnalyticsEventDto();
            event.setEventType("RELATIONSHIP_REMOVED");
            event.setUserId("system");
            event.setEntityId(saved.getId());
            event.setEntityType("PERSON");
            event.setMetadata(Map.of(
                "sourceId", sourceId,
                "targetId", targetId,
                "relationshipType", relType != null ? relType.toString() : "UNKNOWN"
            ));
            event.setTimestamp(System.currentTimeMillis());
            kafkaTemplate.send("person-events", event);
            
            return saved;
        });
    }

    public List<PersonDto> graph(String rootId, int depth) {
        if (!repo.existsById(rootId)) {
            return new ArrayList<>();
        }
        
        // First, collect all people to easily find relationships
        List<Person> allPeople = repo.findAll();
        
        Set<String> resultIds = new LinkedHashSet<>();
        
        // Always include the root person
        resultIds.add(rootId);
        
        repo.findById(rootId).ifPresent(rootPerson -> {
            System.out.println("=== Building graph for: " + rootPerson.getName() + " (" + rootId + ") ===");
            
            // 1. Add direct parents
            String fatherId = rootPerson.getFatherId();
            String motherId = rootPerson.getMotherId();
            
            System.out.println("Parents: father=" + fatherId + ", mother=" + motherId);
            
            if (fatherId != null && !fatherId.trim().isEmpty()) {
                resultIds.add(fatherId);
                System.out.println("Added father: " + fatherId);
            }
            if (motherId != null && !motherId.trim().isEmpty()) {
                resultIds.add(motherId);
                System.out.println("Added mother: " + motherId);
            }
            
            // 2. Add grandparents (parents of parents)
            Set<String> grandparentIds = new HashSet<>();
            for (Person person : allPeople) {
                if (person.getId().equals(fatherId) || person.getId().equals(motherId)) {
                    if (person.getFatherId() != null && !person.getFatherId().trim().isEmpty()) {
                        resultIds.add(person.getFatherId());
                        grandparentIds.add(person.getFatherId());
                        System.out.println("Added grandfather (father of " + person.getName() + "): " + person.getFatherId());
                    }
                    if (person.getMotherId() != null && !person.getMotherId().trim().isEmpty()) {
                        resultIds.add(person.getMotherId());
                        grandparentIds.add(person.getMotherId());
                        System.out.println("Added grandmother (mother of " + person.getName() + "): " + person.getMotherId());
                    }
                }
            }
            
            // 3. Add parent's siblings (aunts and uncles)
            // Find all children of grandparents, excluding the direct parents
            System.out.println("Finding parent's siblings (children of grandparents)...");
            for (Person person : allPeople) {
                if (!person.getId().equals(fatherId) && !person.getId().equals(motherId)) {
                    boolean childOfGrandfather = person.getFatherId() != null && !person.getFatherId().trim().isEmpty() && grandparentIds.contains(person.getFatherId());
                    boolean childOfGrandmother = person.getMotherId() != null && !person.getMotherId().trim().isEmpty() && grandparentIds.contains(person.getMotherId());
                    
                    if (childOfGrandfather || childOfGrandmother) {
                        resultIds.add(person.getId());
                        System.out.println("Added parent's sibling: " + person.getName() + " (" + person.getId() + ")");
                        System.out.println("  - Father: " + person.getFatherId() + " (in grandparentIds: " + childOfGrandfather + ")");
                        System.out.println("  - Mother: " + person.getMotherId() + " (in grandparentIds: " + childOfGrandmother + ")");
                    }
                }
            }
            
            // 4. Add children
            List<Person> children = new ArrayList<>();
            System.out.println("Finding children...");
            for (Person person : allPeople) {
                if (rootPerson.getId().equals(person.getFatherId()) || rootPerson.getId().equals(person.getMotherId())) {
                    resultIds.add(person.getId());
                    children.add(person);
                    System.out.println("Added child: " + person.getName() + " (" + person.getId() + ")");
                }
            }
            
            // 4.1. Add siblings of the given person
            System.out.println("Finding siblings of the given person...");
            for (Person person : allPeople) {
                if (!person.getId().equals(rootId)) { // Exclude the root person
                    String personFatherId = person.getFatherId();
                    String personMotherId = person.getMotherId();
                    
                    boolean sameFather = personFatherId != null && !personFatherId.trim().isEmpty() && personFatherId.equals(fatherId);
                    boolean sameMother = personMotherId != null && !personMotherId.trim().isEmpty() && personMotherId.equals(motherId);
                    
                    // They are siblings if they share at least one parent
                    if (sameFather || sameMother) {
                        resultIds.add(person.getId());
                        System.out.println("Added sibling: " + person.getName() + " (" + person.getId() + ")");
                        System.out.println("  - Same father: " + sameFather + " (father: " + personFatherId + ")");
                        System.out.println("  - Same mother: " + sameMother + " (mother: " + personMotherId + ")");
                    }
                }
            }
            
            // 4.2. Add partner of the given person
            System.out.println("Finding partner of the given person...");
            String rootPersonSpouseId = rootPerson.getSpouseId();
            if (rootPersonSpouseId != null && !rootPersonSpouseId.trim().isEmpty()) {
                resultIds.add(rootPersonSpouseId);
                System.out.println("Added partner: " + rootPersonSpouseId + " (partner of " + rootPerson.getName() + ")");
            }
            
            // 5. Add children's partners
            System.out.println("Finding children's partners...");
            for (Person child : children) {
                String spouseId = child.getSpouseId();
                if (spouseId != null && !spouseId.trim().isEmpty()) {
                    resultIds.add(spouseId);
                    System.out.println("Added child's partner: " + spouseId + " (partner of " + child.getName() + ")");
                }
            }
            
            // 6. Add grandchildren (children of children)
            System.out.println("Finding grandchildren...");
            for (Person child : children) {
                for (Person person : allPeople) {
                    String personFatherId = person.getFatherId();
                    String personMotherId = person.getMotherId();
                    boolean childIsFather = personFatherId != null && !personFatherId.trim().isEmpty() && child.getId().equals(personFatherId);
                    boolean childIsMother = personMotherId != null && !personMotherId.trim().isEmpty() && child.getId().equals(personMotherId);
                    
                    if (childIsFather || childIsMother) {
                        resultIds.add(person.getId());
                        System.out.println("Added grandchild: " + person.getName() + " (" + person.getId() + ")");
                    }
                }
            }
            
            System.out.println("=== Final result IDs: " + resultIds + " ===");
        });
        
        // Convert all collected IDs to PersonDto list (same as person API)
        List<PersonDto> result = toDtos(resultIds);
        System.out.println("=== Final result count: " + result.size() + " ===");
        return result;
    }

    // Helper to fetch PersonDto for ids
    private List<PersonDto> toDtos(Collection<String> ids) {
        List<PersonDto> dtos = new ArrayList<>();
        for (String id : ids) {
            repo.findById(id).ifPresent(p -> dtos.add(new PersonDto(p.getId(), p.getName(), p.getBirthDate(), p.getDeathDate(), p.getFatherId(), p.getMotherId(), p.getSpouseId(), p.isAlive(), p.getImageUrl(), p.getAttributes(), p.getComment())));
        }
        return dtos;
    }

    public List<PersonDto> parents(String id) {
        return repo.findById(id)
                .map(p -> {
                    Set<String> parentIds = new LinkedHashSet<>();
                    for (Relationship r : p.getRelationships()) if (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARENT) parentIds.add(r.getTargetId());
                    return toDtos(parentIds);
                }).orElseGet(ArrayList::new);
    }

    public List<PersonDto> children(String id) {
        List<String> childIds = new ArrayList<>();
        for (Person p : repo.findAll()) {
            for (Relationship r : p.getRelationships()) {
                if (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARENT && id.equals(r.getTargetId())) {
                    childIds.add(p.getId());
                }
            }
        }
        return toDtos(childIds);
    }

    public List<PersonDto> siblings(String id) {
        // siblings: other children of the same parents
        Set<String> sibIds = new LinkedHashSet<>();
        List<PersonDto> parents = parents(id);
        for (PersonDto parent : parents) {
            for (Person child : repo.findAll()) {
                for (Relationship r : child.getRelationships()) {
                    if (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARENT && parent.getId().equals(r.getTargetId()) && !child.getId().equals(id)) {
                        sibIds.add(child.getId());
                    }
                }
            }
        }
        return toDtos(sibIds);
    }

    public List<PersonDto> grandparents(String id) {
        Set<String> gp = new LinkedHashSet<>();
        for (PersonDto parent : parents(id)) {
            for (Relationship r : repo.findById(parent.getId()).map(Person::getRelationships).orElse(Collections.emptyList())) {
                if (r.getType() == com.famistry.famistry_personnel.model.RelationshipType.PARENT) gp.add(r.getTargetId());
            }
        }
        return toDtos(gp);
    }

    public List<PersonDto> grandchildren(String id) {
        Set<String> gch = new LinkedHashSet<>();
        for (PersonDto child : children(id)) {
            for (PersonDto gc : children(child.getId())) gch.add(gc.getId());
        }
        return toDtos(gch);
    }

    public List<PersonDto> parentsSiblings(String id) {
        Set<String> ps = new LinkedHashSet<>();
        for (PersonDto parent : parents(id)) {
            for (PersonDto s : siblings(parent.getId())) {
                if (!s.getId().equals(parent.getId())) ps.add(s.getId());
            }
        }
        return toDtos(ps);
    }

    public List<PersonDto> cousins(String id) {
        Set<String> cousins = new LinkedHashSet<>();
        for (PersonDto psib : parentsSiblings(id)) {
            for (PersonDto child : children(psib.getId())) cousins.add(child.getId());
        }
        return toDtos(cousins);
    }
}
