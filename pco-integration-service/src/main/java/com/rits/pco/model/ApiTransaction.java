package com.rits.pco.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "api_transactions")
public class ApiTransaction {
    @Id
    private String id;
    private String correlationId;
    private String pcoId;
    private String agentId;
    private String apiUrl;
    private Map<String, Object> request; // ✅ Keep request as a JSON Map
    private String method;
    private String response;
}
