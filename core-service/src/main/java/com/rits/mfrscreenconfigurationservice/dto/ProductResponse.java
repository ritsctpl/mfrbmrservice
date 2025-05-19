package com.rits.mfrscreenconfigurationservice.dto;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class ProductResponse {
    private String productName;
    private String description;
}
