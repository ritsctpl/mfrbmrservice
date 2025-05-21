package com.rits.componentbuilderservice.dto;

import com.rits.componentbuilderservice.model.TableConfig;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComponentRequest {
    private String site;
    private String componentLabel;
    private String dataType;
    private String unit;
    private String defaultValue;
    private String apiUrl;
    private String minValue;
    private String maxValue;
    private Boolean required;
    private TableConfig tableConfig;
    private String validation;
    private String userId;
}
