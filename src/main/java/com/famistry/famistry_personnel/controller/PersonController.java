package com.famistry.famistry_personnel.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.famistry.famistry_personnel.model.Person;
import com.famistry.famistry_personnel.model.Relationship;
import com.famistry.famistry_personnel.service.PersonService;

@RestController
@RequestMapping("/api/person")
public class PersonController {
    private final PersonService svc;

    public PersonController(PersonService svc) { this.svc = svc; }

    @PostMapping
    public ResponseEntity<Person> create(@Valid @RequestBody Person p) {
        Person created = svc.create(p);
        return ResponseEntity.created(URI.create("/api/people/" + created.getId())).body(created);
    }

    @GetMapping
    public List<Person> list() { return svc.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Person> get(@PathVariable String id) {
        return svc.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Person> update(@PathVariable String id, @Valid @RequestBody Person p) {
        return ResponseEntity.ok(svc.update(id, p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            svc.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/relationships")
    public ResponseEntity<?> addRelationship(@PathVariable String id, @RequestBody Relationship rel) {
        return svc.addRelationship(id, rel).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/relationships/{targetId}")
    public ResponseEntity<?> removeRelationship(@PathVariable String id, @PathVariable String targetId) {
        return svc.removeRelationship(id, targetId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/graph")
    public Map<String, Object> graph(@PathVariable String id, @RequestParam(defaultValue = "1") int depth) {
        return svc.graph(id, Math.max(0, depth));
    }

    @GetMapping("/{id}/relations/{rel}")
    public ResponseEntity<?> relations(@PathVariable String id, @PathVariable String rel) {
        switch (rel.toLowerCase()) {
            case "parents":
                return ResponseEntity.ok(svc.parents(id));
            case "children":
                return ResponseEntity.ok(svc.children(id));
            case "siblings":
                return ResponseEntity.ok(svc.siblings(id));
            case "grandparents":
                return ResponseEntity.ok(svc.grandparents(id));
            case "grandchildren":
                return ResponseEntity.ok(svc.grandchildren(id));
            case "parents-siblings":
            case "parentsiblings":
                return ResponseEntity.ok(svc.parentsSiblings(id));
            case "cousins":
                return ResponseEntity.ok(svc.cousins(id));
            default:
                return ResponseEntity.badRequest().body("Unknown relation: " + rel);
        }
    }
}
