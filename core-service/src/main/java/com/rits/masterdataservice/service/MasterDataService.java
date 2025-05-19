package com.rits.masterdataservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.userservice.dto.UserRequest;
import com.rits.userservice.dto.UserResponse;
import com.rits.userservice.model.UserMessageModel;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class MasterDataService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder; // Inject WebClient.Builder
    @Value("${user-service.url}/create")
    private String createUserUrl;
    /**
     * Initialize master data for all collections.
     */
    public void initializeMasterData() {
        String masterDataPath = "classpath:/masterData/*.json";

        try {
            Resource[] resources = resourcePatternResolver.getResources(masterDataPath);

            if (resources.length == 0) {
                System.out.println("No JSON files found in the master data folder. Skipping initialization.");
                return;
            }

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    String fileName = resource.getFilename();
                    if (fileName != null && fileName.endsWith(".json")) {
                        String collectionName = fileName.replace(".json", "");

                        // Call WebClient if the file is "r_user.json" (ignore case)
                      /*  if ("r_user".equalsIgnoreCase(collectionName)) {
                            callCreateUserAPI(resource);
                        }*/

                        if (mongoTemplate.collectionExists(collectionName)) {
                            long count = mongoTemplate.count(new Query(), collectionName);
                            if (count > 0) {
                                System.out.println("Master data for collection " + collectionName +
                                        " already exists. Skipping initialization.");
                                continue;
                            }
                        }
                        handleMasterDataFile(collectionName, resource);
                    }
                } else {
                    System.out.println("Skipping unreadable resource: " + resource.getFilename());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error initializing master data: " + e.getMessage(), e);
        }
    }

    /**
     * Calls the create user API when r_user.json is processed.
     */
    private void callCreateUserAPI(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Read the JSON file into a list of UserRequest objects
            List<UserRequest> userRequests = objectMapper.readValue(
                    inputStream, new TypeReference<List<UserRequest>>() {}
            );

            for (UserRequest userRequest : userRequests) {
                String userId = userRequest.getUserId(); // Assuming UserRequest has getUserId()

                // Check if user already exists in MongoDB
                Query query = new Query(Criteria.where("user").is(userId));
                boolean userExists = mongoTemplate.exists(query, "r_user"); // Assuming collection is "r_user"

                if (userExists) {
                    System.out.println("User with ID " + userId + " already exists. Skipping...");
                    continue; // Skip creating user if already exists
                }

                // User doesn't exist, proceed with API call
                UserMessageModel response = webClientBuilder.build()
                        .post()
                        .uri(createUserUrl)
                        .bodyValue(userRequest)
                        .retrieve()
                        .bodyToMono(UserMessageModel.class)
                        .block();

                System.out.println("User created: " + response);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error processing r_user.json: " + e.getMessage(), e);
        }
    }


    /**
     * Refresh master data for a specific collection.
     */
    public void refreshMasterDataForCollection(String collectionName) {
        String filePath = "classpath:/masterData/" + collectionName + ".json";

        try {
            Resource resource = resourceLoader.getResource(filePath);
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Resource not found or unreadable: " + filePath);
            }
            handleMasterDataFile(collectionName, resource);
        } catch (Exception e) {
            throw new RuntimeException("Error refreshing master data for collection: " + collectionName, e);
        }
    }

    /**
     * Handle master data for a specific collection.
     */
    private void handleMasterDataFile(String collectionName, Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            List<Map<String, Object>> data = objectMapper.readValue(
                    inputStream, new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> record : data) {
                if (record.containsKey("_id")) {
                    Object idValue = record.get("_id");
                    if (idValue instanceof Map) {
                        Map<?, ?> idMap = (Map<?, ?>) idValue;
                        if (idMap.containsKey("$oid")) {
                            idValue = new ObjectId(idMap.get("$oid").toString());
                        }
                    }
                    Query query = new Query(Criteria.where("_id").is(idValue));
                    record.remove("_id");
                    Update update = new Update();
                    for (Map.Entry<String, Object> entry : record.entrySet()) {
                        update.set(entry.getKey(), entry.getValue());
                    }
                    mongoTemplate.upsert(query, update, collectionName);
                } else {
                    mongoTemplate.insert(record, collectionName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing resource: " + resource.getFilename(), e);
        }
    }
}
