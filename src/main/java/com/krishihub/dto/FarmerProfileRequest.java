package com.krishihub.dto;
import lombok.Data;
@Data
public class FarmerProfileRequest {
    private String fullName;
    private String aadhaarNumber;
    private String village;
    private String mandal;
    private String district;
    private String preferredLanguage;
}
