package com.portfolio.workassistant.chat;

import java.time.Instant;
import java.util.UUID;

public record ChatTurn(UUID id, UUID conversationId, ChatRequest.TaskType task,
                       String question, String answer, Instant createdAt) {}
