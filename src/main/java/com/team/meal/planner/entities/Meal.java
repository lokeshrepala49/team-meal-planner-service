package com.team.meal.planner.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    private String title;

    private String cuisine;

    @ElementCollection
    @CollectionTable(name = "meal_tags", joinColumns = @JoinColumn(name = "meal_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    private Integer maxAttendees;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getCuisine() {
        return cuisine;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Integer getMaxAttendees() {
        return maxAttendees;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
