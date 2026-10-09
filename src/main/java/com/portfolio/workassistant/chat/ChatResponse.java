package com.portfolio.workassistant.chat;

import java.util.UUID;

public record ChatResponse(UUID conversationId, UUID messageId, String answer, String mode) {}
