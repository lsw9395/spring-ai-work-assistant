package com.portfolio.workassistant.ai;

import com.portfolio.workassistant.chat.ChatRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Semaphore;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class OpenAiGateway implements AiGateway {
    private final HttpClient client;
    private final URI endpoint;
    private final String key;
    private final String model;
    private final Duration timeout;
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final Semaphore slots = new Semaphore(4);

    public OpenAiGateway(HttpClient client, URI endpoint, String key, String model, Duration timeout) {
        this.client = client;
        this.endpoint = endpoint;
        this.key = key;
        this.model = model;
        this.timeout = timeout;
    }

    @Override public String mode() { return "openai"; }

    @Override public String answer(ChatRequest request) {
        if (!slots.tryAcquire()) throw new AiException(HttpStatus.TOO_MANY_REQUESTS,
                "BUSY", "처리 중인 요청이 많습니다. 잠시 후 다시 시도해 주세요.");
        try {
            List<Map<String, String>> input = new ArrayList<>();
            request.history().forEach(m -> input.add(Map.of("role", m.role(), "content", m.content())));
            input.add(Map.of("role", "user", "content", request.message().trim()));
            String task = switch (request.task()) {
                case MEETING -> "회의 내용을 핵심 논의, 결정 사항, 담당자별 후속 업무로 정리하세요.";
                case EMAIL -> "제목과 본문을 갖춘 정중한 업무 메일 초안을 작성하세요.";
                case CHECKLIST -> "실행 가능한 업무 체크리스트를 작성하세요.";
                case GENERAL -> "요청한 업무를 구체적이고 간결하게 도와주세요.";
            };
            Map<String, Object> body = Map.of("model", model, "store", false,
                    "max_output_tokens", 1000, "input", input, "instructions",
                    "당신은 한국어 업무 보조 도구입니다. " + task
                    + " 제공되지 않은 사실, 담당자, 기한은 지어내지 말고 [확인 필요]로 표시하세요."
                    + " 결과는 검토용 초안이며 실제 작업을 수행했다고 주장하지 마세요. 일반 텍스트로 답하세요.");
            HttpRequest httpRequest = HttpRequest.newBuilder(endpoint).timeout(timeout)
                    .header("Authorization", "Bearer " + key).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) throw new AiException(HttpStatus.TOO_MANY_REQUESTS,
                    "AI_RATE_LIMIT", "AI 사용 한도에 도달했습니다. 사용량을 확인하거나 잠시 후 다시 시도해 주세요.");
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new AiException(HttpStatus.BAD_GATEWAY, "AI_AUTH", "AI 연결 설정을 확인해 주세요.");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) throw upstream();
            JsonNode root = mapper.readTree(response.body());
            if (!"completed".equals(root.path("status").asText())) {
                throw new AiException(HttpStatus.BAD_GATEWAY, "AI_INCOMPLETE",
                        "AI 응답이 완성되지 않았습니다. 요청을 더 짧게 나누어 다시 시도해 주세요.");
            }
            StringJoiner answer = new StringJoiner("\n");
            for (JsonNode output : root.path("output")) {
                if (!"message".equals(output.path("type").asText())) continue;
                for (JsonNode content : output.path("content")) {
                    if ("refusal".equals(content.path("type").asText())) {
                        throw new AiException(HttpStatus.UNPROCESSABLE_ENTITY, "AI_REFUSAL",
                                "AI가 이 요청에 응답할 수 없습니다. 요청 내용을 바꾸어 주세요.");
                    }
                    if ("output_text".equals(content.path("type").asText())) answer.add(content.path("text").asText());
                }
            }
            String result = answer.toString();
            if (result.isBlank() || result.length() > 6000) throw upstream();
            return result;
        } catch (HttpTimeoutException e) {
            throw new AiException(HttpStatus.GATEWAY_TIMEOUT, "AI_TIMEOUT",
                    "AI 응답 대기 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_INTERRUPTED", "요청 처리가 중단되었습니다.");
        } catch (IOException | tools.jackson.core.JacksonException e) {
            // Never return or log provider bodies, authorization headers, or user content.
            throw upstream();
        } finally {
            slots.release();
        }
    }

    private AiException upstream() {
        return new AiException(HttpStatus.BAD_GATEWAY, "AI_UNAVAILABLE",
                "AI 서비스에 연결할 수 없거나 응답을 읽을 수 없습니다. 잠시 후 다시 시도해 주세요.");
    }
}
