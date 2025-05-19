package com.rits.lineclearanceservice.dto;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RetrieveLineClearanceLogRequest {
    private String site;
    private String resourceId;
    private String workCenterId;
}
