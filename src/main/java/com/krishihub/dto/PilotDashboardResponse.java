package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PilotDashboardResponse {
    private Double todayAcres;
    private Double todayEarnings;
    private Integer todayJobs;
    private Double monthAcres;
    private Double monthEarnings;
    private Integer monthJobs;
    private Double totalAcres;
    private Double totalEarnings;
    private Integer pendingJobs;
}
