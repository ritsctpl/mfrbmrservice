package com.rits.oeeservice.controller;

import com.rits.oeeservice.model.ApiConfiguration;
import com.rits.oeeservice.service.ApiConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/v1/oee-service/apiconfigurations")
public class ApiConfigurationController {

    @Autowired
    private ApiConfigurationService apiConfigurationService;

    @PostMapping
    public ResponseEntity<ApiConfiguration> createApiConfiguration(@RequestBody ApiConfiguration configuration) {
        ApiConfiguration created = apiConfigurationService.createApiConfiguration(configuration);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ApiConfiguration>> getAllApiConfigurations() {
        List<ApiConfiguration> list = apiConfigurationService.getAllApiConfigurations();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiConfiguration> getApiConfiguration(@PathVariable Long id) {
        ApiConfiguration configuration = apiConfigurationService.getApiConfigurationById(id);
        if (configuration == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(configuration, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiConfiguration> updateApiConfiguration(@PathVariable Long id,
                                                                   @RequestBody ApiConfiguration configuration) {
        ApiConfiguration updated = apiConfigurationService.updateApiConfiguration(id, configuration);
        if (updated == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiConfiguration(@PathVariable Long id) {
        apiConfigurationService.deleteApiConfiguration(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
