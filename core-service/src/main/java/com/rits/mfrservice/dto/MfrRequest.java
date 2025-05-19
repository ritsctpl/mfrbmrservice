package com.rits.mfrservice.dto;

import com.rits.mfrservice.model.*;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MfrRequest {

    private String site;
    private  String mfrNo;
    private  String version;
    private  String productName;
    private String modifiedBy;
    private String createdBy;

    private HeaderDetails headerDetails;
    private List<FooterDetails> footerDetails;
    private Sections sections;


}
