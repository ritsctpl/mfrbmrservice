
package com.rits.processing;

import com.bazaarvoice.jolt.Chainr;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.model.JoltSpec;
import com.rits.integration.model.SplitMessageEntity;
import com.rits.integration.repository.JoltSpecRepository;
import com.rits.integration.util.EncodingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.EvaluateException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProcessorRegistry {

    @Autowired
    private JoltSpecRepository joltSpecRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public ProcessorRegistry(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper(); // To handle JSON conversions
    }

    // Split Pre-processing
    /*public List<Map<String, Object>> executeSplitPreProcessing(Map<String, Object> message, String preprocessJolt, String preprocessApi, String transformationType) {
        List<Map<String, Object>> splitMessages = applyJoltTransformation(message, preprocessJolt);

        if (preprocessApi != null && !preprocessApi.isEmpty()) {
            splitMessages = splitMessages.stream()
                    .map(splitMessage -> callApi(preprocessApi, splitMessage))
                    .collect(Collectors.toList());
        }

        return splitMessages;
    }

    public Map<String, Object> executePreProcessing(String identifier, Map<String, Object> message, String preprocessJolt, String preprocessApi, String transformationType) {
        // Check for JSON transformation (using JOLT) and preprocess API
        if (preprocessJolt != null && !preprocessJolt.isEmpty()) {
            message = applyJoltTransformation(message, preprocessJolt).get(0); // Assuming only one output for simple JOLT transformation
        }

        if (preprocessApi != null && !preprocessApi.isEmpty()) {
            message = callApi(preprocessApi, message);
        }

        return message;
    }

    public Map<String, Object> executePostProcessing(Map<String, Object> response, String postProcessJolt, String postProcessApi, String transformationType) {
        // Check for JSON transformation (using JOLT) and post-process API
        if (postProcessJolt != null && !postProcessJolt.isEmpty()) {
            response = applyJoltTransformation(response, postProcessJolt).get(0); // Assuming only one output for simple JOLT transformation
        }

        if (postProcessApi != null && !postProcessApi.isEmpty()) {
            response = callApi(postProcessApi, response);
        }

        return response;
    }*/

    // Split Pre-processing
    public List<Map<String, Object>> executeSplitPreProcessing(Map<String, Object> message, String preprocessSpec, String preprocessApi, String transformationType) {
        List<Map<String, Object>> splitMessages = new ArrayList<>();

        if ("JOLT".equalsIgnoreCase(transformationType)) {
            splitMessages = applyJoltTransformation(message, preprocessSpec);
        } else if ("JSONATA".equalsIgnoreCase(transformationType)) {
            splitMessages = applyJsonataTransformation(message, preprocessSpec);
        }

        if (preprocessApi != null && !preprocessApi.isEmpty()) {
            splitMessages = splitMessages.stream()
                    .map(splitMessage -> callApi(preprocessApi, splitMessage))
                    .collect(Collectors.toList());
        }

        return splitMessages;
    }

    public Map<String, Object> executePreProcessing(String identifier, Map<String, Object> message, String preprocessSpec, String preprocessApi, String transformationType) {
        // Check for JSON transformation (using JOLT or JSONata) and preprocess API
        if (preprocessSpec != null && !preprocessSpec.isEmpty()) {
            if ("JOLT".equalsIgnoreCase(transformationType)) {
                message = applyJoltTransformation(message, preprocessSpec).get(0); // Assuming only one output for simple JOLT transformation
            } else if ("JSONATA".equalsIgnoreCase(transformationType)) {
                message = applyJsonataTransformation(message, preprocessSpec).get(0);
            }
        }

        if (preprocessApi != null && !preprocessApi.isEmpty()) {
            message = callApi(preprocessApi, message);
        }

        return message;
    }

    public Map<String, Object> executePostProcessing(Map<String, Object> response, String postProcessSpec, String postProcessApi, String transformationType) {
        // Check for JSON transformation (using JOLT or JSONata) and post-process API
        if (postProcessSpec != null && !postProcessSpec.isEmpty()) {
            if ("JOLT".equalsIgnoreCase(transformationType)) {
                response = applyJoltTransformation(response, postProcessSpec).get(0); // Assuming only one output for simple JOLT transformation
            } else if ("JSONATA".equalsIgnoreCase(transformationType)) {
                response = applyJsonataTransformation(response, postProcessSpec).get(0);
            }
        }

        if (postProcessApi != null && !postProcessApi.isEmpty()) {
            response = callApi(postProcessApi, response);
        }

        return response;
    }


    // JOLT Transformation for JSON to JSON conversion
    public List<Map<String, Object>> applyJoltTransformation(Map<String, Object> inputJson, String joltSpec) {
        try {

            JoltSpec joltSpecDetail = joltSpecRepository.findBySpecName(joltSpec);
            if (joltSpec == null) {
                throw new RuntimeException("JoltSpec with specName " + joltSpec + " not found");
            }

            // Parse the JOLT specification (JOLT spec is a JSON string, so we parse it into a List<Object>)
            // If joltSpec is already a List<Object>, no need to parse it
            List<Object> spec = joltSpecDetail.getJoltSpec();


            // First, apply the JOLT spec to transform the input JSON into the "combined" structure
            //   List<Object> spec = objectMapper.readValue(joltSpec, new TypeReference<List<Object>>() {});
            Chainr chainr = Chainr.fromSpec(spec);

            // Perform the transformation on the input JSON
            Object combinedJson = chainr.transform(inputJson);

            // Convert the combined output back to a Map
            Map<String, Object> combinedMap = objectMapper.convertValue(combinedJson, new TypeReference<Map<String, Object>>() {});

            // Now, split the "combined" structure into separate messages
            List<Map<String, Object>> splitMessages = new ArrayList<>();

            // Assuming the "combined" structure has multiple parts to split
            Map<String, Object> combined = (Map<String, Object>) combinedMap.get("combined");
            if (combined != null) {
                // Loop through each entry in the "combined" map and create separate split messages
                for (Map.Entry<String, Object> entry : combined.entrySet()) {
                    // Create a new map for each split message
                    Map<String, Object> splitMessage = new HashMap<>();
                    splitMessage.put("identifier", entry.getKey());  // e.g., "processOrderProcess"
                    splitMessage.put("message", entry.getValue());    // The actual JSON content

                    // Add to the list of split messages
                    splitMessages.add(splitMessage);
                }
            } else {
                // If there is no "combined" key, return the transformed JSON itself as a single message
                splitMessages.add(combinedMap);
            }

            // Return the list of split messages
            return splitMessages;

        } catch (Exception e) {
            throw new RuntimeException("Error during JOLT transformation", e);
        }
    }

    // JSONata Transformation for JSON to JSON conversion

    public List<Map<String, Object>> applyJsonataTransformation(Map<String, Object> inputJson, String jsonataExpression) {
        try {
            // Fetch the JSONata spec from the repository
            JoltSpec jsonataSpec = joltSpecRepository.findBySpecName(jsonataExpression);
            if (jsonataSpec == null || jsonataSpec.getJsonataSpec() == null) {
                throw new RuntimeException("JSONATA Spec not found for " + jsonataExpression);
            }

            // Decode the base64 JSONata spec
            String decodedJsonataSpec = EncodingUtils.decodeFromBase64(jsonataSpec.getJsonataSpec());

            // Parse the JSONata expression
            Expressions expression = Expressions.parse(decodedJsonataSpec);

            // Convert the input JSON to a JsonNode representation
            JsonNode inputJsonNode = objectMapper.valueToTree(inputJson);

            // Evaluate the JSONata expression with the input JSON
            JsonNode resultNode = expression.evaluate(inputJsonNode);

            // Process the result
            List<Map<String, Object>> resultList;
            if (resultNode.isArray()) {
                // Convert the result to a List of Maps
                resultList = objectMapper.convertValue(resultNode, new TypeReference<List<Map<String, Object>>>() {});
            } else {
                // If the result is not an array, wrap it into a list
                Map<String, Object> resultMap = objectMapper.convertValue(resultNode, new TypeReference<Map<String, Object>>() {});
                resultList = Collections.singletonList(resultMap);
            }

           // Return the result
            return resultList;

        } catch (EvaluateException e) {
            throw new RuntimeException("Error during JSONata transformation", e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing JSON transformation", e);
        }
    }

  /*  public List<Map<String, Object>> applyJsonataTransformation(Map<String, Object> inputJson, String jsonataExpression) {
        try {

            JoltSpec jsonataSpec = joltSpecRepository.findBySpecName(jsonataExpression);
            if (jsonataSpec == null || jsonataSpec.getJsonataSpec() == null) {
                throw new RuntimeException("JSONATA Spec not found for " + jsonataExpression);
            }

            String decodedJsonataSpec = EncodingUtils.decodeFromBase64(jsonataSpec.getJsonataSpec());

            // Parse the JSONata expression
            Expressions expression = Expressions.parse(decodedJsonataSpec);

            // Convert the input JSON to a JsonNode representation
            JsonNode inputJsonNode = objectMapper.valueToTree(inputJson);

            // Evaluate the JSONata expression with the input JSON
            JsonNode resultNode = expression.evaluate(inputJsonNode);

            // Convert the result to a Map
            Map<String, Object> resultMap = objectMapper.convertValue(resultNode, new TypeReference<Map<String, Object>>() {});

            // Add the result to a list to return
            List<Map<String, Object>> resultList = new ArrayList<>();
            resultList.add(resultMap);

            return resultList;
        } catch (EvaluateException e) {
            throw new RuntimeException("Error during JSONata transformation", e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing JSON transformation", e);
        }
    }
*/

    // XML to JSON Conversion
    public String convertXmlToJson(String xml) {
        /*try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            StringWriter writer = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            String jsonString = writer.toString();
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {}).toString();
        } catch (Exception e) {
            throw new RuntimeException("Error converting XML to JSON", e);
        }*/
        /*try {
            // Use XmlMapper to parse the XML string
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(xml);

            // Convert the JsonNode to JSON string
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("Error converting XML to JSON", e);
        }*/

        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            JsonNode node = xmlMapper.readTree(xml);

            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("Error converting XML to JSON", e);
        }
    }

    // JSON to XML Conversion
    public String convertJsonToXml(String json) {
        try {
            // Create ObjectMapper to parse JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            // Parse the JSON string into a JsonNode
            JsonNode jsonNode = objectMapper.readTree(json);

            // Use XmlMapper to convert the JsonNode into an XML string
            XmlMapper xmlMapper = new XmlMapper();
            String xml = xmlMapper.writeValueAsString(jsonNode);

            return xml.trim(); // Trim whitespace for cleaner XML output
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to XML", e);
        }
        /*try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(json)));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to XML", e);
        }*/
    }

    // XSLT Transformation for XML to XML conversion
    public String applyXsltTransformation(String xsltSpecName, String inputXml) {
        try {
            JoltSpec xsltSpec = joltSpecRepository.findBySpecName(xsltSpecName);
            if (xsltSpec == null || xsltSpec.getXsltSpec() == null) {
                throw new RuntimeException("XSLT Spec not found for " + xsltSpecName);
            }

            String decodedXsltSpec = EncodingUtils.decodeFromBase64(xsltSpec.getXsltSpec());

            TransformerFactory factory = TransformerFactory.newInstance();
            StreamSource xslt = new StreamSource(new StringReader(decodedXsltSpec));
            Transformer transformer = factory.newTransformer(xslt);

            StringWriter writer = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(inputXml)), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error applying XSLT transformation", e);
        }
    }

    // Create SplitMessageEntities from JSON
    public List<SplitMessageEntity> createSplitMessageEntitiesFromJson(String messageBody, IntegrationEntity integrationEntity) {
        try {
            Map<String, Object> message;
            if (messageBody.trim().startsWith("[")) {
                message = objectMapper.readValue(messageBody, new TypeReference<List<Map<String, Object>>>() {}).get(0);
            } else {
                message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
            }

        //    Map<String, Object> message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> splitMessages = executeSplitPreProcessing(message, integrationEntity.getPreprocessJolt(), integrationEntity.getPreprocessApi(),integrationEntity.getTransformationType());
            List<SplitMessageEntity> splitMessageEntities = new ArrayList<>();

            for (Map<String, Object> splitMessage : splitMessages) {
                String splitIdentifier = (String) splitMessage.get("identifier");
                String splitUid = UUID.randomUUID().toString();
                String sequence = getSequenceForSplitIdentifier(integrationEntity, splitIdentifier);

                SplitMessageEntity splitMessageEntity = new SplitMessageEntity();
                splitMessageEntity.setId(splitUid);
                splitMessageEntity.setParentIdentifier(integrationEntity.getIdentifier());
                splitMessageEntity.setSplitIdentifier(splitIdentifier);
                splitMessageEntity.setMessageBody(objectMapper.writeValueAsString(splitMessage));
                splitMessageEntity.setSequence(sequence);
                splitMessageEntity.setProcessed(false);
                splitMessageEntity.setProcessedAsXml(false);
                splitMessageEntity.setCreatedDateTime(LocalDateTime.now());

                splitMessageEntities.add(splitMessageEntity);
            }

            return splitMessageEntities;
        } catch (Exception e) {
            throw new RuntimeException("Error creating SplitMessageEntities from JSON", e);
        }
    }

    // Helper to get sequence for split identifier
    private String getSequenceForSplitIdentifier(IntegrationEntity integrationEntity, String splitIdentifier) {
        for (IntegrationEntity.SplitDefinition splitDef : integrationEntity.getExpectedSplits()) {
            if (splitDef.getSplitIdentifier().equals(splitIdentifier)) {
                return splitDef.getSequence();
            }
        }
        return "0";
    }

    // Utility function to call APIs with a request body in Map format
    public Map<String, Object> callApi(String url, Map<String, Object> requestBody) {
        WebClient webClient = webClientBuilder.build();
        try {
            String response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error calling API at " + url, e);
        }
    }

    // Overloaded Utility function to call APIs with a request body in String format
    public String callApi(String url, String requestBody) {
        WebClient webClient = webClientBuilder.build();
        try {
            return webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error calling API at " + url, e);
        }
    }
}

