package com.example.bajajhealth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Bajaj Health qualifier application. This Spring Boot
 * application is designed to run without any REST controllers exposed. On
 * startup it triggers a background task that interacts with the Bajaj Finserv
 * Health hiring API. The task will register a webhook using the candidate's
 * details, determine the appropriate SQL problem based on the registration
 * number, and submit the solution.
 */
@SpringBootApplication
public class BajajHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BajajHealthApplication.class, args);
    }
}