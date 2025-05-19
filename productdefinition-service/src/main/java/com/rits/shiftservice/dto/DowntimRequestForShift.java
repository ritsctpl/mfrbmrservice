package com.rits.shiftservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DowntimRequestForShift {

    private String site;
    private String shiftId;
    private List<String> shiftIds;
    private LocalDateTime date;
    private LocalDateTime downtimeStart;
    private LocalDateTime downtimeEnd;
}
