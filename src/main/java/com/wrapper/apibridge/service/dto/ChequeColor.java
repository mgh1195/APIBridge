package com.wrapper.apibridge.service.dto;

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

    public static ChequeColor fromValue(int value) {
        for (ChequeColor chequeColor : ChequeColor.values()) {
            if (chequeColor.value == value) {
                return chequeColor;
            }
        }

        return null;
    }
}