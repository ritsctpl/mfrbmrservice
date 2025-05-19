package com.rits.mfrservice.model;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_MFR")
public class Mfr {
    private String site;
    @Id
    private String handle;
    private  String mfrNo;
    private  String version;
    private  String productName;
    private HeaderDetails headerDetails;
    private List<FooterDetails> footerDetails;
    private Sections sections;

    private String createdBy;
    private String modifiedBy;


    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;



}
