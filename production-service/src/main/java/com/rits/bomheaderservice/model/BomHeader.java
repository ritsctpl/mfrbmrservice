package com.rits.bomheaderservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "BOM_HEADER")
public class BomHeader {
    @Id
    private String handle;
    private String site;
    private String pcuBomBO;
    private String pcuBO;
    private List<Bom> bomList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
