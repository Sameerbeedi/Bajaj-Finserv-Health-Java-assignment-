package com.bajaj.runner;

import com.bajaj.model.WebhookRequest;
import com.bajaj.model.WebhookResponse;
import com.bajaj.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupRunner.class);

    private final WebhookService webhookService;

    @Value("${user.name}")
    private String userName;

    @Value("${user.regNo}")
    private String regNo;

    @Value("${user.email}")
    private String email;

    @Value("${sql.query}")
    private String sqlQuery;

    public StartupRunner(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Override
    public void run(String... args) {
        logger.info("==================================================");
        logger.info("Starting Webhook Solver Application");
        logger.info("==================================================");

        try {
            // Step 1: Create webhook request with user details
            WebhookRequest request = new WebhookRequest(userName, regNo, email);
            
            // Determine which question to solve
            String questionType = webhookService.determineQuestion(regNo);
            logger.info("Question Type: {}", questionType);
            
            // Step 2: Generate webhook
            WebhookResponse webhookResponse = webhookService.generateWebhook(request);
            
            if (webhookResponse == null) {
                logger.error("Failed to generate webhook - received null response");
                return;
            }

            if (webhookResponse.getWebhook() == null || webhookResponse.getAccessToken() == null) {
                logger.error("Invalid webhook response - missing webhook URL or access token");
                return;
            }

            logger.info("==================================================");
            logger.info("Webhook Details:");
            logger.info("URL: {}", webhookResponse.getWebhook());
            logger.info("Token: {}...", webhookResponse.getAccessToken().substring(0, Math.min(20, webhookResponse.getAccessToken().length())));
            logger.info("==================================================");

            // Step 3: Submit the SQL solution
            // Note: Update the sql.query in application.properties with your actual SQL query
            String response = webhookService.submitSolution(
                    webhookResponse.getWebhook(),
                    webhookResponse.getAccessToken(),
                    sqlQuery
            );

            logger.info("==================================================");
            logger.info("Submission Complete!");
            logger.info("Server Response: {}", response);
            logger.info("==================================================");

        } catch (Exception e) {
            logger.error("==================================================");
            logger.error("Error during execution: {}", e.getMessage(), e);
            logger.error("==================================================");
        }
    }
}
