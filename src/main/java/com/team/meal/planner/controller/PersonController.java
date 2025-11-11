package com.team.meal.planner.controller;

import com.team.meal.planner.dto.PersonCreate;
import com.team.meal.planner.entities.Person;
import com.team.meal.planner.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

@Tag(name = "People", description = "Endpoints for managing people and their dietary preferences")
@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @Operation(
            summary = "Create a new person",
            description = "Registers a person with name, email, and dietary restrictions (e.g., VEGAN, HALAL)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Person created successfully",
                            content = @Content(schema = @Schema(implementation = Person.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<Person> createPerson(@Valid @RequestBody PersonCreate dto) {
        Person saved = personService.createPerson(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }
}
