package com.krishihub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RatingRequest {
    @Min(1) @Max(5) private int rating;
    private String reviewText;
}
