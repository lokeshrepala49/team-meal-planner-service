package com.team.meal.planner.service;

import com.team.meal.planner.dto.SignupResult;
import com.team.meal.planner.entities.DietaryTag;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.entities.Person;
import com.team.meal.planner.entities.Signup;
import com.team.meal.planner.exception.ConflictException;
import com.team.meal.planner.repository.MealRepository;
import com.team.meal.planner.repository.PersonRepository;
import com.team.meal.planner.repository.SignupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SignupBusinessRulesTest {

    MealRepository mealRepository;
    PersonRepository personRepository;
    SignupRepository signupRepository;
    SignupService signupService;

    @BeforeEach
    void setUp() {
        mealRepository = mock(MealRepository.class);
        personRepository = mock(PersonRepository.class);
        signupRepository = mock(SignupRepository.class);
        signupService = new SignupService(signupRepository, mealRepository, personRepository);
    }

    @Test
    void personCannotSignUpMoreThanOncePerDay() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.of(LocalDate.now().plusDays(1), java.time.LocalTime.NOON));

        Person person = new Person();

        when(mealRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(2L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(1L, 2L)).thenReturn(Optional.empty());
        when(signupRepository.existsByPersonIdAndMealDateRange(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> signupService.createSignup(1L, 2L, "note"));
        verify(signupRepository, never()).save(any());
    }

    @Test
    void dietaryRestrictionsMustBeSatisfied() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(2));
        meal.setTags(new HashSet<>(List.of("VEGETARIAN_FRIENDLY")));

        Person person = new Person();
        person.setDietaryTags(new HashSet<>(List.of(DietaryTag.VEGAN)));

        when(mealRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(4L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(3L, 4L)).thenReturn(Optional.empty());
        when(signupRepository.existsByPersonIdAndMealDateRange(eq(4L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(signupRepository.countByMealId(3L)).thenReturn(0L);

        assertThrows(ConflictException.class, () -> signupService.createSignup(3L, 4L, "note"));
        verify(signupRepository, never()).save(any());
    }

    @Test
    void maxAttendeesEnforced_transactionally() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(3));
        meal.setMaxAttendees(1);

        Person person = new Person();

        when(mealRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(6L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(5L, 6L)).thenReturn(Optional.empty());
        when(signupRepository.existsByPersonIdAndMealDateRange(eq(6L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(signupRepository.countByMealId(5L)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> signupService.createSignup(5L, 6L, "note"));
        verify(signupRepository, never()).save(any());
    }

    @Test
    void duplicateSignupIsIdempotent_returnsExisting() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(4));

        Person person = new Person();

        Signup existing = new Signup();
        existing.setMeal(meal);
        existing.setPerson(person);

        when(mealRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(8L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(7L, 8L)).thenReturn(Optional.of(existing));

        SignupResult res = signupService.createSignup(7L, 8L, "note");

        assertFalse(res.isCreated());
        assertEquals(existing, res.getSignup());
        verify(signupRepository, never()).save(any());
    }

    @Test
    void successfulSignup_persistsNewSignup() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(5));
        meal.setMaxAttendees(10);
        meal.setTags(new HashSet<>(List.of("VEGAN_FRIENDLY")));

        Person person = new Person();
        person.setDietaryTags(new HashSet<>(List.of(DietaryTag.VEGAN)));

        when(mealRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(10L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(9L, 10L)).thenReturn(Optional.empty());
        when(signupRepository.existsByPersonIdAndMealDateRange(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(signupRepository.countByMealId(9L)).thenReturn(1L);

        Signup captured = new Signup();
        when(signupRepository.save(any(Signup.class))).thenAnswer(i -> i.getArgument(0));

        SignupResult result = signupService.createSignup(9L, 10L, "note");

        assertTrue(result.isCreated());
        assertNotNull(result.getSignup());
        assertSame(meal, result.getSignup().getMeal());
        assertSame(person, result.getSignup().getPerson());
        verify(signupRepository, times(1)).save(any(Signup.class));
    }
}
