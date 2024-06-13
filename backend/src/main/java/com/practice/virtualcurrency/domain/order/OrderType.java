package com.practice.virtualcurrency.domain.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderType {
    LIMIT("limit"),
    MARKET("market");

    private final String value;

    OrderType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OrderType fromValue(String value) {
        for (OrderType orderType : values()) {
            if (orderType.value.equalsIgnoreCase(value)) {
                return orderType;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
