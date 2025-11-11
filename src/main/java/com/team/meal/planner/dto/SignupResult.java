package com.team.meal.planner.dto;

import com.team.meal.planner.entities.Signup;

public class SignupResult {
    private final Signup signup;
    private final boolean created;

    public SignupResult(Signup signup, boolean created) {
        this.signup = signup;
        this.created = created;
    }

    public Signup getSignup() {
        return signup;
    }

    public boolean isCreated() {
        return created;
    }
}

