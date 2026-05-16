package com.krishihub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
@Data
public class RegisterPilotRequest {
    @NotBlank private String fullName;
    @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$") private String phone;
    private String email;
    @NotBlank private String aadhaarNumber;
    private String village;
    private String mandal;
    private String district;
    private String dronePilotLicense;
    private LocalDate licenseExpiryDate;
    private String dgcaUinNumber;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String upiId;
    private String registrationNotes;
}
