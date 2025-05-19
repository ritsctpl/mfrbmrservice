package com.rits.dhrservice.dto;

import com.rits.nonconformanceservice.dto.DataField;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NcRequest {
    private String site;
    private String pcuBO;
}
