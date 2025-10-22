package com.wrapper.apibridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PodiumRequest {
    private String scProductId;
    private String scApiKey;
}
