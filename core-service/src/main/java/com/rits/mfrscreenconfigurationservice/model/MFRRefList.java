package com.rits.mfrscreenconfigurationservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MFRRefList {
    private String dataField;
    private String mfrRef;
    private String title;

}
