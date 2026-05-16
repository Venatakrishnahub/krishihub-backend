package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String paymentMode;
    private Double amount;
    private String status;
    private String bookingNumber;
    private String paidAt;
}
