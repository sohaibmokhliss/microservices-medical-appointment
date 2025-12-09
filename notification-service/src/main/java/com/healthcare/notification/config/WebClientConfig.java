package com.healthcare.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient smsWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.sms-provider.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient emailWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.resend.com")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer re_LBcwwmd7_4kCAGhXmgKddp9zwjMBL2hi9")
                .build();
    }
}
