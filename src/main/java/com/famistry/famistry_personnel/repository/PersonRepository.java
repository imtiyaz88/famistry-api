package com.famistry.famistry_personnel.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.famistry.famistry_personnel.model.Person;

public interface PersonRepository extends MongoRepository<Person, String> {
}