/*
package com.rits.processing;

import com.bazaarvoice.jolt.Chainr;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.integration.model.JoltSpec;
import com.rits.integration.repository.JoltSpecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProcessorRegistry {

    @Autowired
    private JoltSpecRepository joltSpecRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public ProcessorRegistry(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper(); // To handle JSON conversions
    }

    // Split Pre-processing
    public List<Map<String, Object>> executeSplitPreProcessing(Map<String, Object> message, String preprocessJolt, String preprocessApi) {
        List<Map<String, Object>> splitMessages = applyJoltTransformation(message, preprocessJolt);

        if (preprocessApi != null && !preprocessApi.isEmpty()) {
            splitMessages = splitMessages.stream()
                    .map(splitMessage -> callApi(preprocessApi, splitMessage))
                    .collect(Collectors.toList());
        }

        return splitMessages;
    }

    public Map<String, Object> executePreProcessing(String identifier, Map<String, Object> message, String preprocessJolt, String preprocessApi) {
        // Check for JSON transformation (using JOLT) and preprocess API
        if (preprocessJolt != null && !preprocessJolt.isEmpty()) {
            message = applyJoltTransformation(message, preprocessJolt).get(0); // Assuming only one output for simple JOLT transformation
        }

        if (preprocessApi != null && !preprocessApi.isEmpty()) {
            message = callApi(preprocessApi, message);
        }

        return message;
    }

    public Map<String, Object> executePostProcessing(Map<String, Object> response, String postProcessJolt, String postProcessApi) {
        // Check for JSON transformation (using JOLT) and post-process API
        if (postProcessJolt != null && !postProcessJolt.isEmpty()) {
            response = applyJoltTransformation(response, postProcessJolt).get(0); // Assuming only one output for simple JOLT transformation
        }

        if (postProcessApi != null && !postProcessApi.isEmpty()) {
            response = callApi(postProcessApi, response);
        }

        return response;
    }

    // Utility function for JOLT transformation (JSON to JSON)
    */
