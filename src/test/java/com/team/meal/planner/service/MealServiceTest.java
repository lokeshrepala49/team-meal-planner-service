package com.team.meal.planner.service;

import com.team.meal.planner.dto.MealUpdate;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.exception.ConflictException;
import com.team.meal.planner.repository.MealRepository;
import com.team.meal.planner.repository.SignupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MealServiceTest {

    MealRepository mealRepository;
    SignupRepository signupRepository;
    MealService mealService;

    @BeforeEach
    void setUp() {
        mealRepository = mock(MealRepository.class);
        signupRepository = mock(SignupRepository.class);
        mealService = new MealService(mealRepository, signupRepository);
    }

    @Test
    void updateMeal_conflict_onOptimisticLocking() {
        // Arrange: existing meal
        Meal existing = new Meal();
        existing.setDate(LocalDateTime.now().plusDays(1));
        existing.setTitle("Pasta");
        existing.setCuisine("Italian");
        existing.setMaxAttendees(10);

        when(mealRepository.findById(5L)).thenReturn(Optional.of(existing));

        MealUpdate update = new MealUpdate();
        update.setTitle("Updated Pasta");
        update.setVersion(1L);

        // Simulate optimistic locking failure during save
        when(mealRepository.save(any(Meal.class))).thenThrow(new OptimisticLockingFailureException("Optimistic lock failed"));

        // Act & Assert
        assertThrows(ConflictException.class, () -> mealService.updateMeal(5L, update));

        verify(mealRepository, times(1)).findById(5L);
        verify(mealRepository, times(1)).save(any(Meal.class));
    }
}

