package com.wrapper.apibridge.service.dto;

import lombok.Data;

@Data
public class FinnotechResponse<T> {
    private T result;
    private String status;
    private FinnotechError error;
    private String trackId;
    private String responseCode;
}
