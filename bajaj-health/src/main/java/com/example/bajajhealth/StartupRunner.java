package com.example.bajajhealth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link CommandLineRunner} that executes the hiring workflow as soon as the
 * Spring context is started. It performs the following steps:
 * <ol>
 *     <li>Sends a POST request to generate a webhook using the candidate's
 *     name, registration number and email address.</li>
 *     <li>Determines the SQL problem based on the last two digits of the
 *     registration number.</li>
 *     <li>Submits the solution (final SQL query) to the returned webhook
 *     endpoint using the provided access token.</li>
 * </ol>
 *
 * All necessary candidate information is externalized via application
 * properties so that they can be changed without touching the code. No REST
 * endpoints are exposed: the program executes automatically on startup.
 */
@Component
public class StartupRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupRunner.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${candidate.name}")
    private String candidateName;
    @Value("${candidate.regNo}")
    private String candidateRegNo;
    @Value("${candidate.email}")
    private String candidateEmail;

    public StartupRunner() {
        // Initialize the RestTemplate and ObjectMapper. It's fine to create
        // instances directly here since the runner executes only once.
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Starting Bajaj Health qualifier workflow...");
            WebhookResponse response = generateWebhook();
            if (response == null || response.getWebhook() == null || response.getAccessToken() == null) {
                logger.error("Failed to register webhook or missing response fields.");
                return;
            }
            logger.info("Webhook URL: {}", response.getWebhook());
            logger.info("Access token obtained.");

            // Determine which SQL question to solve based on last two digits
            int lastTwoDigits = Integer.parseInt(candidateRegNo.substring(Math.max(candidateRegNo.length() - 2, 0)));
            String finalQuery = computeSolutionForRegNo(lastTwoDigits);
            logger.info("Computed SQL query: {}", finalQuery);

            // Submit the solution using the webhook URL and access token
            submitSolution(response.getWebhook(), response.getAccessToken(), finalQuery);
            logger.info("Submission completed.");
        } catch (Exception ex) {
            logger.error("An error occurred during the hiring workflow", ex);
        }
    }

    /**
     * Generates a webhook by sending the candidate's information to the hiring
     * API. The API returns a URL for submission and an access token.
     *
     * @return a {@link WebhookResponse} containing the webhook and access token
     */
    private WebhookResponse generateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, String> payload = new HashMap<>();
        payload.put("name", candidateName);
        payload.put("regNo", candidateRegNo);
        payload.put("email", candidateEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        logger.info("Sending POST request to register webhook...");
        return restTemplate.postForObject(url, request, WebhookResponse.class);
    }

    /**
     * Computes the final SQL solution based on the last two digits of the
     * candidate's registration number. If the number is even, question 2 is
     * selected; otherwise question 1. The SQL string returned here should be
     * ready for execution against the provided schema.
     *
     * @param lastTwoDigits the last two digits of the registration number
     * @return a SQL query string representing the solution
     */
    private String computeSolutionForRegNo(int lastTwoDigits) {
        boolean isEven = (lastTwoDigits % 2) == 0;
        if (isEven) {
            // Question 2: count employees younger than each employee within the same department
            return "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                   "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                   "FROM EMPLOYEE e1 " +
                   "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                   "LEFT JOIN EMPLOYEE e2 ON e2.DEPARTMENT = e1.DEPARTMENT AND e2.DOB > e1.DOB " +
                   "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                   "ORDER BY e1.EMP_ID DESC";
        } else {
            // Question 1 is not described in this task; if needed, implement accordingly.
            return "-- TODO: Implement solution for Question 1";
        }
    }

    /**
     * Submits the computed SQL solution to the provided webhook URL. A JWT
     * access token is required and will be set in the Authorization header.
     *
     * @param webhookUrl  the URL to which the final solution should be posted
     * @param accessToken the JWT token supplied by the generateWebhook API
     * @param finalQuery  the SQL query solution to submit
     */
    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        Map<String, String> answer = new HashMap<>();
        answer.put("finalQuery", finalQuery);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(answer, headers);
        logger.info("Submitting solution to webhook...");
        // Use postForObject; response can be logged if required
        try {
            String response = restTemplate.postForObject(webhookUrl, request, String.class);
            logger.info("Submission response: {}", response);
        } catch (Exception e) {
            logger.error("Failed to submit the solution to webhook", e);
        }
    }
}