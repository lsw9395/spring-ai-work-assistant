package com.portfolio.workassistant.chat;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService service;

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("mode", service.mode(), "maxMessageLength", 2000, "historyLimit", 10);
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return service.chat(request);
    }
}
