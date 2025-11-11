package com.team.meal.planner.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

public class MealCreate {
    @NotBlank private String title;
    private String cuisine;
    @NotNull private LocalDateTime date;
    private Set<String> tags;
    @Min(1) private Integer maxAttendees;
    // getters/setters
    public String getTitle(){return title;} public void setTitle(String t){title=t;}
    public String getCuisine(){return cuisine;} public void setCuisine(String c){c=c;}
    public LocalDateTime getDate(){return date;} public void setDate(LocalDateTime d){date=d;}
    public Set<String> getTags(){return tags;} public void setTags(Set<String> t){tags=t;}
    public Integer getMaxAttendees(){return maxAttendees;} public void setMaxAttendees(Integer m){maxAttendees=m;}
}
