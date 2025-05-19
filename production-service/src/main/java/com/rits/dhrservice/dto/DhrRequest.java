package com.rits.dhrservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DhrRequest {
    private String site;
    private String pcu;
}
