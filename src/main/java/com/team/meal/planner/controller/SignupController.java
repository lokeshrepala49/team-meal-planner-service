package com.team.meal.planner.controller;

import com.team.meal.planner.dto.SignupCreate;
import com.team.meal.planner.entities.Signup;
import com.team.meal.planner.service.SignupService;
import com.team.meal.planner.dto.SignupResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/signups")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping
    public ResponseEntity<Signup> createSignup(@Valid @RequestBody SignupCreate dto) {
        SignupResult result = signupService.createSignup(dto.getMealId(), dto.getPersonId(), dto.getNote());
        if (!result.isCreated()) {
            return ResponseEntity.ok(result.getSignup());
        }
        Signup saved = result.getSignup();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Signup>> listPersonSignups(
            @RequestParam Long personId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "day") String range
    ) {
        List<Signup> signups = signupService.listPersonSignups(personId, date, range);
        return ResponseEntity.ok(signups);
    }
}
