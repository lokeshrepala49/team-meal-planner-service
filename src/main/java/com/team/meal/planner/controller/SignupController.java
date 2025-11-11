package com.team.meal.planner.controller;

import com.team.meal.planner.dto.SignupCreate;
import com.team.meal.planner.entities.Person;
import com.team.meal.planner.entities.Signup;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.exception.BadRequestException;
import com.team.meal.planner.repository.MealRepository;
import com.team.meal.planner.repository.PersonRepository;
import com.team.meal.planner.repository.SignupRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/signups")
public class SignupController {

    private final SignupRepository signupRepository;
    private final MealRepository mealRepository;
    private final PersonRepository personRepository;

    public SignupController(SignupRepository signupRepository, MealRepository mealRepository, PersonRepository personRepository) {
        this.signupRepository = signupRepository;
        this.mealRepository = mealRepository;
        this.personRepository = personRepository;
    }

    @PostMapping
    public ResponseEntity<Signup> createSignup(@Valid @RequestBody SignupCreate dto) {
        Optional<Meal> mOpt = mealRepository.findById(dto.getMealId());
        if (mOpt.isEmpty()) throw new BadRequestException("Meal not found");
        Meal meal = mOpt.get();

        Optional<Person> pOpt = personRepository.findById(dto.getPersonId());
        if (pOpt.isEmpty()) throw new BadRequestException("Person not found");


        if (meal.getDate() != null && meal.getDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot sign up for past meals");
        }

        long existing = signupRepository.countByMealId(meal.getId());
        if (meal.getMaxAttendees() != null && existing >= meal.getMaxAttendees()) {
            throw new BadRequestException("Meal is full");
        }

        boolean already = signupRepository.existsByMealIdAndPersonId(meal.getId(), dto.getPersonId());
        if (already) throw new BadRequestException("Person already signed up for this meal");

        Signup s = new Signup();
        s.setMeal(meal);
        s.setPerson(pOpt.get());
        s.setNote(dto.getNote());

        Signup saved = signupRepository.save(s);

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
        if (!personRepository.existsById(personId)) throw new BadRequestException("Person not found");

        LocalDateTime start;
        LocalDateTime end;
        if (date == null) date = LocalDate.now();
        if ("week".equalsIgnoreCase(range)) {
            start = date.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            end = start.plusDays(7).minusNanos(1);
        } else {
            start = date.atStartOfDay();
            end = date.atTime(LocalTime.MAX);
        }
        List<Signup> signups = signupRepository.findByPersonIdAndDateBetween(personId, start, end);
        return ResponseEntity.ok(signups);
    }
}
