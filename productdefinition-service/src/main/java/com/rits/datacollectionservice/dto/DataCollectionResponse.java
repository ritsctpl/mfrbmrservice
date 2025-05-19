package com.rits.datacollectionservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollectionResponse {
    private String dataCollection;
    private String version;
    private String description;
}
