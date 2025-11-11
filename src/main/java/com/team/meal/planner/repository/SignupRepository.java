package com.team.meal.planner.repository;

import com.team.meal.planner.entities.Signup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SignupRepository extends JpaRepository<Signup, Long> {

    @Query("select count(s) from Signup s where s.meal.id = :mealId")
    long countByMealId(@Param("mealId") Long mealId);

    @Query("select s from Signup s where s.person.id = :personId and s.meal.date >= :start and s.meal.date < :end")
    List<Signup> findByPersonIdAndMealDateRange(@Param("personId") Long personId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Duplicate/alias to match controller usage
    @Query("select s from Signup s where s.person.id = :personId and s.meal.date >= :start and s.meal.date < :end")
    List<Signup> findByPersonIdAndDateBetween(@Param("personId") Long personId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select s from Signup s where s.meal.id = :mealId and s.person.id = :personId")
    Optional<Signup> findByMealIdAndPersonId(@Param("mealId") Long mealId, @Param("personId") Long personId);

    // Derived query to check existence of a signup for a given meal and person
    boolean existsByMealIdAndPersonId(Long mealId, Long personId);


}
