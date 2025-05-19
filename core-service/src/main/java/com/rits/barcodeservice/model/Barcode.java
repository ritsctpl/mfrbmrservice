package com.rits.barcodeservice.model;

import com.rits.barcodeservice.model.ListDetails;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "R_BARCODE")
public class Barcode {
    private String site;
    @Id
    private String handle;
    private String code;
    private List<ListDetails> codeList;
    private String createdBy;
    private String modifiedBy;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
