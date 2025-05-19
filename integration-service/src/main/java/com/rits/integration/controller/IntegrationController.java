package com.rits.integration.controller;

import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.model.IntegrationMessageModel;
import com.rits.integration.service.IntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/app/v1/integration-service")
public class IntegrationController {

    @Autowired
    private IntegrationService integrationService;

    @PostMapping("createIntegrationFlow")
    public ResponseEntity<?> createOrUpdateIntegration(@RequestBody IntegrationEntity integrationEntity) {
        IntegrationMessageModel createIntegration = integrationService.createOrUpdateIntegration(integrationEntity);
        return ResponseEntity.ok(IntegrationMessageModel.builder().message_details(createIntegration.getMessage_details())
                .integrationEntityResponse(createIntegration.getIntegrationEntityResponse()).build());
    }



//    @GetMapping("getAllIntegrationFlow")
//    public ResponseEntity<List<IntegrationEntity>> getAllIntegrations() {
//        return ResponseEntity.ok(integrationService.getAllIntegrations());
//    }

    @GetMapping("getAllIntegrationFlow")
    public ResponseEntity<List<IntegrationEntity>> getAllIntegrations(@PathVariable String site) {
        return ResponseEntity.ok(integrationService.getAllIntegrations(site));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Optional<IntegrationEntity>> getIntegrationById(@PathVariable String id) {
//        return ResponseEntity.ok(integrationService.getIntegrationById(id));
//    }

    @GetMapping("/{site}/{id}")
    public ResponseEntity<IntegrationEntity> getIntegrationById(@PathVariable String site, @PathVariable String id) {
        return ResponseEntity.ok(integrationService.getIntegrationById(site, id));
    }

//    @GetMapping("/identifier/{identifier}")
//    public ResponseEntity<IntegrationEntity> getIntegrationByIdentifier(@PathVariable String identifier) {
//        return ResponseEntity.ok(integrationService.getIntegrationByIdentifier(identifier));
//    }

    @GetMapping("/identifier/{site}/{identifier}")
    public ResponseEntity<IntegrationEntity> getIntegrationByIdentifier(@PathVariable String site, @PathVariable String identifier) {
        return ResponseEntity.ok(integrationService.getIntegrationByIdentifier(site, identifier));
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<IntegrationEntity> updateIntegration(@PathVariable String id, @RequestBody IntegrationEntity entity) {
//        return ResponseEntity.ok(integrationService.updateIntegration(id, entity));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<IntegrationEntity> updateIntegration(@PathVariable String id, @RequestBody IntegrationEntity entity) {
        return ResponseEntity.ok(integrationService.updateIntegration(id, entity));
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteIntegration(@PathVariable String id) {
//        IntegrationMessageModel response = integrationService.deleteIntegration(id);
//        return ResponseEntity.ok(response);
//    }

    @DeleteMapping("/{site}/{id}")
    public ResponseEntity<?> deleteIntegration(@PathVariable String site, @PathVariable String id) {
        IntegrationMessageModel response = integrationService.deleteIntegration(site, id);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("getAllIntegrationSummary")
//    public ResponseEntity<List<Map<String, Object>>> getAllIntegrationSummaries() {
//        return ResponseEntity.ok(integrationService.getAllIntegrationSummaries());
//    }

    @PostMapping("getAllIntegrationSummary")
    public ResponseEntity<List<Map<String, Object>>> getAllIntegrationSummaries(@RequestBody IntegrationEntity entity) {
        return ResponseEntity.ok(integrationService.getAllIntegrationSummaries(entity));
    }
}

/*
package com.rits.integration.controller;

import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.service.IntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/v1/integration-service")
public class IntegrationController {

    @Autowired
    private IntegrationService integrationService;

    @PostMapping("createIntegrationFlow")
    public ResponseEntity<IntegrationEntity> createIntegration(@RequestBody IntegrationEntity integrationEntity) {
        return ResponseEntity.ok(integrationService.createIntegration(integrationEntity));
    }

    @GetMapping("getAllIntegrationFlow")
    public ResponseEntity<List<IntegrationEntity>> getAllIntegrations() {
        return ResponseEntity.ok(integrationService.getAllIntegrations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<IntegrationEntity>> getIntegrationById(@PathVariable String id) {
        return ResponseEntity.ok(integrationService.getIntegrationById(id));
    }

    @GetMapping("/identifier/{identifier}")
    public ResponseEntity<IntegrationEntity> getIntegrationByIdentifier(@PathVariable String identifier) {
        return ResponseEntity.ok(integrationService.getIntegrationByIdentifier(identifier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IntegrationEntity> updateIntegration(@PathVariable String id, @RequestBody IntegrationEntity entity) {
        return ResponseEntity.ok(integrationService.updateIntegration(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIntegration(@PathVariable String id) {
        integrationService.deleteIntegration(id);
        return ResponseEntity.noContent().build();
    }
}
*/
