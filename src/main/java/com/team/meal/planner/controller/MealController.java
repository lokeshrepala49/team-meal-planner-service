package com.team.meal.planner.controller;

import com.team.meal.planner.dto.MealCreate;
import com.team.meal.planner.dto.MealDetails;
import com.team.meal.planner.dto.MealUpdate;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.exception.BadRequestException;
import com.team.meal.planner.exception.ConflictException;
import com.team.meal.planner.repository.MealRepository;
import com.team.meal.planner.repository.SignupRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealRepository mealRepository;
    private final SignupRepository signupRepository;

    public MealController(MealRepository mealRepository, SignupRepository signupRepository) {
        this.mealRepository = mealRepository;
        this.signupRepository = signupRepository;
    }

    @PostMapping
    public ResponseEntity<Meal> createMeal(@Valid @RequestBody MealCreate dto) {
        Meal m = new Meal();
        m.setTitle(dto.getTitle());
        m.setCuisine(dto.getCuisine());
        m.setDate(dto.getDate());
        m.setTags(dto.getTags());
        m.setMaxAttendees(dto.getMaxAttendees());
        Meal saved = mealRepository.save(m);
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
        // load all and filter in-memory for brevity
        List<Meal> all = mealRepository.findAll(Sort.by(parseSort(sort)));
        List<Meal> filtered = all.stream()
                .filter(m -> {
                    LocalDateTime dt = m.getDate();
                    if (dateFrom != null && (dt == null || dt.toLocalDate().isBefore(dateFrom))) return false;
                    if (dateTo != null && (dt == null || dt.toLocalDate().isAfter(dateTo))) return false;
                    if (cuisine != null && (m.getCuisine() == null || !m.getCuisine().equalsIgnoreCase(cuisine))) return false;
                    if (tag != null && (m.getTags() == null || !m.getTags().contains(tag))) return false;
                    return true;
                }).collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<Meal> content = start <= end ? filtered.subList(start, end) : Collections.emptyList();
        Page<Meal> result = new PageImpl<>(content, PageRequest.of(page, size, Sort.by(parseSort(sort))), filtered.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealDetails> getMeal(@PathVariable Long id) {
        Optional<Meal> opt = mealRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Meal meal = opt.get();
        long attendees = signupRepository.countByMealId(id);
        MealDetails details = new MealDetails(meal, attendees);
        return ResponseEntity.ok(details);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meal> updateMeal(@PathVariable Long id, @Valid @RequestBody MealUpdate request) {
        Optional<Meal> mOpt = mealRepository.findById(id);
        if (mOpt.isEmpty()) return ResponseEntity.notFound().build();
        Meal meal = mOpt.get();

        if (request.getDate() != null) meal.setDate(request.getDate());
        if (request.getTitle() != null) meal.setTitle(request.getTitle());
        if (request.getCuisine() != null) meal.setCuisine(request.getCuisine());
        if (request.getTags() != null) meal.setTags(request.getTags());
        if (request.getMaxAttendees() != null) meal.setMaxAttendees(request.getMaxAttendees());
        if (request.getVersion() != null) meal.setVersion(request.getVersion());

        try {
            Meal saved = mealRepository.save(meal);
            return ResponseEntity.ok(saved);
        } catch (OptimisticLockException | org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("Concurrent update conflict");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        long count = signupRepository.countByMealId(id);
        if (count > 0) {
            throw new BadRequestException("Cannot delete meal with existing signups");
        }
        if (!mealRepository.existsById(id)) return ResponseEntity.notFound().build();
        mealRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        String prop = parts.length > 0 ? parts[0] : "date";
        Sort.Direction dir = (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(dir, prop);
    }
}
