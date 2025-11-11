package com.team.meal.planner.service;

import com.team.meal.planner.dto.MealCreate;
import com.team.meal.planner.dto.MealDetails;
import com.team.meal.planner.dto.MealUpdate;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.exception.BadRequestException;
import com.team.meal.planner.exception.ConflictException;
import com.team.meal.planner.repository.MealRepository;
import com.team.meal.planner.repository.SignupRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Service
public class MealService {

    private final MealRepository mealRepository;
    private final SignupRepository signupRepository;

    public MealService(MealRepository mealRepository, SignupRepository signupRepository) {
        this.mealRepository = mealRepository;
        this.signupRepository = signupRepository;
    }

    public Meal createMeal(MealCreate dto) {
        Meal meal = new Meal();
        meal.setTitle(dto.getTitle());
        meal.setCuisine(dto.getCuisine());
        meal.setDate(dto.getDate());
        meal.setTags(dto.getTags());
        meal.setMaxAttendees(dto.getMaxAttendees());
        return mealRepository.save(meal);
    }

    public Page<Meal> listMeals(LocalDate dateFrom, LocalDate dateTo, String cuisine, String tag,
                                int page, int size, String sort) {
        page = Math.max(page, 0);
        size = (size <= 0) ? 20 : size;
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return mealRepository.findByFilters(
                dateFrom != null ? dateFrom.atStartOfDay() : null,
                dateTo != null ? dateTo.plusDays(1).atStartOfDay() : null,
                cuisine, tag, pageable
        );
    }

    public MealDetails getMealDetails(Long id) {
        Meal meal = findMealOrThrow(id);
        long attendees = signupRepository.countByMealId(id);
        return new MealDetails(meal, attendees);
    }

    public Meal updateMeal(Long id, MealUpdate request) {
        Meal meal = findMealOrThrow(id);
        applyUpdates(meal, request);

        if (request.getVersion() != null && !request.getVersion().equals(meal.getVersion())) {
            throw new ConflictException("Meal version mismatch — please refresh and try again.");
        }

        try {
            return mealRepository.save(meal);
        } catch (OptimisticLockException | OptimisticLockingFailureException ex) {
            throw new ConflictException("Concurrent update conflict");
        }
    }

    public void deleteMeal(Long id) {
        Meal meal = findMealOrThrow(id);
        long signups = signupRepository.countByMealId(id);
        if (signups > 0) throw new BadRequestException("Cannot delete meal with existing signups");
        mealRepository.deleteById(id);
    }

    private Meal findMealOrThrow(Long id) {
        return mealRepository.findById(id).orElseThrow(() -> new BadRequestException("Meal not found"));
    }

    private void applyUpdates(Meal meal, MealUpdate request) {
        Optional.ofNullable(request.getDate()).ifPresent(meal::setDate);
        Optional.ofNullable(request.getTitle()).ifPresent(meal::setTitle);
        Optional.ofNullable(request.getCuisine()).ifPresent(meal::setCuisine);
        Optional.ofNullable(request.getTags()).ifPresent(meal::setTags);
        Optional.ofNullable(request.getMaxAttendees()).ifPresent(meal::setMaxAttendees);
        Optional.ofNullable(request.getVersion()).ifPresent(meal::setVersion);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Direction.DESC, "date");
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        if (!List.of("date", "title", "cuisine", "maxAttendees").contains(field)) {
            field = "date";
        }
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }
}
