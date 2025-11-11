package com.team.meal.planner.dto;

import com.team.meal.planner.entities.Signup;
import java.time.LocalDateTime;

public class SignupResponse {
    private Long id;
    private String note;
    private LocalDateTime createdAt;
    private Long mealId;
    private String mealTitle;
    private String cuisine;
    private LocalDateTime mealDate;
    private Long personId;
    private String personName;

    public SignupResponse(Signup signup) {
        this.id = signup.getId();
        this.note = signup.getNote();
        this.createdAt = signup.getCreatedAt();
        if (signup.getMeal() != null) {
            this.mealId = signup.getMeal().getId();
            this.mealTitle = signup.getMeal().getTitle();
            this.cuisine = signup.getMeal().getCuisine();
            this.mealDate = signup.getMeal().getDate();
        }
        if (signup.getPerson() != null) {
            this.personId = signup.getPerson().getId();
            this.personName = signup.getPerson().getName();
        }
    }

    // Getters only (immutability is fine for a response)
    public Long getId() { return id; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getMealId() { return mealId; }
    public String getMealTitle() { return mealTitle; }
    public String getCuisine() { return cuisine; }
    public LocalDateTime getMealDate() { return mealDate; }
    public Long getPersonId() { return personId; }
    public String getPersonName() { return personName; }
}
