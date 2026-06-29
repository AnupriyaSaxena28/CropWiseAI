package com.cropwise.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    /** Large buffer so Gemini/Sarvam base64 image+audio payloads fit. */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024));
    }
}
