package com.authplatform.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.client")
public class AuthClientProperties {

    private String baseUrl = "https://auth-service-platform.onrender.com";

    private String apiKey;

    private String projectSecret;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getProjectSecret() {
        return projectSecret;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setProjectSecret(String projectSecret) {
        this.projectSecret = projectSecret;
    }
}
