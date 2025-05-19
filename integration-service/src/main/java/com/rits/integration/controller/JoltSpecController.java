package com.rits.integration.controller;

import com.rits.integration.dto.SpecResponse;
import com.rits.integration.exception.IntegrationException;
import com.rits.integration.model.IntegrationMessageModel;
import com.rits.integration.model.JoltSpec;
import com.rits.integration.model.MessageDetails;
import com.rits.integration.repository.JoltSpecRepository;
import com.rits.integration.util.EncodingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/integration-service/jolt-spec")
public class JoltSpecController {

    @Autowired
    private JoltSpecRepository joltSpecRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

//    @PostMapping("/create")
//    public ResponseEntity<?> createJoltSpec(@RequestBody JoltSpec joltSpec) {
//        // Check if a JoltSpec with the same specName already exists
//        JoltSpec existingSpec = joltSpecRepository.findBySpecName(joltSpec.getSpecName());
//
//        if (existingSpec != null) {
//            throw new IntegrationException(7, existingSpec.getSpecName(), existingSpec.getType());
//        }
//
//        // Add created and modified timestamps before saving
//        if (joltSpec.getCreatedDateTime() == null) {
//            joltSpec.setCreatedDateTime(LocalDateTime.now());
//        }
//        joltSpec.setLastModifiedDateTime(LocalDateTime.now());
//
//
//        JoltSpec savedSpec = joltSpecRepository.save(joltSpec);
//        String createdMessage = getFormattedMessage(1, joltSpec.getSpecName());
//        return ResponseEntity.ok(IntegrationMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(savedSpec).build());
//    }

    @PostMapping("/create")
    public ResponseEntity<?> createJoltSpec(@RequestBody JoltSpec joltSpec) {
        // Check if a JoltSpec with the same specName already exists
        if(joltSpec.getSite() == null || joltSpec.getSite().isEmpty()) throw new IntegrationException(7001);
        if(joltSpec.getSpecName() == null || joltSpec.getSpecName().isEmpty()) throw new IntegrationException(10);
        JoltSpec existingSpec = joltSpecRepository.findBySpecNameAndSite(joltSpec.getSpecName(),joltSpec.getSite());

        if (existingSpec != null) {
            throw new IntegrationException(7, existingSpec.getSpecName(), existingSpec.getType());
        }

        // Add created and modified timestamps before saving
        if (joltSpec.getCreatedDateTime() == null) {
            joltSpec.setCreatedDateTime(LocalDateTime.now());
        }
        joltSpec.setLastModifiedDateTime(LocalDateTime.now());


        JoltSpec savedSpec = joltSpecRepository.save(joltSpec);
        String createdMessage = getFormattedMessage(1, joltSpec.getSpecName());
        return ResponseEntity.ok(IntegrationMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(savedSpec).build());
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<JoltSpec> getJoltSpecById(@PathVariable String id) {
//        JoltSpec joltSpec = joltSpecRepository.findById(id).orElse(null);
//
//        if (joltSpec != null && "XSLT".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getXsltSpec() != null) {
//            // Decode the XSLT spec from Base64
//            String decodedXsltSpec = EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec());
//            joltSpec.setXsltSpec(decodedXsltSpec);
//        }
//
//        if (joltSpec != null && "JSONATA".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getJsonataSpec() != null) {
//            // Decode the XSLT spec from Base64
//            String decodedJsonataSpec = EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec());
//            joltSpec.setJsonataSpec(decodedJsonataSpec);
//        }
//        return ResponseEntity.ok(joltSpec);
//    }
    @GetMapping("/{site}/{id}")
    public ResponseEntity<JoltSpec> getJoltSpecById(@PathVariable String site, @PathVariable String id) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        if(id == null || id.isEmpty()) throw new IntegrationException(7002);
        JoltSpec joltSpec = joltSpecRepository.findByIdAndSite(id, site);
        if(joltSpec ==null) throw new IntegrationException(7003);

        if (joltSpec != null && "XSLT".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getXsltSpec() != null) {
            // Decode the XSLT spec from Base64
            String decodedXsltSpec = EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec());
            joltSpec.setXsltSpec(decodedXsltSpec);
        }

        if (joltSpec != null && "JSONATA".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getJsonataSpec() != null) {
            // Decode the JSONATA spec from Base64
            String decodedJsonataSpec = EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec());
            joltSpec.setJsonataSpec(decodedJsonataSpec);
        }
        return ResponseEntity.ok(joltSpec);
    }

