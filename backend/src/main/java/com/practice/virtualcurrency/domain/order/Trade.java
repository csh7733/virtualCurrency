package com.practice.virtualcurrency.domain.order;

public enum Trade {
    QUANTITY("수량거래"),
    PRCIE("가격거래");

    private final String description;

    Trade(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
