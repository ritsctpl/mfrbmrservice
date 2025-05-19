package com.rits.downtimeservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DowntimeRequestForShift {

    private String site;
    private String shiftId;
    private List<String> shiftIds;
    private LocalDateTime date;
    private LocalDateTime downtimeStart;
    private LocalDateTime downtimeEnd;
}
