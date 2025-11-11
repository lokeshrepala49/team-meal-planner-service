package com.team.meal.planner.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class MealUpdate {
        private java.time.LocalDateTime date;
        private String title;
        private String cuisine;
        private Set<String> tags;
        private Integer maxAttendees;
        private Long version;

        public LocalDateTime getDate() { return date; }
        public void setDate(java.time.LocalDateTime date) { this.date = date; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCuisine() { return cuisine; }
        public void setCuisine(String cuisine) { this.cuisine = cuisine; }
        public Set<String> getTags() { return tags; }
        public void setTags(Set<String> tags) { this.tags = tags; }
        public Integer getMaxAttendees() { return maxAttendees; }
        public void setMaxAttendees(Integer maxAttendees) { this.maxAttendees = maxAttendees; }
        public Long getVersion() { return version; }
        public void setVersion(Long version) { this.version = version; }
    }