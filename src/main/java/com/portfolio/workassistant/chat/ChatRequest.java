package com.portfolio.workassistant.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record ChatRequest(
        @NotNull UUID conversationId,
        @NotBlank @Size(max = 2000) String message,
        @NotNull TaskType task,
        @NotNull @Size(max = 10) List<@NotNull @Valid HistoryMessage> history) {
    public enum TaskType { GENERAL, MEETING, EMAIL, CHECKLIST }
    public record HistoryMessage(
            @NotBlank @Pattern(regexp = "user|assistant") String role,
            @NotBlank @Size(max = 6000) String content) {}
}
