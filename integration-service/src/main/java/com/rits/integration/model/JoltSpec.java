package com.rits.integration.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "jolt_specs")
public class JoltSpec {

    @Id
    private String id;
    private String site;
    private String specName;       // Name of the JOLT spec
    private List<Object> joltSpec; // JOLT specification stored as a List (for JSON structure)
    private String description;    // Optional description of the spec
    private String xsltSpec;
    private String jsonataSpec;
    private String type; // Type of the spec, either "JOLT" or "XSLT" or JSONATA

    @CreatedDate
    @Field("created_date_time")
    private LocalDateTime createdDateTime; // Date and time when the message was created

    private LocalDateTime lastModifiedDateTime;

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

}
