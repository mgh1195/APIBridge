package com.wrapper.apibridge.service;

import com.wrapper.apibridge.dto.PodiumRequest;
import com.wrapper.apibridge.dto.PodiumResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PodiumIBANCheckService {
    private final String productId;
    private final String apiKey;
    private final PodiumAPIClient podiumAPIClient;

    public PodiumIBANCheckService(
            @Value("${app.podium.services.iban.productId}") String productId,
            @Value("${app.podium.services.iban.apiKey}") String apiKey,
            PodiumAPIClient podiumAPIClient
    ) {
        this.productId = productId;
        this.apiKey = apiKey;
        this.podiumAPIClient = podiumAPIClient;
    }

    @Data
    public static class IBANCheckResponse {
        private Boolean matched;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class IBANCheckRequest extends PodiumRequest {
        private String iban;
        private String legalNationalCode;
    }

    public PodiumResponse<IBANCheckResponse> check(String iban, String legalNationalCode) {
        IBANCheckRequest request = new IBANCheckRequest(iban, legalNationalCode);
        request.setScProductId(productId);
        request.setScApiKey(apiKey);

        return podiumAPIClient.callService(request);
    }
}
