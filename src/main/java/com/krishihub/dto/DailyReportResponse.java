package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DailyReportResponse {
    private String date;
    private Double acres;
    private Double earnings;
    private Integer jobs;
    private String districts;
}
