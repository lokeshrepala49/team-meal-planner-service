package com.team.meal.planner.repository;

import com.team.meal.planner.entities.Meal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Meal m WHERE m.id = :id")
    Optional<Meal> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT m FROM Meal m
         WHERE (:dateFrom IS NULL OR m.date >= :dateFrom)
           AND (:dateTo IS NULL OR m.date <= :dateTo)
           AND (:cuisine IS NULL OR LOWER(m.cuisine) = LOWER(:cuisine))
           AND (:tag IS NULL OR :tag IN elements(m.tags))
        """)
    Page<Meal> findByFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("cuisine") String cuisine,
            @Param("tag") String tag,
            Pageable pageable
    );
}
