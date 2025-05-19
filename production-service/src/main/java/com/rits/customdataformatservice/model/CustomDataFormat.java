package com.rits.customdataformatservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_CUSTOMDATAFORMAT")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomDataFormat {

    private String site;
    @Id
    private String handle;
    private String code;
    private String description;
    private String character;
    private String dataField;
    private String leadingCharacter;
    private int fixedLength;
    private int active;
    private int sequence;
    private LocalDateTime createdDateTime;

}
