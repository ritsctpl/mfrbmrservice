package com.rits.oeeservice.model;

import javax.persistence.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "api_configuration")
public class ApiConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_name", unique = true, nullable = false)
    private String apiName;

    @Column(name = "stored_procedure", nullable = false)
    private String storedProcedure;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    // JSON string defining input parameters (e.g., [{"name":"startDate","type":"DATE","required":true}, ...])
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "input_parameters", columnDefinition = "TEXT")
    @Type(type = "org.hibernate.type.TextType")
    private String inputParameters;

    // Optional: JSON string defining output structure (if needed)
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "output_structure", columnDefinition = "TEXT")
    @Type(type = "org.hibernate.type.TextType")
    private String outputStructure;

    // Getters and Setters

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getApiName() {
        return apiName;
    }
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
    public String getStoredProcedure() {
        return storedProcedure;
    }
    public void setStoredProcedure(String storedProcedure) {
        this.storedProcedure = storedProcedure;
    }
    public String getHttpMethod() {
        return httpMethod;
    }
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    public String getInputParameters() {
        return inputParameters;
    }
    public void setInputParameters(String inputParameters) {
        this.inputParameters = inputParameters;
    }
    public String getOutputStructure() {
        return outputStructure;
    }
    public void setOutputStructure(String outputStructure) {
        this.outputStructure = outputStructure;
    }
}
