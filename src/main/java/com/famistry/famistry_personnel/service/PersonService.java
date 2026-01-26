package com.famistry.famistry_personnel.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.famistry.famistry_personnel.model.Person;
import com.famistry.famistry_personnel.model.Relationship;
import com.famistry.famistry_personnel.repository.PersonRepository;
import com.famistry.famistry_personnel.dto.PersonDto;
import com.famistry.famistry_personnel.dto.RelationshipDto;

@Service
public class PersonService {
    private final PersonRepository repo;

    public PersonService(PersonRepository repo) { this.repo = repo; }

    public Person create(Person p) { return repo.save(p); }
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
            return repo.save(existing);
        }).orElseGet(() -> {
            updated.setId(id);
            return repo.save(updated);
        });
    }
    public void delete(String id) throws IllegalArgumentException {
        repo.findById(id).ifPresent(p -> {
            if (!p.getRelationships().isEmpty()) {
                throw new IllegalArgumentException("Cannot delete person with existing relationships. Please remove all relationships first.");
            }
            repo.deleteById(id);
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
            return repo.save(p);
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
            
            return repo.save(p);
        });
    }

    public Map<String, Object> graph(String rootId, int depth) {
        Map<String, Object> result = new HashMap<>();
        Map<String, PersonDto> nodes = new LinkedHashMap<>();
        List<RelationshipDto> edges = new ArrayList<>();
        if (!repo.existsById(rootId)) {
            result.put("nodes", nodes.values());
            result.put("edges", edges);
            return result;
        }
        Queue<String> q = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        q.add(rootId);
        seen.add(rootId);
        int level = 0;
        while (!q.isEmpty() && level <= depth) {
            int sz = q.size();
            for (int i = 0; i < sz; i++) {
                String id = q.poll();
                repo.findById(id).ifPresent(p -> {
                    nodes.putIfAbsent(p.getId(), new PersonDto(p.getId(), p.getName(), p.getBirthDate(), p.getDeathDate(), p.getFatherId(), p.getMotherId(), p.getSpouseId(), p.isAlive(), p.getImageUrl(), p.getAttributes(), p.getComment()));
                    for (Relationship r : p.getRelationships()) {
                        String t = r.getType() == null ? null : r.getType().value();
                        edges.add(new RelationshipDto(p.getId(), r.getTargetId(), t));
                        if (!seen.contains(r.getTargetId())) {
                            seen.add(r.getTargetId());
                            q.add(r.getTargetId());
                        }
                    }
                });
            }
            level++;
        }
        result.put("nodes", nodes.values());
        result.put("edges", edges);
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
