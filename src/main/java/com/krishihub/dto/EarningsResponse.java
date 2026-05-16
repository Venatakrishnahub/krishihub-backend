package com.krishihub.dto;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EarningsResponse {
    private Double totalEarnings;
    private Double totalAcres;
    private Integer totalJobs;
    private List<DailyReportResponse> breakdown;
}
