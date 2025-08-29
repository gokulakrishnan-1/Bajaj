# Bajaj Finserv Health Qualifier – Java Solution

This project contains a self‑contained Spring Boot application created as a solution for the **Bajaj Finserv Health Qualifier (Java track)**.  The objective of the qualifier is to automate the process of registering a webhook with the hiring API, solving an assigned SQL problem, and submitting the final answer through the returned webhook.  The assignment details are provided in the supplied PDF files.

## Problem summary

When the application starts it must:

1. Send a `POST` request to `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA` with the candidate’s name, registration number and email.  The API responds with a `webhook` URL and an `accessToken`.
2. Determine which SQL question to solve based on the last two digits of the registration number:
   * **Odd last two digits** → Question 1 (not required here).
   * **Even last two digits** → Question 2.  The question asks for the count of employees who are younger than each employee within the same department.  You must return `EMP_ID`, `FIRST_NAME`, `LAST_NAME`, `DEPARTMENT_NAME` and `YOUNGER_EMPLOYEES_COUNT`, ordered by `EMP_ID` descending.
3. Submit the final SQL query to the returned webhook URL using the `accessToken` as a JWT in the `Authorization` header.

### Solution for Question 2

Employees with a later date of birth are considered younger.  The following SQL satisfies the requirements:

```sql
SELECT
    e1.EMP_ID,
    e1.FIRST_NAME,
    e1.LAST_NAME,
    d.DEPARTMENT_NAME,
    COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
FROM EMPLOYEE e1
JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
LEFT JOIN EMPLOYEE e2
    ON e2.DEPARTMENT = e1.DEPARTMENT
    AND e2.DOB > e1.DOB
GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
ORDER BY e1.EMP_ID DESC;
```

This query compares each employee (`e1`) to all others (`e2`) within the same department.  The condition `e2.DOB > e1.DOB` counts employees whose date of birth is after the current employee’s, i.e. those who are younger.  `LEFT JOIN` ensures employees with no younger colleagues are included with a zero count.

## Project structure

```
├── bajaj-health/
│   ├── pom.xml                  # Maven build configuration
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/bajajhealth/
│   │   │   │   ├── BajajHealthApplication.java   # Spring Boot entry point
│   │   │   │   ├── StartupRunner.java            # Executes workflow on startup
│   │   │   │   └── WebhookResponse.java          # DTO for API response
│   │   │   └── resources/
│   │   │       └── application.properties        # Candidate configuration
│   └── README.md               # This file
```

## How to build and run

> **Prerequisite:** Ensure you have JDK 17+ and Maven installed.  In this coding environment `javac` and Maven are not available, so you must build the project on your local machine.

1. Clone or download this repository onto your machine.
2. Modify `src/main/resources/application.properties` and replace the placeholders `REPLACE_WITH_YOUR_NAME` and `REPLACE_WITH_YOUR_EMAIL` with your actual details.  Ensure `candidate.regNo` matches your registration number (e.g. `3752`).
3. From the project root (`bajaj-health`), build the JAR:
   ```bash
   mvn clean package
   ```
   This will create a file named `bajaj-health-1.0.0.jar` in the `target` directory.
4. Run the application using:
   ```bash
   java -jar target/bajaj-health-1.0.0.jar
   ```
   When executed, the application will log the registration process, compute the appropriate SQL query, and submit it to the webhook.

## Submitting your assessment

According to the instructions, you need to provide the following when submitting your qualifier:

1. **Public GitHub repository link:** Create a new repository (e.g. `bajaj-health-java`) on your GitHub account and push the contents of the `bajaj-health` folder to it.  Make sure the repository is public so the examiners can access your code.
2. **Compiled JAR:** After building the project locally, upload the generated JAR (located in `target/bajaj-health-1.0.0.jar`) to your GitHub repository.  Provide a raw, publicly downloadable link to this JAR.  You can obtain the raw link by clicking on the JAR file in GitHub and selecting “Download raw”.
3. **Assessment form:** Navigate to the form URL provided in the question paper and fill in your name, roll number, email, the GitHub repository link and the raw JAR link.  Submit the form to complete your qualifier.

If you encounter any issues while building or running this project, double‑check your Java and Maven installations.  The Spring Boot configuration deliberately disables the web server (`spring.main.web-application-type=NONE`) so that the application will exit once the workflow is complete.
