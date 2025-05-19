package com.rits.mfrscreenconfigurationservice.dto;
import com.rits.dataFieldService.dto.DataFieldResponse;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class ProductResponseList {
    private List<ProductResponse> productList;

}
