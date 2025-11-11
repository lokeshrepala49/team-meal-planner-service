package com.team.meal.planner.service;

import com.team.meal.planner.dto.SignupResult;
import com.team.meal.planner.entities.*;
import com.team.meal.planner.exception.BadRequestException;
import com.team.meal.planner.exception.ConflictException;
import com.team.meal.planner.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SignupService {

    private final SignupRepository signupRepository;
    private final MealRepository mealRepository;
    private final PersonRepository personRepository;

    public SignupService(SignupRepository signupRepository,
                         MealRepository mealRepository,
                         PersonRepository personRepository) {
        this.signupRepository = signupRepository;
        this.mealRepository = mealRepository;
        this.personRepository = personRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SignupResult createSignup(Long mealId, Long personId, String note) {
        Meal meal = mealRepository.findByIdForUpdate(mealId)
                .orElseThrow(() -> new BadRequestException("Meal not found"));
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new BadRequestException("Person not found"));

        // 1 Idempotent create — return existing
        Optional<Signup> existing = signupRepository.findByMealIdAndPersonId(mealId, personId);
        if (existing.isPresent()) {
            return new SignupResult(existing.get(), false);
        }

        // 2 One meal per day
        ensureOneMealPerDay(meal, person);

        // 3 Dietary restrictions
        ensureDietaryCompatibility(meal, person);

        // 4 Capacity enforcement (transactional)
        ensureMealHasCapacity(meal);

        // Save new signup
        Signup signup = new Signup();
        signup.setMeal(meal);
        signup.setPerson(person);
        signup.setNote(note);

        Signup saved = signupRepository.save(signup);
        return new SignupResult(saved, true);
    }

    private void ensureOneMealPerDay(Meal meal, Person person) {
        LocalDate mealDay = meal.getDate().toLocalDate();
        LocalDateTime start = mealDay.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        boolean exists = signupRepository.existsByPersonIdAndMealDateRange(person.getId(), start, end);
        if (exists) {
            throw new ConflictException("Person already signed up for another meal on this day");
        }
    }

    private void ensureDietaryCompatibility(Meal meal, Person person) {
        for (DietaryTag tag : person.getDietaryTags()) {
            if (tag == null || tag == DietaryTag.NONE) continue;

            boolean match = meal.getTags().stream()
                    .filter(Objects::nonNull)
                    .anyMatch(t -> t.equalsIgnoreCase(tag.name()) || t.startsWith(tag.name() + "_"));

            if (!match) {
                throw new ConflictException("Meal does not satisfy dietary requirement: " + tag.name());
            }
        }
    }

    private void ensureMealHasCapacity(Meal meal) {
        long current = signupRepository.countByMealId(meal.getId());
        Integer max = meal.getMaxAttendees();
        if (max != null && current >= max) {
            throw new ConflictException("Meal is full");
        }
    }

    public List<Signup> listPersonSignups(Long personId, LocalDate date, String range) {
        if (!personRepository.existsById(personId)) {
            throw new BadRequestException("Person not found");
        }

        LocalDate baseDate = (date == null) ? LocalDate.now() : date;
        LocalDateTime start, end;

        if ("week".equalsIgnoreCase(range)) {
            start = baseDate.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            end = start.plusDays(7).minusNanos(1);
        } else {
            start = baseDate.atStartOfDay();
            end = baseDate.atTime(java.time.LocalTime.MAX);
        }

        return signupRepository.findByPersonIdAndDateBetween(personId, start, end);
    }
}
