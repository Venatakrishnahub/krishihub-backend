package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RazorpayOrderResponse {
    private String orderId;
    private Long amount;
    private String currency;
    private String keyId;
    private String bookingNumber;
    private String farmerName;
}
