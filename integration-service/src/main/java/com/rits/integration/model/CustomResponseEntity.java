package com.rits.integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
// Lombok annotation that automatically generates getters, setters, toString, equals, and hashCode methods
@AllArgsConstructor      // Generates a constructor with all fields
@NoArgsConstructor       // Generates a default constructor
@Document(collection = "response_entities")
public class CustomResponseEntity {

    @Id
    private String messageId;
    private String identifier;
    private String site;
    private String input;
    private String preprocessJoltResponse;
    private String preprocessXsltResponse;
    private String preprocessApiResponse;
    private String apiToProcessResponse;
    private String postProcessJoltResponse;
    private String postProcessApiResponse;
    private String passHandlerResponse;
    private String failHandlerResponse;
    private String status;
    private String statusMessage;
    private LocalDateTime createdDateTime;

}