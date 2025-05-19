package com.rits.bmrservice.dto;

import com.rits.mfrscreenconfigurationservice.model.Product;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MfrScreenConfigurationRequest {
    private String site;
    private String productName;
    private String description;
    private Product product;
    private String configType;
    private String defaultMfr;
    private String version;


    private String createdBy;
    private String modifiedBy;

}
