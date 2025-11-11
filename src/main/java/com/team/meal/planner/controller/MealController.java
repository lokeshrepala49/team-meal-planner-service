package com.team.meal.planner.controller;

import com.team.meal.planner.dto.MealCreate;
import com.team.meal.planner.dto.MealDetails;
import com.team.meal.planner.dto.MealUpdate;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.service.MealService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping
    public ResponseEntity<Meal> createMeal(@Valid @RequestBody MealCreate dto) {
        Meal saved = mealService.createMeal(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    public ResponseEntity<Page<Meal>> listMeals(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date,desc") String sort
    ) {
        return ResponseEntity.ok(mealService.listMeals(dateFrom, dateTo, cuisine, tag, page, size, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealDetails> getMeal(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.getMealDetails(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meal> updateMeal(@PathVariable Long id, @Valid @RequestBody MealUpdate request) {
        return ResponseEntity.ok(mealService.updateMeal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }
}
