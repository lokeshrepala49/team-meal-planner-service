package com.team.meal.planner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PersonCreate {
    @NotBlank private String name;
    @Email private String email;
    public String getName(){return name;} public void setName(String n){name=n;}
    public String getEmail(){return email;} public void setEmail(String e){email=e;}
}
