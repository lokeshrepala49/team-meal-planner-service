package com.team.meal.planner.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SignupCreate {
    @NotNull private Long mealId;
    @NotNull private Long personId;
    @Size(max = 500) private String note;
    // getters/setters
    public Long getMealId(){return mealId;} public void setMealId(Long m){mealId=m;}
    public Long getPersonId(){return personId;} public void setPersonId(Long p){personId=p;}
    public String getNote(){return note;} public void setNote(String n){note=n;}
}
