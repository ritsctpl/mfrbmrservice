package com.rits.bmrservice.dto;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class MFRResponse {
    private String bmrNo;
    private String mfrNo;
    private String version;
    private String site;
    private List<String> dataFieldList;
    private MfrFullResponse mfrFullResponse;
}
