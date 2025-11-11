package com.team.meal.planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TeamMealPlannerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamMealPlannerServiceApplication.class, args);
	}

}
