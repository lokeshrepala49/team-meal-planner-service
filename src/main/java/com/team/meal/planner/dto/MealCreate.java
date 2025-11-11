package com.team.meal.planner.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

public class MealCreate {

    @NotBlank(message = "Title is required")
    private String title;

    private String cuisine;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Meal date must be today or in the future")
    private LocalDateTime date;

    private Set<@NotBlank(message = "Tag cannot be blank") String> tags;

    @NotNull(message = "Max attendees is required")
    @Min(value = 1, message = "Max attendees must be at least 1")
    private Integer maxAttendees;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Integer getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }
}
