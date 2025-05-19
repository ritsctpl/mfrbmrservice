package com.rits.mfrscreenconfigurationservice.model;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "R_MFR_SCREEN_CONFIGURATION")
public class MFRScreenConfiguration {
    private String site;
    @Id
    private String handle;
    private String productName;
    private String description;
    private Product product;
    private String configType;
    private String defaultMfr;
    private String version;
    private List<MFRRefList> mrfRefList;

    private String createdBy;
    private String modifiedBy;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}

