package com.rits.lineclearanceservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineClearanceResponse {
    private String templateName;
    private String description;
}