//    @GetMapping("/all")
//    public ResponseEntity<List<JoltSpec>> getAllJoltSpecs() {
//        return ResponseEntity.ok(joltSpecRepository.findAll());
//    }

    @GetMapping("/all/{site}")
    public ResponseEntity<List<JoltSpec>> getAllJoltSpecs(@PathVariable String site) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        return ResponseEntity.ok(joltSpecRepository.findAllBySite(site));
    }

    //    @GetMapping("/all/{type}")
//    public ResponseEntity<List<JoltSpec>> getAllJoltSpecsByType(@PathVariable String type) {
//        List<JoltSpec> joltSpecs = joltSpecRepository.findByType(type);
//        return ResponseEntity.ok(joltSpecs);
//    }
    @GetMapping("/all/{site}/{type}")
    public ResponseEntity<List<JoltSpec>> getAllJoltSpecsByType(@PathVariable String site, @PathVariable String type) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        if(type == null || type.isEmpty()) throw new IntegrationException(7005);
        List<JoltSpec> joltSpecs = joltSpecRepository.findByTypeAndSite(type, site);
        return ResponseEntity.ok(joltSpecs);
    }

//    @GetMapping("/all/decoded")
//    public ResponseEntity<List<JoltSpec>> getAllJoltSpecsDecoded() {
//        List<JoltSpec> joltSpecs = joltSpecRepository.findAll();
//
//        for (JoltSpec joltSpec : joltSpecs) {
//            if ("XSLT".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getXsltSpec() != null) {
//                String decodedXsltSpec = EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec());
//                joltSpec.setXsltSpec(decodedXsltSpec);
//            }
//
//            if ("JSONATA".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getJsonataSpec() != null) {
//                String decodedJsonataSpec = EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec());
//                joltSpec.setJsonataSpec(decodedJsonataSpec);
//            }
//        }
//
//        return ResponseEntity.ok(joltSpecs);
//    }

    @GetMapping("/all/decoded/{site}")
    public ResponseEntity<List<JoltSpec>> getAllJoltSpecsDecoded(@PathVariable String site) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        List<JoltSpec> joltSpecs = joltSpecRepository.findAllBySite(site);

        for (JoltSpec joltSpec : joltSpecs) {
            if ("XSLT".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getXsltSpec() != null) {
                String decodedXsltSpec = EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec());
                joltSpec.setXsltSpec(decodedXsltSpec);
            }

            if ("JSONATA".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getJsonataSpec() != null) {
                String decodedJsonataSpec = EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec());
                joltSpec.setJsonataSpec(decodedJsonataSpec);
            }
        }

        return ResponseEntity.ok(joltSpecs);
    }
