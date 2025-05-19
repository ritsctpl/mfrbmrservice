package com.rits.bmrservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "R_MFR_DOSANDDONTS")
public class DosAndDonts {
    @Id
    private String id;
    private String handle;
    private Object title;
    private Object dosAndDontsData;
    private int active;
    private String site;
    private String dataField;
}
