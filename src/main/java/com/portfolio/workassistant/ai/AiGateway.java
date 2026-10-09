package com.portfolio.workassistant.ai;

import com.portfolio.workassistant.chat.ChatRequest;

public interface AiGateway {
    String answer(ChatRequest request);
    String mode();
}
