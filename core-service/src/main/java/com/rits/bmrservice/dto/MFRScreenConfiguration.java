package com.rits.bmrservice.dto;
import com.rits.mfrscreenconfigurationservice.model.MFRRefList;
import com.rits.mfrscreenconfigurationservice.model.Product;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MFRScreenConfiguration {
    private String site;
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

