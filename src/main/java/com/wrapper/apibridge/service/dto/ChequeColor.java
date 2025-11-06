package com.wrapper.apibridge.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ChequeColor {
    WHITE(1),
    YELLOW(2),
    ORANGE(3),
    BROWN(4),
    RED(5);

    private final int value;

    ChequeColor(int value) {
        this.value = value;
    }

    @JsonCreator
    public static ChequeColor fromString(String value) {
        for (ChequeColor chequeColor : ChequeColor.values()) {
            if (chequeColor.value == Integer.parseInt(value)) {
                return chequeColor;
            }
        }

        return null;
    }
}