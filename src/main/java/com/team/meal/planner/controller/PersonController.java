package com.team.meal.planner.controller;

import com.team.meal.planner.dto.PersonCreate;
import com.team.meal.planner.entities.Person;
import com.team.meal.planner.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonRepository personRepository;

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @PostMapping
    public ResponseEntity<Person> createPerson(@Valid @RequestBody PersonCreate dto) {
        Person p = new Person();
        p.setName(dto.getName());
        p.setEmail(dto.getEmail());
        Person saved = personRepository.save(p);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }
}
