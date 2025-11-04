package com.wrapper.apibridge.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrapper.apibridge.exception.FinnotechClientException;
import com.wrapper.apibridge.exception.FinnotechServerException;
import com.wrapper.apibridge.service.dto.FinnotechResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class RestClientConfiguration {
    private final ObjectMapper objectMapper;

    public RestClientConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public RestClient finnotechRestClient(
            @Value("${app.finnotech.base-url}")
            String baseUrl
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    FinnotechResponse<Void> finnotechResponse = mapErrorResponse(response);
                    throw new FinnotechClientException(finnotechResponse.getError().getMessage());
                }))
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    FinnotechResponse<Void> finnotechResponse = mapErrorResponse(response);
                    throw new FinnotechServerException(finnotechResponse.getError().getMessage());
                }))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private FinnotechResponse<Void> mapErrorResponse(ClientHttpResponse response) {
        try {
            String content = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(content, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new FinnotechServerException("Cannot read finnotech response body", e);
        }
    }
}