//    @GetMapping("/all/specNames")
//    public ResponseEntity<List<Map<String, String>>> getAllSpecNamesAndDescriptions() {
//        List<JoltSpec> joltSpecs = joltSpecRepository.findAll();
//
//        // Create a list of maps containing only specName and description
//        List<Map<String, String>> specNamesAndDescriptions = joltSpecs.stream().map(joltSpec -> {
//            Map<String, String> specInfo = new HashMap<>();
//            specInfo.put("specName", joltSpec.getSpecName());
//            specInfo.put("description", joltSpec.getDescription());
//            specInfo.put("type",joltSpec.getType());
//            return specInfo;
//        }).collect(Collectors.toList());
//
//        return ResponseEntity.ok(specNamesAndDescriptions);
//    }

    @GetMapping("/all/specNames/{site}")
    public ResponseEntity<List<Map<String, String>>> getAllSpecNamesAndDescriptions(@PathVariable String site) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        List<JoltSpec> joltSpecs = joltSpecRepository.findAllBySite(site);

        // Create a list of maps containing only specName and description
        List<Map<String, String>> specNamesAndDescriptions = joltSpecs.stream().map(joltSpec -> {
            Map<String, String> specInfo = new HashMap<>();
            specInfo.put("specName", joltSpec.getSpecName());
            specInfo.put("description", joltSpec.getDescription());
            specInfo.put("type", joltSpec.getType());
            return specInfo;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(specNamesAndDescriptions);
    }

//    @GetMapping("/byType/specNames/{type}")
//    public ResponseEntity<List<Map<String, String>>> getSpecNamesAndDescriptionsByType(@PathVariable String type) {
//        List<JoltSpec> joltSpecs = joltSpecRepository.findByType(type);
//
//        // Create a list of maps containing only specName and description
//        List<Map<String, String>> specNamesAndDescriptions = joltSpecs.stream().map(joltSpec -> {
//            Map<String, String> specInfo = new HashMap<>();
//            specInfo.put("specName", joltSpec.getSpecName());
//            specInfo.put("description", joltSpec.getDescription());
//            return specInfo;
//        }).collect(Collectors.toList());
//
//        return ResponseEntity.ok(specNamesAndDescriptions);
//    }

    @GetMapping("/byType/specNames/{site}/{type}")
    public ResponseEntity<List<Map<String, String>>> getSpecNamesAndDescriptionsByType(@PathVariable String site, @PathVariable String type) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        List<JoltSpec> joltSpecs = joltSpecRepository.findByTypeAndSite(type, site);

        // Create a list of maps containing only specName and description
        List<Map<String, String>> specNamesAndDescriptions = joltSpecs.stream().map(joltSpec -> {
            Map<String, String> specInfo = new HashMap<>();
            specInfo.put("specName", joltSpec.getSpecName());
            specInfo.put("description", joltSpec.getDescription());
            return specInfo;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(specNamesAndDescriptions);
    }


    // Endpoint to get JoltSpec by specName
//    @GetMapping("/byName/{specName}")
//    public ResponseEntity<JoltSpec> getJoltSpecByName(@PathVariable String specName) {
//        JoltSpec joltSpec = joltSpecRepository.findBySpecName(specName);
//
//        if (joltSpec != null && "XSLT".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getXsltSpec() != null) {
//            // Decode the XSLT spec from Base64
////            String decodedXsltSpec = EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec());
////            joltSpec.setXsltSpec(decodedXsltSpec);
//            if (isBase64Encoded(joltSpec.getXsltSpec())) {
//                joltSpec.setXsltSpec(EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec()));
//            } else {
//                joltSpec.setXsltSpec(joltSpec.getXsltSpec());
//            }
//        }
//
//        if (joltSpec != null && "JSONATA".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getJsonataSpec() != null) {
//            // Decode the XSLT spec from Base64
////            String decodedJsonataSpec = EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec());
////            joltSpec.setJsonataSpec(decodedJsonataSpec);
//            if (isBase64Encoded(joltSpec.getJsonataSpec())) {
//                joltSpec.setJsonataSpec(EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec()));
//            } else {
//                joltSpec.setJsonataSpec(joltSpec.getJsonataSpec());
//            }
//        }
//        if (joltSpec != null) {
//            return ResponseEntity.ok(joltSpec);
//        } else {
//            throw new IntegrationException(8, specName);
//        }
//    }
    @GetMapping("/byName/{site}/{specName}")
    public ResponseEntity<JoltSpec> getJoltSpecByName(@PathVariable String site, @PathVariable String specName) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        JoltSpec joltSpec = joltSpecRepository.findBySpecNameAndSite(specName, site);

        if (joltSpec != null && "XSLT".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getXsltSpec() != null) {
            if (isBase64Encoded(joltSpec.getXsltSpec())) {
                joltSpec.setXsltSpec(EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec()));
            } else {
                joltSpec.setXsltSpec(joltSpec.getXsltSpec());
            }
        }

        if (joltSpec != null && "JSONATA".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getJsonataSpec() != null) {
            if (isBase64Encoded(joltSpec.getJsonataSpec())) {
                joltSpec.setJsonataSpec(EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec()));
            } else {
                joltSpec.setJsonataSpec(joltSpec.getJsonataSpec());
            }
        }
        if (joltSpec != null) {
            return ResponseEntity.ok(joltSpec);
        } else {
            throw new IntegrationException(8, specName);
        }
    }

    @PostMapping(value = "/dummy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> requestBody) {
        // Log the received message (optional)
        System.out.println("Received message: " + requestBody);

        // Return a response indicating everything is okay
        return ResponseEntity.ok("{\"status\": \"ok to go\"}");
    }

    // Endpoint for encoding a given string to Base64
    @PostMapping("/encode")
    public ResponseEntity<String> encodeString(@RequestBody String input) {
        String encoded = EncodingUtils.encodeToBase64(input);
        return ResponseEntity.ok(encoded);
    }

    // Endpoint for decoding a given Base64 string
    @PostMapping("/decode")
    public ResponseEntity<String> decodeString(@RequestBody String encoded) {
        String decoded = EncodingUtils.decodeFromBase64(encoded);
        return ResponseEntity.ok(decoded);
    }

    // Endpoint to get decoded XSLT spec by specName
