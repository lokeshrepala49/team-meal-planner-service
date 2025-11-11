package com.team.meal.planner.repository;

import com.team.meal.planner.entities.Meal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MealRepositoryTest {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByFilters_returnsMatchingMeals() {
        Meal meal1 = new Meal();
        meal1.setTitle("Vegan Lunch");
        meal1.setCuisine("Indian");
        meal1.setDate(LocalDateTime.now().plusDays(2));
        meal1.setTags(Set.of("VEGAN_FRIENDLY"));
        meal1.setMaxAttendees(20);

        Meal meal2 = new Meal();
        meal2.setTitle("Italian Dinner");
        meal2.setCuisine("Italian");
        meal2.setDate(LocalDateTime.now().plusDays(3));
        meal2.setTags(Set.of("VEGETARIAN_FRIENDLY"));
        meal2.setMaxAttendees(15);

        mealRepository.save(meal1);
        mealRepository.save(meal2);

        Page<Meal> result = mealRepository.findByFilters(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                "Indian",
                "VEGAN_FRIENDLY",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Vegan Lunch");
    }

    @Test
    void findByIdForUpdate_appliesLock() {
        Meal meal = new Meal();
        meal.setTitle("Locked Meal");
        meal.setDate(LocalDateTime.now().plusDays(1));
        meal.setCuisine("Thai");
        meal.setMaxAttendees(5);
        meal.setTags(Set.of("VEGAN_FRIENDLY"));
        mealRepository.saveAndFlush(meal);

        Meal lockedMeal = mealRepository.findByIdForUpdate(meal.getId()).orElseThrow();
        entityManager.lock(lockedMeal, LockModeType.PESSIMISTIC_WRITE);

        assertThat(lockedMeal.getId()).isEqualTo(meal.getId());
    }
}
