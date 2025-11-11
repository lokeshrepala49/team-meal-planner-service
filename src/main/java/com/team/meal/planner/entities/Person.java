package com.team.meal.planner.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @ElementCollection(targetClass = DietaryTag.class)
    @CollectionTable(name = "person_dietary_tags", joinColumns = @JoinColumn(name = "person_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_tag")
    private Set<DietaryTag> dietaryTags = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Set<DietaryTag> getDietaryTags() {
        return dietaryTags;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDietaryTags(Set<DietaryTag> dietaryTags) {
        this.dietaryTags = dietaryTags;
    }

    public boolean isMealSuitable(Set<String> mealTags) {
        if (mealTags == null || mealTags.isEmpty()) return true;
        for (DietaryTag tag : dietaryTags) {
            if (tag == DietaryTag.NONE) continue;
            boolean satisfied = mealTags.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(mealTag ->
                            mealTag.equalsIgnoreCase(tag.name()) ||
                                    mealTag.toUpperCase().startsWith(tag.name() + "_"));
            if (!satisfied) return false;
        }
        return true;
    }
}
