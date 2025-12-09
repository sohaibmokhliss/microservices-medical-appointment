package com.healthcare.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${sms.api.url}")
    private String smsApiUrl;

    @Value("${email.api.url}")
    private String emailApiUrl;

    @Value("${email.api.key:}")
    private String emailApiKey;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient smsWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(smsApiUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient emailWebClient(WebClient.Builder builder) {
        WebClient.Builder clientBuilder = builder
                .baseUrl(emailApiUrl)
                .defaultHeader("Content-Type", "application/json");

        // Only add Authorization header if API key is provided
        if (emailApiKey != null && !emailApiKey.isEmpty()) {
            clientBuilder.defaultHeader("Authorization", "Bearer " + emailApiKey);
        }

        return clientBuilder.build();
    }
}
