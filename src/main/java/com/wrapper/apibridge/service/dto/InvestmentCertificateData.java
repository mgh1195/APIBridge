package com.wrapper.apibridge.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@Builder
public class InvestmentCertificateData {
    @NotNull
    private String documentNumber;
    @NotNull
    private String investorName;
    @NotNull
    private String contractNumber;
    @NotNull
    private String companyNationalId;
    private long investmentAmountRials;
    @NotNull
    private String bankAccountNumber;
    @NotNull
    private String bankName;
    @NotNull
    private String bankAccountOwnerName;
    @Singular
    private List<ChequeData> cheques;

    @Data
    @AllArgsConstructor
    public static class ChequeData {
        private long amount;
        @NotNull
        private String date;
        @NotNull
        private String number;
        @NotNull
        private String documentNumber;
    }
}