/*private List<Map<String, Object>> applyJoltTransformation(Map<String, Object> inputJson, String joltSpec) {
        try {
            // Parse JOLT spec
            List<Object> spec = objectMapper.readValue(joltSpec, new TypeReference<List<Object>>() {});

            // Chainr to apply the transformation
            Chainr chainr = Chainr.fromSpec(spec);

            // Perform the transformation
            Object transformedOutput = chainr.transform(inputJson);

            // Convert the transformed output back to List<Map<String, Object>>
            return objectMapper.convertValue(transformedOutput, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error during JOLT transformation", e);
        }
    }*//*


    public List<Map<String, Object>> applyJoltTransformation(Map<String, Object> inputJson, String joltSpec) {
        try {

            JoltSpec joltSpecDetail = joltSpecRepository.findBySpecName(joltSpec);
            if (joltSpec == null) {
                throw new RuntimeException("JoltSpec with specName " + joltSpec + " not found");
            }

            // Parse the JOLT specification (JOLT spec is a JSON string, so we parse it into a List<Object>)
            // If joltSpec is already a List<Object>, no need to parse it
            List<Object> spec = joltSpecDetail.getJoltSpec();


            // First, apply the JOLT spec to transform the input JSON into the "combined" structure
         //   List<Object> spec = objectMapper.readValue(joltSpec, new TypeReference<List<Object>>() {});
            Chainr chainr = Chainr.fromSpec(spec);

            // Perform the transformation on the input JSON
            Object combinedJson = chainr.transform(inputJson);

            // Convert the combined output back to a Map
            Map<String, Object> combinedMap = objectMapper.convertValue(combinedJson, new TypeReference<Map<String, Object>>() {});

            // Now, split the "combined" structure into separate messages
            List<Map<String, Object>> splitMessages = new ArrayList<>();

            // Assuming the "combined" structure has multiple parts to split
            Map<String, Object> combined = (Map<String, Object>) combinedMap.get("combined");
            if (combined != null) {
                // Loop through each entry in the "combined" map and create separate split messages
                for (Map.Entry<String, Object> entry : combined.entrySet()) {
                    // Create a new map for each split message
                    Map<String, Object> splitMessage = new HashMap<>();
                    splitMessage.put("identifier", entry.getKey());  // e.g., "processOrderProcess"
                    splitMessage.put("message", entry.getValue());    // The actual JSON content

                    // Add to the list of split messages
                    splitMessages.add(splitMessage);
                }
            }

            // Return the list of split messages
            return splitMessages;

        } catch (Exception e) {
            throw new RuntimeException("Error during JOLT transformation", e);
        }
    }

    // Utility function to load JSON from a file
    */
/*public Map<String, Object> loadJsonFromFile(String filePath) {
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error loading JSON from file", e);
        }
    }*//*

    // Utility function to call APIs with a request body in Map format
    public Map<String, Object> callApi(String url, Map<String, Object> requestBody) {
        WebClient webClient = webClientBuilder.build();
        try {
            String response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error calling API at " + url, e);
        }
    }

    // Overloaded Utility function to call APIs with a request body in String format
    public String callApi(String url, String requestBody) {
        WebClient webClient = webClientBuilder.build();
        try {
            return webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error calling API at " + url, e);
        }
    }
}
*/
