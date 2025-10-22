package com.wrapper.apibridge.service;

import com.wrapper.apibridge.dto.PersianDate;
import com.wrapper.apibridge.dto.PodiumRequest;
import com.wrapper.apibridge.dto.PodiumResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IdentityService {
    private final PodiumAPIClient podiumAPIClient;
    private final String productId;
    private final String apiKey;

    public IdentityService(
            PodiumAPIClient podiumAPIClient,
            @Value("${app.podium.services.identity.productId}") String productId,
            @Value("${app.podium.services.identity.apiKey}") String apiKey
    ) {
        this.podiumAPIClient = podiumAPIClient;
        this.productId = productId;
        this.apiKey = apiKey;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    private static class IdentityRequest extends PodiumRequest {
        private String nationalCode;
        private String birthDate;
    }

    @Data
    public static class IdentityResponse {
        private String nationalCode;
        private String birthDate;
        private Info identityInfo;

        @Data
        public static class Info {
            private String nationalCode;              // کدملی ۱۰ رقمی
            private String firstName;                  // نام
            private String lastName;                   // نام خانوادگی
            private String fatherName;                 // نام پدر
            private String birthDate;                  // تاریخ تولد
            private String alive;                      // وضعیت حیات
            private String gender;                     // MALE | UNKNOWN
            private String identificationNumber;       // شماره شناسنامه
            private String identificationSerialCode;   // کد سریال شناسنامه
            private String identificationSerialNumber; // شماره سریال شناسنامه
            private String providerTrackerId;          // کد رهگیری سرویس دهنده
            private String birthPlaceCode;             // کد محل تولد
            private String birthPlace;                 // محل تولد
        }
    }

    public PodiumResponse<IdentityResponse> check(String nationalCode, PersianDate birthDate) {
        IdentityRequest request = new IdentityRequest(nationalCode, birthDate.toYYYYmmddFormat());
        request.setScProductId(productId);
        request.setScApiKey(apiKey);

        return podiumAPIClient.callService(request);
    }
}
