package com.rits.barcodeservice.dto;

import com.rits.barcodeservice.model.ListDetails;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class BarcodeRequest {
    private String site;
    private String code;
    private List<ListDetails> codeList;
    private int active;
    private String userId;
    private String createdBy;
}
