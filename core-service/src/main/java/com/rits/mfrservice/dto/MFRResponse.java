package com.rits.mfrservice.dto;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class MFRResponse {
    private String mfrNo;
    private String version;
}
