package com.rits.uomservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.CollectionTable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Document("R_UOM_CONVERTION")
public class UomConvertionEntity {

    @Id
    private String handle;
    private String site;
    private String baseUnit;
    private String convertionItem;
    private String baseAmt;
    private String conversionUnit;
    private String material;
    private String materialVersion;
//    private String countValue;
    private int active;

    private String cretedBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}



