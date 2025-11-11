package com.team.meal.planner.service;

import com.team.meal.planner.dto.PersonCreate;
import com.team.meal.planner.entities.Person;
import com.team.meal.planner.repository.PersonRepository;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person createPerson(PersonCreate dto) {
        Person p = new Person();
        p.setName(dto.getName());
        p.setEmail(dto.getEmail());
        return personRepository.save(p);
    }
}

