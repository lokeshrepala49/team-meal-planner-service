package com.team.meal.planner.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signups")
public class Signup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    private String note;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Meal getMeal() {
        return meal;
    }

    public Person getPerson() {
        return person;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
