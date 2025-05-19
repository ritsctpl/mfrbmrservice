package com.rits.mfrscreenconfigurationservice.dto;
import com.rits.mfrscreenconfigurationservice.model.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
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
    private List<MFRRefList> mrfRefList;

    private String createdBy;
    private String modifiedBy;

}
