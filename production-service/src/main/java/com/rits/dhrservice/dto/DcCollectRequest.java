package com.rits.dhrservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DcCollectRequest {
    private String site;
    private String pcu;
}
