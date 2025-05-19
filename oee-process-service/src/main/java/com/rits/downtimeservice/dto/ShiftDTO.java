package com.rits.downtimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftDTO {
    private String shiftName;
    private LocalDateTime shiftCreatedDateTime;
    private LocalDateTime breakCreatedDateTime;
}
