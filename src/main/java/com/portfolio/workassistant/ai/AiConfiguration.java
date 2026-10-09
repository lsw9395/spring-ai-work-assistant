package com.portfolio.workassistant.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfiguration {
    @Bean
    AiGateway aiGateway(@Value("${app.ai.mode:demo}") String mode,
                        @Value("${app.ai.model:gpt-4.1-mini}") String model) {
        if ("demo".equals(mode)) return new DemoAiGateway();
        if (!"openai".equals(mode)) throw new IllegalStateException("APP_AI_MODE must be demo or openai");
        // Read exclusively from the server environment; never from a request or committed property.
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is required when APP_AI_MODE=openai");
        }
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER).build();
        return new OpenAiGateway(client, URI.create("https://api.openai.com/v1/responses"),
                key, model, Duration.ofSeconds(30));
    }
}
