package com.wrapper.apibridge.service;

import com.wrapper.apibridge.service.dto.ChequeColor;
import com.wrapper.apibridge.service.dto.FinnotechResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ChequeColorInquiryService {
    private final FinnotechTokenService finnotechTokenService;
    private final RestClient restClient;
    private final String clientId;

    public ChequeColorInquiryService(
            FinnotechTokenService finnotechTokenService,
            @Value("${app.finnotech.client-id}")
            String clientId,
            RestClient finnotechRestClient
    ) {
        this.finnotechTokenService = finnotechTokenService;
        this.clientId = clientId;
        this.restClient = finnotechRestClient;
    }

    public ChequeColor check(String nationalId) {
        assert nationalId != null;

        String requestUri = String.format(
                "/credit/v2/clients/%s/chequeColorInquiry?idCode=%s",
                clientId,
                nationalId
        );

        FinnotechResponse<ChequeColorResponse> response = restClient.get()
                .uri(requestUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + finnotechTokenService.getToken())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert response != null;
        assert response.getResult() != null;

        return ChequeColor.fromValue(Integer.parseInt(response.getResult().chequeColor));
    }

    @Data
    private static class ChequeColorResponse {
        private String chequeColor;
    }
}
