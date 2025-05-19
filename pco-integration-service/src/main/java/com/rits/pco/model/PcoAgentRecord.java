package com.rits.pco.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping
@Document(collection = "pco_agents")
public class PcoAgentRecord {
    @Id
    @Field(targetType = FieldType.OBJECT_ID) // MongoDB auto-generates this ID
    private String id;

    private String correlationId;
    private String pcoId;
    private String agentId;
    private String username;
    private String status;  // "registered"
    private boolean responseEnabled;  // ✅ Add boolean flag for response
}