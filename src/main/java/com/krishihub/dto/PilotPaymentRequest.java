package com.krishihub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
@Data
public class PilotPaymentRequest {
    @NotBlank private String paymentMode;
    private String chequeNumber;
    private LocalDate chequeDate;
    private String bankName;
    private String photoUrl;
}
