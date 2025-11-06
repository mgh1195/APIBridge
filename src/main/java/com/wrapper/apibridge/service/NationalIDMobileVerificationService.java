package com.wrapper.apibridge.service;

import com.wrapper.apibridge.service.dto.FinnotechResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NationalIDMobileVerificationService {

    private final FinnotechTokenService finnotechTokenService;
    private final RestClient restClient;
    private final String clientId;

    public NationalIDMobileVerificationService(
            FinnotechTokenService finnotechTokenService,
            RestClient finnotechRestClient,
            @Value("${app.finnotech.client-id}") String clientId
    ) {
        this.finnotechTokenService = finnotechTokenService;
        this.restClient = finnotechRestClient;
        this.clientId = clientId;
    }

    public boolean verify(String nationalCode, String mobile) {
        return verify(nationalCode, mobile, null);
    }

    public boolean verify(String nationalCode, String mobile, String trackId) {
        assert nationalCode != null;
        assert mobile != null;

        StringBuilder requestUri = new StringBuilder(String.format(
                "/facility/v2/clients/%s/shahkar/verify?nationalCode=%s&mobile=%s",
                clientId,
                nationalCode,
                mobile
        ));

        if (trackId != null) {
            requestUri.append("&trackId=").append(trackId);
        }

        FinnotechResponse<ShahkarVerificationResponse> response = restClient.get()
                .uri(requestUri.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + finnotechTokenService.getToken())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert response != null;
        assert response.getResult() != null;

        return response.getResult().getIsValid();
    }

    @Data
    private static class ShahkarVerificationResponse {
        private Boolean isValid;
    }
}
