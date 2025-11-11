package com.team.meal.planner.repository;

import com.team.meal.planner.entities.Meal;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Meal m WHERE m.id = :id")
    Optional<Meal> findByIdForUpdate(@Param("id") Long id);
}
