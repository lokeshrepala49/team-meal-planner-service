package com.team.meal.planner.dto;

import com.team.meal.planner.entities.Meal;

public class MealDetails {
    private Meal meal;
    private long attendeeCount;

    public MealDetails(Meal meal, long attendeeCount) {
        this.meal = meal;
        this.attendeeCount = attendeeCount;
    }
    public Meal getMeal(){return meal;}
    public long getAttendeeCount(){return attendeeCount;}
}
