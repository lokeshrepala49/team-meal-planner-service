package com.team.meal.planner.controller;

import com.team.meal.planner.dto.MealCreate;
import com.team.meal.planner.dto.MealDetails;
import com.team.meal.planner.dto.MealUpdate;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.service.MealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;

@Tag(name = "Meals", description = "Endpoints for managing meal creation, updates, and listings")
@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @Operation(
            summary = "Create a new meal",
            description = "Creates a new meal entry with title, cuisine, date, tags, and maxAttendees",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Meal created successfully",
                            content = @Content(schema = @Schema(implementation = Meal.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<Meal> createMeal(@Valid @RequestBody MealCreate dto) {
        Meal saved = mealService.createMeal(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }


    @Operation(
            summary = "List meals",
            description = "Retrieve meals filtered by date range, cuisine, and tag with pagination and sorting",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Meals retrieved successfully")
            }
    )
    @GetMapping
    public ResponseEntity<Page<Meal>> listMeals(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date,desc") String sort
    ) {
        return ResponseEntity.ok(mealService.listMeals(dateFrom, dateTo, cuisine, tag, page, size, sort));
    }

    @Operation(
            summary = "Get meal details",
            description = "Retrieve full meal information including number of attendees",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Meal details retrieved",
                            content = @Content(schema = @Schema(implementation = MealDetails.class))),
                    @ApiResponse(responseCode = "404", description = "Meal not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<MealDetails> getMeal(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.getMealDetails(id));
    }

    @Operation(
            summary = "Update a meal",
            description = "Update meal information such as title, cuisine, date, tags, and maxAttendees",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Meal updated successfully",
                            content = @Content(schema = @Schema(implementation = Meal.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Meal not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Meal> updateMeal(@PathVariable Long id, @Valid @RequestBody MealUpdate request) {
        return ResponseEntity.ok(mealService.updateMeal(id, request));
    }

    @Operation(
            summary = "Delete a meal",
            description = "Delete a meal by its ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Meal deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Meal not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }
}
