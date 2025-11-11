package com.team.meal.planner.service;

import com.team.meal.planner.dto.SignupResult;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.entities.Person;
import com.team.meal.planner.entities.Signup;
import com.team.meal.planner.exception.ConflictException;
import com.team.meal.planner.repository.MealRepository;
import com.team.meal.planner.repository.PersonRepository;
import com.team.meal.planner.repository.SignupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SignupService {

    private final SignupRepository signupRepository;
    private final MealRepository mealRepository;
    private final PersonRepository personRepository;
    private final EntityManager entityManager;

    public SignupService(SignupRepository signupRepository, MealRepository mealRepository, PersonRepository personRepository, EntityManager entityManager) {
        this.signupRepository = signupRepository;
        this.mealRepository = mealRepository;
        this.personRepository = personRepository;
        this.entityManager = entityManager;
    }

    /**
     * Create a signup enforcing business rules:
     * 1) person can sign up only once per day
     * 2) person's dietary tags must be satisfied by meal's tags (all restrictive tags must be present)
     * 3) enforce maxAttendees transactionally and return 409 when full
     * 4) idempotent: if a signup already exists for the same meal/person, return it
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SignupResult createSignup(Long mealId, Long personId, String note) {
        Meal meal = mealRepository.findById(mealId).orElseThrow(() -> new IllegalArgumentException("meal not found"));
        Person person = personRepository.findById(personId).orElseThrow(() -> new IllegalArgumentException("person not found"));

        Optional<Signup> existing = signupRepository.findByMealIdAndPersonId(mealId, personId);
        if (existing.isPresent()) {
            return new SignupResult(existing.get(), false);
        }

        // 1) one signup per person per day
        LocalDate mealDay = meal.getDate().toLocalDate();
        // build start/end for the meal day
        LocalDateTime start = mealDay.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        if (!signupRepository.findByPersonIdAndMealDateRange(personId, start, end).isEmpty()) {
            throw new ConflictException("Person already signed up for another meal on this day");
        }

        // 2) dietary tags: person dietaryTags must be subset of meal.tags (note tags are strings like "VEGAN" or descriptive like "VEGAN_FRIENDLY")
        for (var tag : person.getDietaryTags()) {
            if (tag == null) continue;
            String requiredPrefix = tag.name();
            boolean satisfied = meal.getTags().stream().anyMatch(t -> t != null && t.startsWith(requiredPrefix));
            if (!satisfied && tag != com.team.meal.planner.entities.DietaryTag.NONE) {
                throw new ConflictException("Meal does not satisfy dietary requirement: " + tag.name());
            }
        }

        // 3) enforce maxAttendees transactionally
        // Lock meal row to avoid race when counting
        entityManager.lock(meal, LockModeType.PESSIMISTIC_WRITE);

        long count = signupRepository.countByMealId(mealId);
        Integer max = meal.getMaxAttendees();
        if (max != null && count >= max) {
            throw new ConflictException("Meal is full");
        }

        Signup s = new Signup();
        Signup saved = saveNewSignup(s, meal, person, note);
        return new SignupResult(saved, true);
    }

    private Signup saveNewSignup(Signup s, Meal meal, Person person, String note) {
        s.setMeal(meal);
        s.setPerson(person);
        s.setNote(note);
        return signupRepository.save(s);
    }
}
