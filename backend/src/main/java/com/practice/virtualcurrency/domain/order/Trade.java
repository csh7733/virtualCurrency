package com.practice.virtualcurrency.domain.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Trade {
    QUANTITY("quantity"),
    PRICE("price");

    private final String value;

    Trade(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Trade fromValue(String value) {
        for (Trade trade : values()) {
            if (trade.value.equalsIgnoreCase(value)) {
                return trade;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