//    @GetMapping("/decodedXslt/{specName}")
//    public ResponseEntity<String> getDecodedXsltSpecByName(@PathVariable String specName) {
//        JoltSpec joltSpec = joltSpecRepository.findBySpecName(specName);
//
//        if(joltSpec == null)
//            throw new IntegrationException(8, specName);
//
//        if ("XSLT".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getXsltSpec() != null) {
//            // Decode the XSLT spec from Base64
////            String decodedXsltSpec = EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec());
//
//            if (isBase64Encoded(joltSpec.getXsltSpec())) {
//                return ResponseEntity.ok(EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec()));
//            } else {
//                return ResponseEntity.ok(joltSpec.getXsltSpec());
//            }
//
////            return ResponseEntity.ok(decodedXsltSpec);
//        }
//
//        throw new IntegrationException(9);
//    }

    // Endpoint to get decoded XSLT spec by specName and site
    @GetMapping("/decodedXslt/{site}/{specName}")
    public ResponseEntity<String> getDecodedXsltSpecByName(@PathVariable String site, @PathVariable String specName) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        JoltSpec joltSpec = joltSpecRepository.findBySpecNameAndSite(specName, site);

        if (joltSpec == null) {
            throw new IntegrationException(8, specName);
        }

        if ("XSLT".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getXsltSpec() != null) {
            if (isBase64Encoded(joltSpec.getXsltSpec())) {
                return ResponseEntity.ok(EncodingUtils.decodeFromBase64(joltSpec.getXsltSpec()));
            } else {
                return ResponseEntity.ok(joltSpec.getXsltSpec());
            }
        }

        throw new IntegrationException(9);
    }

    public static boolean isBase64Encoded(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


//    @GetMapping("/decodedJsonata/{specName}")
//    public ResponseEntity<String> getDecodedJsonataSpecByName(@PathVariable String specName) {
//        JoltSpec joltSpec = joltSpecRepository.findBySpecName(specName);
//
//        if (joltSpec == null)
//            throw new IntegrationException(8, specName);
//
//        if ("JSONATA".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getJsonataSpec() != null) {
//            // Decode the XSLT spec from Base64
//            String decodedJsonataSpec = EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec());
//            joltSpec.setJsonataSpec(decodedJsonataSpec);
//            return ResponseEntity.ok(decodedJsonataSpec);
//        }
//        throw new IntegrationException(9);
//    }

    // Endpoint to get decoded JSONATA spec by specName and site
    @GetMapping("/decodedJsonata/{site}/{specName}")
    public ResponseEntity<String> getDecodedJsonataSpecByName(@PathVariable String site, @PathVariable String specName) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        JoltSpec joltSpec = joltSpecRepository.findBySpecNameAndSite(specName, site);

        if (joltSpec == null) {
            throw new IntegrationException(8, specName);
        }

        if ("JSONATA".equalsIgnoreCase(joltSpec.getType()) && joltSpec.getJsonataSpec() != null) {
            String decodedJsonataSpec = EncodingUtils.decodeFromBase64(joltSpec.getJsonataSpec());
            joltSpec.setJsonataSpec(decodedJsonataSpec);
            return ResponseEntity.ok(decodedJsonataSpec);
        }
        throw new IntegrationException(9);
    }

