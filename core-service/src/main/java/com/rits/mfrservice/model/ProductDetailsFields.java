package com.rits.mfrservice.model;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductDetailsFields {
    private String productName;
    private String dosageForm;
    private String activeContent;
    private String primaryPack;
    private String unitPack;
    private String additionalBrandName;
    private String nameAndAddressOfTheCompany;
}
