package com.wrapper.apibridge.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IranianDate {
    private int year;
    private int month;
    private int day;

    @Override
    public String toString() {
        return String.format("%04d/%02d/%02d", year, month, day);
    }
}
