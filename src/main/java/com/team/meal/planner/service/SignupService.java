package com.team.meal.planner.service;

import com.team.meal.planner.dto.SignupResult;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.entities.Person;
import com.team.meal.planner.entities.Signup;
import com.team.meal.planner.exception.BadRequestException;
import com.team.meal.planner.exception.ConflictException;
import com.team.meal.planner.repository.MealRepository;
import com.team.meal.planner.repository.PersonRepository;
import com.team.meal.planner.repository.SignupRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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

    @Transactional
    public SignupResult createSignup(Long mealId, Long personId, String note) {
        Meal meal = mealRepository.findByIdForUpdate(mealId)
                .orElseThrow(() -> new BadRequestException("Meal not found"));
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new BadRequestException("Person not found"));

        Optional<Signup> existing = signupRepository.findByMealIdAndPersonId(mealId, personId);
        if (existing.isPresent()) {
            return new SignupResult(existing.get(), false);
        }

        LocalDate mealDay = meal.getDate().toLocalDate();
        LocalDateTime start = mealDay.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        boolean alreadySigned = signupRepository.existsByPersonIdAndMealDateRange(personId, start, end);
        if (alreadySigned) {
            throw new ConflictException("Person already signed up for another meal on this day");
        }

        if (!person.isMealSuitable(meal.getTags())) {
            throw new ConflictException("Meal does not satisfy dietary requirements");
        }

        long count = signupRepository.countByMealId(mealId);
        Integer max = meal.getMaxAttendees();
        if (max != null && count >= max) {
            throw new ConflictException("Meal is full");
        }

        Signup signup = new Signup();
        signup.setMeal(meal);
        signup.setPerson(person);
        signup.setNote(note);
        Signup saved = signupRepository.save(signup);

        return new SignupResult(saved, true);
    }

    public List<Signup> listPersonSignups(Long personId, LocalDate date, String range) {
        if (!personRepository.existsById(personId)) {
            throw new BadRequestException("Person not found");
        }

        LocalDate reference = (date != null) ? date : LocalDate.now();
        LocalDateTime start;
        LocalDateTime end;

        if ("week".equalsIgnoreCase(range)) {
            start = reference.with(DayOfWeek.MONDAY).atStartOfDay();
            end = start.plusDays(7).minusNanos(1);
        } else {
            start = reference.atStartOfDay();
            end = reference.atTime(LocalTime.MAX);
        }

        return signupRepository.findByPersonIdAndDateBetween(personId, start, end);
    }
}
