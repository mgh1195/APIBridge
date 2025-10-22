package com.wrapper.apibridge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrapper.apibridge.configuration.PodiumAPIConfiguration;
import com.wrapper.apibridge.dto.PodiumRequest;
import com.wrapper.apibridge.dto.PodiumResponse;
import com.wrapper.apibridge.exception.PodiumAPIException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PodiumAPIClient {
    private final PodiumAPIConfiguration configuration;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PodiumAPIClient(PodiumAPIConfiguration configuration, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> PodiumResponse<T> callService(PodiumRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("_token_", configuration.getToken());
            headers.set("_token_issuer_", configuration.getTokenIssuer());

            MultiValueMap<String, String> formData = convertToFormData(request);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    configuration.getUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            PodiumResponse<T> responseEntity = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });

            if (responseEntity.getHasError()) {
                throw new PodiumAPIException(
                        "API call returned error. errorCode: " + responseEntity.getErrorCode() +
                                " message: " + responseEntity.getMessage()
                );
            }

            return responseEntity;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new PodiumAPIException(
                    "API call failed with status: " + e.getStatusCode() +
                            ", body: " + e.getResponseBodyAsString(), e
            );
        } catch (Exception e) {
            throw new PodiumAPIException("Failed to call Podium API: " + e.getMessage(), e);
        }
    }

    private MultiValueMap<String, String> convertToFormData(PodiumRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        Map<String, Object> requestMap = objectMapper.convertValue(request,
                new TypeReference<>() {
                });

        requestMap.forEach((key, value) -> {
            if (value != null) {
                formData.add(key, String.valueOf(value));
            }
        });

        return formData;
    }

}