//    @PostMapping("/update")
//    public ResponseEntity<?> updateJoltSpec(@RequestBody JoltSpec joltSpec) {
//        // Validate that the ID is present
//        if (joltSpec.getSpecName() == null) {
//            throw new IntegrationException(10);
//        }
//
//        // Check if the JoltSpec with the given ID exists
//        JoltSpec existingSpec = joltSpecRepository.findBySpecName(joltSpec.getSpecName());
//        if (existingSpec == null) {
//            throw new IntegrationException(8, joltSpec.getSpecName());
//        }
//
//        // Check if the new specName already exists in another JoltSpec
//        if (!existingSpec.getSpecName().equals(joltSpec.getSpecName())) {
//            JoltSpec specWithSameName = joltSpecRepository.findBySpecName(joltSpec.getSpecName());
//            if (specWithSameName != null) {
//                throw new IntegrationException(7, specWithSameName.getSpecName(), specWithSameName.getType());
//            }
//        }
//
//        // Update the existing JoltSpec entity with new values
//        existingSpec.setSpecName(joltSpec.getSpecName());
//        existingSpec.setType(joltSpec.getType());
//        existingSpec.setXsltSpec(joltSpec.getXsltSpec());
//        existingSpec.setJoltSpec(joltSpec.getJoltSpec());
//        existingSpec.setJsonataSpec(joltSpec.getJsonataSpec());
//        existingSpec.setDescription(joltSpec.getDescription());
//        existingSpec.setLastModifiedDateTime(LocalDateTime.now());
//
//        // Save the updated JoltSpec
//        JoltSpec updatedSpec = joltSpecRepository.save(existingSpec);
//        String updatedMessage = getFormattedMessage(2, joltSpec.getSpecName());
//        return ResponseEntity.ok(IntegrationMessageModel.builder().message_details(new MessageDetails(updatedMessage,"S")).response(updatedSpec).build());
//    }

    @PostMapping("/update")
    public ResponseEntity<?> updateJoltSpec(@RequestBody JoltSpec joltSpec) {
        if(joltSpec.getSite() == null || joltSpec.getSite().isEmpty()) throw new IntegrationException(7001);
        if(joltSpec.getSpecName() == null || joltSpec.getSpecName().isEmpty()) throw new IntegrationException(10);

        // Check if the JoltSpec with the given ID exists
        JoltSpec existingSpec = joltSpecRepository.findBySpecNameAndSite(joltSpec.getSpecName(),joltSpec.getSite());
        if (existingSpec == null) {
            throw new IntegrationException(8, joltSpec.getSpecName());
        }

        // Check if the new specName already exists in another JoltSpec
        if (!existingSpec.getSpecName().equals(joltSpec.getSpecName())) {
            JoltSpec specWithSameName = joltSpecRepository.findBySpecNameAndSite(joltSpec.getSpecName(),joltSpec.getSite());
            if (specWithSameName != null) {
                throw new IntegrationException(7, specWithSameName.getSpecName(), specWithSameName.getType());
            }
        }

        // Update the existing JoltSpec entity with new values
        existingSpec.setSpecName(joltSpec.getSpecName());
        existingSpec.setType(joltSpec.getType());
        existingSpec.setXsltSpec(joltSpec.getXsltSpec());
        existingSpec.setJoltSpec(joltSpec.getJoltSpec());
        existingSpec.setJsonataSpec(joltSpec.getJsonataSpec());
        existingSpec.setDescription(joltSpec.getDescription());
        existingSpec.setLastModifiedDateTime(LocalDateTime.now());

        // Save the updated JoltSpec
        JoltSpec updatedSpec = joltSpecRepository.save(existingSpec);
        String updatedMessage = getFormattedMessage(2, joltSpec.getSpecName());
        return ResponseEntity.ok(IntegrationMessageModel.builder().message_details(new MessageDetails(updatedMessage,"S")).response(updatedSpec).build());
    }

