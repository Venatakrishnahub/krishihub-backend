package com.krishihub.dto;
import lombok.Data;
@Data
public class OnlineStatusRequest {
    private boolean online;
    private Double latitude;
    private Double longitude;
}
