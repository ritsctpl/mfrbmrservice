package com.rits.mfrservice.model;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class HeaderDetails {
    private String nameOfTheProduct;
    private String mfrNo;
    private String version;
    private String superseds;
    private String batchSize;
    private String shelfLife;
    private String mfgLicNo;
    private String effectiveDate;
}
