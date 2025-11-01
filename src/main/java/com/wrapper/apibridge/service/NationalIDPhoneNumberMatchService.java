package com.wrapper.apibridge.service;

import com.wrapper.apibridge.service.dto.FinnotechResponse;
import com.wrapper.apibridge.service.dto.NationalIDPhoneNumberMatchSMSResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NationalIDPhoneNumberMatchService {
    private final FinnotechTokenService finnotechTokenService;
    private final RestClient restClient;
    private final String clientId;

    public NationalIDPhoneNumberMatchService(
            FinnotechTokenService finnotechTokenService,
            @Value("${app.finnotech.base-url}")
            String baseUrl,
            @Value("${app.finnotech.client-id}")
            String clientId
    ) {
        this.finnotechTokenService = finnotechTokenService;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.clientId = clientId;
    }

    @Data
    private static class ValidationSMSResponse {
        private Boolean smsSent;
    }

    public NationalIDPhoneNumberMatchSMSResponse sendValidationSMS(String phoneNumber, String nationalID) {
        String requestUri = String.format(
                "/kyc/v2/clients/%s/shahkar/smsSend?nationalCode=%s&mobile=%s",
                clientId,
                nationalID,
                phoneNumber
        );

        FinnotechResponse<ValidationSMSResponse> response = restClient.get()
                .uri(requestUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + finnotechTokenService.getToken())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert response != null;
        assert response.getResult() != null;

        return new NationalIDPhoneNumberMatchSMSResponse(response.getTrackId(), response.getResult().getSmsSent());
    }

    @Data
    private static class ValidationResponse {
        private Boolean isValid;
    }

    public Boolean validateCode(String requestTrackId, String code) {
        String requestUri = String.format(
                "/kyc/v2/clients/%s/smsShahkarVerification?inquiryTrackId=%s&otp=%s",
                clientId,
                requestTrackId,
                code
        );

        FinnotechResponse<ValidationResponse> response = restClient.get()
                .uri(requestUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + finnotechTokenService.getToken())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert response != null;
        assert response.getResult() != null;

        return response.getResult().isValid;
    }
}
