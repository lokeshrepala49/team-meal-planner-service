package com.team.meal.planner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.meal.planner.dto.MealCreate;
import com.team.meal.planner.entities.Meal;
import com.team.meal.planner.service.MealService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealController.class)
class MealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MealService mealService;

    @Test
    void createMeal_returns201AndBody() throws Exception {
        MealCreate req = new MealCreate();
        req.setTitle("Indian Dinner");
        req.setCuisine("Indian");
        req.setDate(LocalDateTime.now().plusDays(1));
        req.setTags(Set.of("VEGAN_FRIENDLY"));
        req.setMaxAttendees(10);

        Meal saved = new Meal();
        saved.setTitle("Indian Dinner");
        saved.setCuisine("Indian");
        saved.setDate(req.getDate());
        saved.setMaxAttendees(10);
        saved.setTags(Set.of("VEGAN_FRIENDLY"));
        saved.setVersion(1L);

        Mockito.when(mealService.createMeal(Mockito.any(MealCreate.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Indian Dinner")))
                .andExpect(jsonPath("$.cuisine", is("Indian")))
                .andExpect(jsonPath("$.maxAttendees", is(10)));
    }

    @Test
    void createMeal_invalidRequest_returns400() throws Exception {
        MealCreate invalid = new MealCreate();
        mockMvc.perform(post("/api/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
