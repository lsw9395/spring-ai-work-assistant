package com.portfolio.workassistant.chat;

import org.springframework.stereotype.Repository;

@Repository
public class NoOpChatHistoryRepository implements ChatHistoryRepository {
    @Override
    public void save(ChatTurn turn) {
        // MVP intentionally does not retain conversation content on the server.
    }
}
