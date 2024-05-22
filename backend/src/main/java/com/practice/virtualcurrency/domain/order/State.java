package com.practice.virtualcurrency.domain.order;

public enum State {
    BUY("매수"),
    SELL("매도");

    private final String description;

    State(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
