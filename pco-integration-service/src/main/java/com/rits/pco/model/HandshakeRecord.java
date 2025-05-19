package com.rits.pco.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.web.bind.annotation.RequestMapping;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping
@Document(collection = "handshake_records")
public class HandshakeRecord {
    @Id
    @Field(targetType = FieldType.OBJECT_ID) // Ensures MongoDB auto-generates it
    private String id;

    private String correlationId;
    private String pcoId;
    private String username;
    private String status;  // "registered"
}
