package com.rits.componentbuilderservice.model;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_COMPONENT")
public class Component {
    private String site;
    @Id
    private String handle;
    private String componentLabel;
    private String dataType;
    private String unit;
    private String defaultValue;
    private Boolean required;
    private String validation;
    private TableConfig tableConfig;
    private Integer active;
    private String userId;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
