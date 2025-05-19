package com.rits.ncservice.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "R_LISTMAINTENANCE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class DataFields {
    private String dataField;

    private String value;


}
