package com.wrapper.apibridge.service;

import com.wrapper.apibridge.exception.NationalIDBirthDayDoesNotMatchException;
import com.wrapper.apibridge.service.dto.FinnotechResponse;
import com.wrapper.apibridge.service.dto.IranianDate;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class IdentityInquiryService {
    private final RestClient finnotechRestClient;
    private final FinnotechTokenService finnotechTokenService;
    private final String clientId;

    public IdentityInquiryService(
            RestClient finnotechRestClient,
            FinnotechTokenService finnotechTokenService,
            @Value("${app.finnotech.client-id}")
            String clientId
    ) {
        this.finnotechRestClient = finnotechRestClient;
        this.finnotechTokenService = finnotechTokenService;
        this.clientId = clientId;
    }

    @Data
    public static class IdentityResponse {
        private String firstName;
        private String lastName;
        private String birthDate;
        private String fatherName;
        private String deathStatus;
        private String nationalId;
        private String gender;
        private Integer identityNo;
        private String identitySeri;
        private String identitySerial;
        private String officeName;
        private String officeCode;
        private String trackingCode;
        private String operationStatus;
    }

    public IdentityResponse inquiry(String nationalId, IranianDate birthDate) {
        return inquiry(nationalId, birthDate, null);
    }

    public IdentityResponse inquiry(String nationalId, IranianDate birthDate, String trackId) {

        assert nationalId != null;
        assert birthDate != null;

        StringBuilder requestUri = new StringBuilder(String.format(
                "/kyc/v2/clients/%s/identificationInquiry?nationalCode=%s&birthDate=%s",
                clientId,
                nationalId,
                birthDate
        ));

        if (trackId != null) {
            requestUri.append("&trackId=").append(trackId);
        }

        FinnotechResponse<IdentityResponse> response = finnotechRestClient.get()
                .uri(requestUri.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + finnotechTokenService.getToken())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert response != null;
        assert response.getResult() != null;

        if (response.getResult().nationalId == null) {
            throw new NationalIDBirthDayDoesNotMatchException();
        }

        return response.getResult();
    }
}
