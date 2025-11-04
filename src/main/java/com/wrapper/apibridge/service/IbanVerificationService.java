package com.wrapper.apibridge.service;


import com.wrapper.apibridge.service.dto.FinnotechResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class IbanVerificationService {
    private final FinnotechTokenService finnotechTokenService;
    private final RestClient restClient;
    private final String clientId;

    public IbanVerificationService(
            FinnotechTokenService finnotechTokenService,
            RestClient finnotechRestClient,
            @Value("${app.finnotech.client-id}")
            String clientId
    ) {
        this.finnotechTokenService = finnotechTokenService;
        this.restClient = finnotechRestClient;
        this.clientId = clientId;
    }

    public boolean verify(String nationalId, String iban) {
        return verify(nationalId, iban, null);
    }

    public boolean verify(String nationalId, String iban, String trackId) {
        assert nationalId != null;
        assert iban != null;

        StringBuilder requestUri = new StringBuilder(String.format(
                "/kyc/v2/clients/%s/ibanOwnerVerification?nid=%s&iban=%s",
                clientId,
                nationalId,
                iban
        ));

        if (trackId != null) {
            requestUri.append("&trackId=").append(trackId);
        }

        FinnotechResponse<IbanOwnerVerificationResponse> response = restClient.get()
                .uri(requestUri.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + finnotechTokenService.getToken())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert response != null;
        assert response.getResult() != null;

        return "yes".equalsIgnoreCase(response.getResult().getIsValid());
    }

    @Data
    private static class IbanOwnerVerificationResponse {
        private String isValid;
    }
}
