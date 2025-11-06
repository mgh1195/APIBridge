package com.wrapper.apibridge.service.dto;

import lombok.Data;

@Data
public class CompanyInfo {
    private String companyId;
    private String registrationNo;
    private String title;
    private String registrationDate;
    private String capital;
    private String address;
    private String postalCode;
    private String taxNumber;
    private Double lat;
    private Double lng;
    private String website;
    private String tel;
    private String fax;
    private String email;
    private String status;
    private String edareKol;
    private String vahedSabti;
    private String lastUpdate;
    private String registrationTypeId;
    private String registrationTypeTitle;
    private String persianRegistrationDate;
    private String knowledgeBasedState;
    private String knowledgeBasedCategory;
    private String knowledgeBasedConfirmationDate;
}