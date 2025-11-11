package com.team.meal.planner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Async
    public void sendSignupConfirmation(String recipientEmail, String mealTitle) {
        log.info("Async email stub: notifying {} about signup for meal {}", recipientEmail, mealTitle);
    }
}

