package com.bajaj.service;

import com.bajaj.model.SolutionRequest;
import com.bajaj.model.WebhookRequest;
import com.bajaj.model.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    private final WebClient webClient;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.generate-webhook-path}")
    private String generateWebhookPath;

    @Value("${api.test-webhook-path}")
    private String testWebhookPath;

    public WebhookService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Step 1: Generate webhook by sending POST request with user details
     */
    public WebhookResponse generateWebhook(WebhookRequest request) {
        logger.info("Generating webhook for user: {}", request.getName());
        
        String url = baseUrl + generateWebhookPath;
        
        try {
            WebhookResponse response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WebhookResponse.class)
                    .block();

            if (response != null) {
                logger.info("Webhook generated successfully");
                logger.info("Webhook URL: {}", response.getWebhook());
                logger.info("Access Token received: {}", 
                    response.getAccessToken() != null ? "Yes" : "No");
            }
            
            return response;
        } catch (Exception e) {
            logger.error("Error generating webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate webhook", e);
        }
    }

    /**
     * Step 2: Submit the SQL solution to the webhook URL
     */
    public String submitSolution(String webhookUrl, String accessToken, String sqlQuery) {
        logger.info("Submitting solution to webhook");
        logger.info("Webhook URL: {}", webhookUrl);
        logger.info("SQL Query: {}", sqlQuery);

        SolutionRequest solutionRequest = new SolutionRequest(sqlQuery);

        try {
            String response = webClient.post()
                    .uri(webhookUrl)
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(solutionRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("Solution submitted successfully");
            logger.info("Response: {}", response);
            
            return response;
        } catch (Exception e) {
            logger.error("Error submitting solution: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to submit solution", e);
        }
    }

    /**
     * Determine which question to solve based on registration number
     */
    public String determineQuestion(String regNo) {
        // Extract last two digits from regNo
        String digits = regNo.replaceAll("[^0-9]", "");
        if (digits.length() >= 2) {
            int lastTwoDigits = Integer.parseInt(digits.substring(digits.length() - 2));
            if (lastTwoDigits % 2 == 0) {
                logger.info("Registration number ends with EVEN digits ({}): Solve Question 2", lastTwoDigits);
                return "QUESTION_2_EVEN";
            } else {
                logger.info("Registration number ends with ODD digits ({}): Solve Question 1", lastTwoDigits);
                return "QUESTION_1_ODD";
            }
        }
        logger.warn("Could not determine question from regNo: {}", regNo);
        return "UNKNOWN";
    }
}
