package com.rits.barcodeservice.dto;

import com.rits.barcodeservice.model.ListDetails;
import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BarcodeResponse {
   private List<ListDetails> codeList;

}
