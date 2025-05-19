package com.rits.integration.dto;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecResponse {
    private String specName;
    private String description;
    private String type;

}
