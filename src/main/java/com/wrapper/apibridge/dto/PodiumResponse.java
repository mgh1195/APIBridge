package com.wrapper.apibridge.dto;

import lombok.Data;

@Data
public class PodiumResponse<T> {
    private Boolean hasError;
    private Integer messageId;
    private String referenceNumber;
    private Integer errorCode;
    private Integer count;
    private Integer ott;
    private T result;
}
