package com.wrapper.apibridge.service;

import com.wrapper.apibridge.service.dto.FinnotechResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class FinnotechTokenService extends AbstractTokenService {
    private final String nid;
    private final List<String> scopes;
    private final RestClient restClient;

    public FinnotechTokenService(
            @Value("${app.finnotech.token-url}")
            String url,
            @Value("${app.finnotech.nid}")
            String nid,
            @Value("${app.finnotech.scopes}")
            List<String> scopes,
            @Value("${app.finnotech.client-id}")
            String clientId,
            @Value("${app.finnotech.client-secret}")
            String clientSecret
    ) {
        this.nid = nid;
        this.scopes = scopes;

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + calculateAuth(clientId, clientSecret))
                .build();
    }

    private String calculateAuth(String clientId, String clientSecret) {
        return Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
    }

    @PostConstruct
    public void initializeToken() {
        Map<String, Object> requestBody = Map.of(
                "nid", this.nid,
                "scopes", this.scopes,
                "grant_type", "client_credentials"
        );

        sendTokenRequestAndSetNewToken(requestBody);
    }


    @Override
    protected void refreshToken() {
        if (getRefreshToken() == null) {
            throw new RuntimeException("Cannot refresh token when no refresh token has been set");
        }

        Map<String, Object> requestBody = Map.of(
                "grant_type", "refresh_token",
                "token_type", "CLIENT-CREDENTIAL",
                "refresh_token", getRefreshToken()
        );

        sendTokenRequestAndSetNewToken(requestBody);
    }

    private void sendTokenRequestAndSetNewToken(Map<String, Object> requestBody) {
        Long creationTime = System.currentTimeMillis();

        FinnotechResponse<TokenResponse> tokenResponse = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert tokenResponse != null;
        assert tokenResponse.getResult() != null;

        setToken(tokenResponse.getResult().value);
        setExpirationTime(creationTime + tokenResponse.getResult().lifeTime);

        if (tokenResponse.getResult().refreshToken != null) {
            setRefreshToken(tokenResponse.getResult().refreshToken);
        }
    }

    @Data
    private static class TokenResponse {
        private String value;
        private List<String> scopes;
        private Long lifeTime;
        private String creationDate;
        private String refreshToken;
    }
}
