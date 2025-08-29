package com.example.bajajhealth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple DTO used to deserialize the response from the generate webhook API.
 * The JSON payload returned by the API contains a webhook URL where the
 * solution should be posted and an access token used for authentication
 * during submission.
 */
public class WebhookResponse {

    /**
     * The URL to which the final SQL query should be submitted. This field
     * maps to the JSON property "webhook" returned by the API.
     */
    @JsonProperty("webhook")
    private String webhook;

    /**
     * A JWT access token provided by the API. This token must be included in
     * the Authorization header when submitting the final answer.
     */
    @JsonProperty("accessToken")
    private String accessToken;

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}