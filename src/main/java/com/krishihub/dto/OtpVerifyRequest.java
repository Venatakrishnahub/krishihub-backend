package com.krishihub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class OtpVerifyRequest {
    @NotBlank @Size(min = 6, max = 6) private String otp;
}
