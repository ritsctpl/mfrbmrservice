package com.rits.mfrservice.model;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductDetails {
    private String title;
    private ProductDetailsFields data;
    private String tableId;
}