//    @PostMapping("/delete")
//    public ResponseEntity<?> deleteJoltSpecBySpecName(@RequestParam String specName) {
//        // Find the JoltSpec by its specName
//        JoltSpec joltSpec = joltSpecRepository.findBySpecName(specName);
//
//        // If not found, return 404
//        if (joltSpec == null) {
//            throw new IntegrationException(8, specName);
//        }
//
//        // Delete the JoltSpec
//        joltSpecRepository.delete(joltSpec);
//        String deleteMessage = getFormattedMessage(3, joltSpec.getSpecName());
//        return ResponseEntity.ok(IntegrationMessageModel.builder().message_details(new MessageDetails(deleteMessage,"S")).build());
//    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteJoltSpecBySpecName(@RequestBody JoltSpec joltSpec) {
        if(joltSpec.getSite() == null || joltSpec.getSite().isEmpty()) throw new IntegrationException(7001);
        JoltSpec existingJoltSpec = joltSpecRepository.findBySpecNameAndSite(joltSpec.getSpecName(), joltSpec.getSite());

        // If not found, return 404
        if (joltSpec == null) {
            throw new IntegrationException(8, joltSpec.getSpecName());
        }

        // Delete the JoltSpec
        joltSpecRepository.delete(existingJoltSpec);
        String deleteMessage = getFormattedMessage(3, joltSpec.getSpecName());
        return ResponseEntity.ok(IntegrationMessageModel.builder().message_details(new MessageDetails(deleteMessage,"S")).build());
    }

    @PostMapping("/getBySpecName")
    public ResponseEntity<?> getBySpecName(@RequestBody JoltSpec joltSpec) {
        if(joltSpec.getSite() == null || joltSpec.getSite().isEmpty()) throw new IntegrationException(7001);
        if(joltSpec.getSpecName() == null || joltSpec.getSpecName().isEmpty()) throw new IntegrationException(10);
        List<JoltSpec> existingJoltSpec = joltSpecRepository.findBySpecNameContainingIgnoreCaseAndSite(joltSpec.getSpecName(), joltSpec.getSite());

        if (existingJoltSpec == null || existingJoltSpec.isEmpty()) {
            throw new IntegrationException(8, joltSpec.getSpecName());
        }

        List<SpecResponse> specResponseList = new ArrayList<>();

        SpecResponse specResponse = null;

        for(JoltSpec spec: existingJoltSpec){
            specResponse = new SpecResponse();
            specResponse.setSpecName(spec.getSpecName());
            specResponse.setDescription(spec.getDescription());
            specResponse.setType(spec.getType());
            specResponseList.add(specResponse);
        }

        return ResponseEntity.ok(specResponseList);
    }

    @PostMapping("/processRecipe")
    public ResponseEntity<Map<String, Object>> processRecipe(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> message = (Map<String, Object>) request.get("message");
            List<Object> phases = (List<Object>) message.get("phases");

            // Check each element in phases array
            for (int i = 0; i < phases.size(); i++) {
                Object phase = phases.get(i);

                // Check if current element is a nested array
                if (phase instanceof List) {
                    List<Object> subArray = (List<Object>) phase;
                    phases.remove(i); // Remove the nested array

                    // Add all phases from subarray to main array
                    for (Object subPhase : subArray) {
                        phases.add(i, subPhase);
                        i++; // Increment i to account for the newly added element
                    }
                    i--; // Decrement i since we removed the original nested array
                }
            }

            message.put("phases", phases);
            return ResponseEntity.ok(request);

        } catch (Exception e) {
            throw new RuntimeException("Error occurred at processRecipe API", e);
        }
    }

}
