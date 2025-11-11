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

    Optional<Signup> findByMealIdAndPersonId(Long mealId, Long personId);

    long countByMealId(Long mealId);

    @Query("SELECT s FROM Signup s WHERE s.person.id = :personId AND s.meal.date BETWEEN :start AND :end")
    List<Signup> findByPersonIdAndDateBetween(@Param("personId") Long personId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Signup s WHERE s.person.id = :personId AND s.meal.date BETWEEN :start AND :end")
    boolean existsByPersonIdAndMealDateRange(@Param("personId") Long personId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}

