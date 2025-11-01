package com.wrapper.apibridge.service.dto;

import lombok.Data;

@Data
public class FinnotechResponse<T> {
    private T result;
    private String status;
    private String error;
}
