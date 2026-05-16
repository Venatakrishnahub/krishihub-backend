package com.krishihub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
@Data
public class RescheduleRequest {
    @NotNull private LocalDate newDate;
    @NotBlank private String newShift;
    private String reason;
}
