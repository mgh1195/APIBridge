package com.wrapper.apibridge.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NationalIDPhoneNumberMatchSMSResponse {
    private String trackId;
    private Boolean smsSent;
}
