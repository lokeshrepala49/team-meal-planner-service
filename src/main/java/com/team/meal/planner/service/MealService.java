package com.team.meal.planner.service;

import com.team.meal.planner.dto.MealCreate;
import com.team.meal.planner.dto.MealDetails;
import com.team.meal.planner.dto.MealUpdate;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.exception.BadRequestException;
import com.team.meal.planner.exception.ConflictException;
import com.team.meal.planner.repository.MealRepository;
import com.team.meal.planner.repository.SignupRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final SignupRepository signupRepository;

    public MealService(MealRepository mealRepository, SignupRepository signupRepository) {
        this.mealRepository = mealRepository;
        this.signupRepository = signupRepository;
    }

    public Meal createMeal(MealCreate dto) {
        Meal m = new Meal();
        m.setTitle(dto.getTitle());
        m.setCuisine(dto.getCuisine());
        m.setDate(dto.getDate());
        m.setTags(dto.getTags());
        m.setMaxAttendees(dto.getMaxAttendees());
        return mealRepository.save(m);
    }

    public Page<Meal> listMeals(LocalDate dateFrom, LocalDate dateTo, String cuisine, String tag, int page, int size, String sort) {
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
        return new PageImpl<>(content, PageRequest.of(page, size, Sort.by(parseSort(sort))), filtered.size());
    }

    public MealDetails getMealDetails(Long id) {
        Meal meal = mealRepository.findById(id).orElseThrow(() -> new BadRequestException("Meal not found"));
        long attendees = signupRepository.countByMealId(id);
        return new MealDetails(meal, attendees);
    }

    public Meal updateMeal(Long id, MealUpdate request) {
        Meal meal = mealRepository.findById(id).orElseThrow(() -> new BadRequestException("Meal not found"));

        if (request.getDate() != null) meal.setDate(request.getDate());
        if (request.getTitle() != null) meal.setTitle(request.getTitle());
        if (request.getCuisine() != null) meal.setCuisine(request.getCuisine());
        if (request.getTags() != null) meal.setTags(request.getTags());
        if (request.getMaxAttendees() != null) meal.setMaxAttendees(request.getMaxAttendees());
        if (request.getVersion() != null) meal.setVersion(request.getVersion());

        try {
            return mealRepository.save(meal);
        } catch (OptimisticLockException | OptimisticLockingFailureException ex) {
            throw new ConflictException("Concurrent update conflict");
        }
    }

    public void deleteMeal(Long id) {
        long count = signupRepository.countByMealId(id);
        if (count > 0) {
            throw new BadRequestException("Cannot delete meal with existing signups");
        }
        if (!mealRepository.existsById(id)) {
            throw new BadRequestException("Meal not found");
        }
        mealRepository.deleteById(id);
    }

    private Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        String prop = parts.length > 0 ? parts[0] : "date";
        Sort.Direction dir = (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(dir, prop);
    }
}

