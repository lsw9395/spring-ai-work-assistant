package com.portfolio.workassistant;

import com.portfolio.workassistant.ai.*;
import com.portfolio.workassistant.chat.ChatRequest;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;

class OpenAiGatewayTest {
    HttpServer server;
    AtomicReference<String> received = new AtomicReference<>();
    AtomicReference<String> auth = new AtomicReference<>();
    String reply;
    int status = 200;
    long delay;
    @BeforeEach void setup() throws Exception {
        reply = "{\"status\":\"completed\",\"output\":[{\"type\":\"reasoning\"},{\"type\":\"message\",\"content\":[{\"type\":\"output_text\",\"text\":\"첫 문장\"},{\"type\":\"output_text\",\"text\":\"두 문장\"}]}]}";
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/responses", exchange -> {
            received.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var out = exchange.getResponseBody()) { out.write(bytes); }
        });
        server.start();
    }
    @AfterEach void teardown() { server.stop(0); }
    OpenAiGateway gateway(Duration timeout) {
        return new OpenAiGateway(HttpClient.newHttpClient(),
                URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/responses"),
                "test-placeholder", "test-model", timeout);
    }
    ChatRequest request() {
        return new ChatRequest(UUID.randomUUID(), "메일 작성", ChatRequest.TaskType.EMAIL,
                List.of(new ChatRequest.HistoryMessage("user", "이전 질문")));
    }
    @Test void sendsStatelessResponsesRequestAndParsesAllText() {
        assertThat(gateway(Duration.ofSeconds(2)).answer(request())).isEqualTo("첫 문장\n두 문장");
        var json = JsonMapper.builder().build().readTree(received.get());
        assertThat(json.path("store").asBoolean()).isFalse();
        assertThat(json.path("model").asText()).isEqualTo("test-model");
        assertThat(json.path("input").size()).isEqualTo(2);
        assertThat(json.path("max_output_tokens").asInt()).isEqualTo(1000);
        assertThat(auth.get()).isEqualTo("Bearer test-placeholder");
    }
    @Test void rateLimitAndAuthNeverLeakProviderBody() {
        reply = "private upstream detail";
        for (int code : List.of(429, 401, 500)) {
            status = code;
            assertThatThrownBy(() -> gateway(Duration.ofSeconds(2)).answer(request()))
                    .isInstanceOf(AiException.class).hasMessageNotContaining("private");
        }
    }
    @Test void enforcesRequestTimeout() {
        delay = 400;
        assertThatThrownBy(() -> gateway(Duration.ofMillis(50)).answer(request()))
                .isInstanceOfSatisfying(AiException.class, e -> assertThat(e.code()).isEqualTo("AI_TIMEOUT"));
    }
    @Test void rejectsMalformedEmptyAndIncompleteResponse() {
        for (String invalid : List.of("not-json", "{\"status\":\"completed\",\"output\":[]}",
                "{\"status\":\"incomplete\",\"output\":[]}")) {
            reply = invalid;
            assertThatThrownBy(() -> gateway(Duration.ofSeconds(2)).answer(request())).isInstanceOf(AiException.class);
        }
    }
    @Test void refusalIsExplicit() {
        reply = "{\"status\":\"completed\",\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"refusal\",\"refusal\":\"No\"}]}]}";
        assertThatThrownBy(() -> gateway(Duration.ofSeconds(2)).answer(request()))
                .isInstanceOfSatisfying(AiException.class, e -> assertThat(e.code()).isEqualTo("AI_REFUSAL"));
    }
}
