package com.rits.scrapservice.dto;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BomHeaderRequest {
    private String site;
    private int qtyInQueue;
    private String pcuBomBO;
    private String pcuBO;
    private String userId;
}
