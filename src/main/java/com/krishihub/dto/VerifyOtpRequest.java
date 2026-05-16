package com.krishihub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class VerifyOtpRequest {
    @NotBlank private String phone;
    @NotBlank @Size(min = 6, max = 6) private String otp;
    private String fcmToken;
    private String deviceType;
}
