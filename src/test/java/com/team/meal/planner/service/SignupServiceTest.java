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
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignupServiceTest {

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
    void createSignup_idempotent_existingSignupReturned() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(1));
        meal.setMaxAttendees(5);
        meal.setTitle("T1");
        meal.setCuisine("Indian");

        Person person = new Person();
        person.setName("Alice");

        Signup existing = new Signup();
        existing.setMeal(meal);
        existing.setPerson(person);

        when(mealRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(2L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(1L, 2L)).thenReturn(Optional.of(existing));

        SignupResult res = signupService.createSignup(1L, 2L, "note");

        assertFalse(res.isCreated());
        assertEquals(existing, res.getSignup());
        verify(signupRepository, never()).save(any());
    }

    @Test
    void createSignup_conflict_alreadySignedSameDay() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(2));

        Person person = new Person();
        person.setName("Bob");

        when(mealRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(20L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(10L, 20L)).thenReturn(Optional.empty());
        when(signupRepository.existsByPersonIdAndMealDateRange(eq(20L), any(), any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> signupService.createSignup(10L, 20L, "n"));
        verify(signupRepository, never()).save(any());
    }

    @Test
    void createSignup_conflict_mealFull() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(3));
        meal.setMaxAttendees(2);

        Person person = new Person();
        person.setName("Carol");

        when(mealRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(21L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(11L, 21L)).thenReturn(Optional.empty());
        when(signupRepository.existsByPersonIdAndMealDateRange(eq(21L), any(), any())).thenReturn(false);
        when(signupRepository.countByMealId(11L)).thenReturn(2L);

        assertThrows(ConflictException.class, () -> signupService.createSignup(11L, 21L, "n"));
        verify(signupRepository, never()).save(any());
    }

    @Test
    void createSignup_conflict_dietaryMismatch() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(4));
        meal.setTags(new HashSet<>(List.of("VEGETARIAN_FRIENDLY")));

        Person person = new Person();
        person.setName("Dan");
        person.setDietaryTags(new HashSet<>(List.of(DietaryTag.VEGAN)));

        when(mealRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(22L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(12L, 22L)).thenReturn(Optional.empty());
        when(signupRepository.existsByPersonIdAndMealDateRange(eq(22L), any(), any())).thenReturn(false);
        when(signupRepository.countByMealId(12L)).thenReturn(0L);

        assertThrows(ConflictException.class, () -> signupService.createSignup(12L, 22L, "n"));
        verify(signupRepository, never()).save(any());
    }

    @Test
    void createSignup_successful() {
        Meal meal = new Meal();
        meal.setDate(LocalDateTime.now().plusDays(5));
        meal.setMaxAttendees(10);
        meal.setTags(new HashSet<>(List.of("VEGAN_FRIENDLY")));

        Person person = new Person();
        person.setName("Eve");
        person.setDietaryTags(new HashSet<>(List.of(DietaryTag.VEGAN)));

        when(mealRepository.findByIdForUpdate(13L)).thenReturn(Optional.of(meal));
        when(personRepository.findById(23L)).thenReturn(Optional.of(person));
        when(signupRepository.findByMealIdAndPersonId(13L, 23L)).thenReturn(Optional.empty());
        when(signupRepository.existsByPersonIdAndMealDateRange(eq(23L), any(), any())).thenReturn(false);
        when(signupRepository.countByMealId(13L)).thenReturn(1L);

        ArgumentCaptor<Signup> captor = ArgumentCaptor.forClass(Signup.class);
        when(signupRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        SignupResult res = signupService.createSignup(13L, 23L, "note");

        assertTrue(res.isCreated());
        Signup saved = res.getSignup();
        assertNotNull(saved);
        assertSame(person, saved.getPerson());
        assertSame(meal, saved.getMeal());
        verify(signupRepository, times(1)).save(any(Signup.class));
    }
}
