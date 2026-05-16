package com.krishihub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class SendOtpRequest {
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String phone;
}
