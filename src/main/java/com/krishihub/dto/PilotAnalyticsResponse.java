package com.krishihub.dto;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PilotAnalyticsResponse {
    private List<Object> dailyEarnings;
    private List<Object> locationBreakdown;
    private Double avgEarningPerAcre;
}
