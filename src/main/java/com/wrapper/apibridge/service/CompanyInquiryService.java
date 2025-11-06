package com.wrapper.apibridge.service;

import com.wrapper.apibridge.service.dto.CompanyInfo;
import com.wrapper.apibridge.service.dto.FinnotechResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CompanyInquiryService {
    private final RestClient finnotechRestClient;
    private final FinnotechTokenService finnotechTokenService;
    private final String clientId;

    public CompanyInquiryService(
            RestClient finnotechRestClient,
            FinnotechTokenService finnotechTokenService,
            @Value("${app.finnotech.client-id}")
            String clientId
    ) {
        this.finnotechRestClient = finnotechRestClient;
        this.finnotechTokenService = finnotechTokenService;
        this.clientId = clientId;
    }


    public CompanyInfo inquiry(String companyId) {
        return inquiry(companyId, null);
    }

    public CompanyInfo inquiry(String companyId, String trackId) {

        assert companyId != null;

        StringBuilder requestUri = new StringBuilder(String.format(
                "/kyb/v2/clients/%s/companyInfo?companyId=%s",
                clientId,
                companyId
        ));

        if (trackId != null) {
            requestUri.append("&trackId=").append(trackId);
        }

        FinnotechResponse<CompanyInfo> response = finnotechRestClient.get()
                .uri(requestUri.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + finnotechTokenService.getToken())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert response != null;
        assert response.getResult() != null;

        return response.getResult();
    }
}
