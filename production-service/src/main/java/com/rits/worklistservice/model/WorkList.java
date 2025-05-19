package com.rits.worklistservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "R_WORKLIST")
public class WorkList {
    @Id
    private String handle;
    private String preDefinedFieldGroup;
    private String sequence;
    private String fieldName;
    private String fieldValue;
}
