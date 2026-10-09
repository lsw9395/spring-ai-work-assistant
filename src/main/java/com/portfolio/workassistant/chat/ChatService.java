package com.portfolio.workassistant.chat;

import com.portfolio.workassistant.ai.AiGateway;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final AiGateway gateway;
    private final ChatHistoryRepository repository;

    public ChatResponse chat(ChatRequest request) {
        String answer = gateway.answer(request);
        UUID id = UUID.randomUUID();
        repository.save(new ChatTurn(id, request.conversationId(), request.task(),
                request.message().trim(), answer, Instant.now()));
        return new ChatResponse(request.conversationId(), id, answer, gateway.mode());
    }

    public String mode() { return gateway.mode(); }
}
