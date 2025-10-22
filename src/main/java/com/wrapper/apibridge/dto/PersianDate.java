package com.wrapper.apibridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersianDate {
    private int year;
    private int month;
    private int day;

    public String toYYYYmmddFormat() {
        return String.format("%04d%02d%02d", year, month, day);
    }
}
