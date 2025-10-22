package com.wrapper.apibridge.service;

import com.wrapper.apibridge.dto.PodiumRequest;
import com.wrapper.apibridge.dto.PodiumResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShahkarService {
    private final PodiumAPIClient podiumAPIClient;
    private final String productId;
    private final String apiKey;

    public ShahkarService(
            PodiumAPIClient podiumAPIClient,
            @Value("${app.podium.services.shahkar.productId}") String productId,
            @Value("${app.podium.services.shahkar.apiKey}") String apiKey
    ) {
        this.podiumAPIClient = podiumAPIClient;
        this.productId = productId;
        this.apiKey = apiKey;
    }

    @Data
    public static class ShahkarResponse {
        private Boolean matched;
        private String message;
        private Integer resultCode;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    private static class ShahkarRequest extends PodiumRequest {
        private Body body;

        @Data
        @AllArgsConstructor
        private static class Body {
            private String nationalCode;
            private String mobileNumber;
        }
    }

    public PodiumResponse<ShahkarResponse> check(String nationalCode, String mobileNumber) {
        ShahkarRequest request = new ShahkarRequest(new ShahkarRequest.Body(nationalCode, mobileNumber));
        request.setScProductId(productId);
        request.setScApiKey(apiKey);
        return podiumAPIClient.callService(request);
    }
}
