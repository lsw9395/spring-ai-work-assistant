package com.portfolio.workassistant.chat;

/** Persistence port. A future JPA adapter can implement this without changing the controller. */
public interface ChatHistoryRepository {
    void save(ChatTurn turn);
}
