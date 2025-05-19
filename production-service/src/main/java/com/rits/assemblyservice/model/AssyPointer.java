package com.rits.assemblyservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_ASSY_POINTER")
public class AssyPointer {

    @Id
    private String id;
    private String mainparent;
    private String immediateparent;
    private String currentpcu;
    private String sequence;


}