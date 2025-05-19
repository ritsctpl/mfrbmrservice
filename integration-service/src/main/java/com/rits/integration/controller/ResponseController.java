package com.rits.integration.controller;

import com.rits.integration.dto.FilterByMultiFieldRequest;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/v1/integration-service/workflowresponses")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    //    @GetMapping("/getall")
//    public ResponseEntity<List<CustomResponseEntity>> getAllResponses() {
//        return ResponseEntity.ok(responseService.getAllResponses());
//    }
    @GetMapping("/getall/{site}")
    public ResponseEntity<List<CustomResponseEntity>> getAllResponses(@PathVariable String site) {
        return ResponseEntity.ok(responseService.getAllResponses(site));
    }

    //    @GetMapping("/{id}")
//    public ResponseEntity<Optional<CustomResponseEntity>> getResponseById(@PathVariable String id) {
//        return ResponseEntity.ok(responseService.getResponseById(id));
//    }
    @GetMapping("/{site}/{id}")
    public ResponseEntity<CustomResponseEntity> getResponseById(@PathVariable String site,@PathVariable String id) {
        return ResponseEntity.ok(responseService.getResponseById(site,id));
    }

    //    @PostMapping
//    public ResponseEntity<CustomResponseEntity> createResponse(@RequestBody CustomResponseEntity responseEntity) {
//        return ResponseEntity.ok(responseService.createResponse(responseEntity));
//    }
    @PostMapping
    public ResponseEntity<CustomResponseEntity> createResponse(@RequestBody CustomResponseEntity responseEntity) {
        return ResponseEntity.ok(responseService.createResponse(responseEntity));
    }

    //    @PutMapping("/{id}")
//    public ResponseEntity<CustomResponseEntity> updateResponse(@PathVariable String id, @RequestBody CustomResponseEntity updatedResponse) {
//        CustomResponseEntity updated = responseService.updateResponse(id, updatedResponse);
//        if (updated != null) {
//            return ResponseEntity.ok(updated);
//        }
//        return ResponseEntity.notFound().build();
//    }
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponseEntity> updateResponse(@PathVariable String id, @RequestBody CustomResponseEntity updatedResponse) {
        CustomResponseEntity updated = responseService.updateResponse(id, updatedResponse);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    //    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteResponse(@PathVariable String id) {
//        responseService.deleteResponse(id);
//        return ResponseEntity.noContent().build();
//    }
    @DeleteMapping("{site}/{id}")
    public ResponseEntity<Void> deleteResponse(@PathVariable String site,@PathVariable String id) {
        responseService.deleteResponse(site,id);
        return ResponseEntity.noContent().build();
    }

    // New Methods:

    // Get top 50 responses ordered by createdDateTime (latest first)
//    @GetMapping("/gettop50")
//    public ResponseEntity<List<CustomResponseEntity>> getTop50Responses() {
//        return ResponseEntity.ok(responseService.getTop50Responses());
//    }
    @GetMapping("/gettop50/{site}")
    public ResponseEntity<List<CustomResponseEntity>> getTop50Responses(@PathVariable String site) {
        return ResponseEntity.ok(responseService.getTop50Responses(site));
    }

    // Get responses filtered by createdDateTime range
    @GetMapping("/byDateRange")
    public ResponseEntity<List<CustomResponseEntity>> getResponsesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(responseService.getResponsesByDateRange(startDate, endDate));
    }

    // Get responses filtered by identifier
    @GetMapping("/byIdentifier/{identifier}")
    public ResponseEntity<CustomResponseEntity> getResponsesByIdentifier(@PathVariable String identifier) {
        Optional<CustomResponseEntity> response = responseService.getResponseByIdentifier(identifier);
        return response.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    // Get responses filtered by status
    @GetMapping("/byStatus/{status}")
    public ResponseEntity<List<CustomResponseEntity>> getResponsesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(responseService.getResponsesByStatus(status));
    }
    @GetMapping("/filter")
    public ResponseEntity<List<CustomResponseEntity>> getFilteredResponses(
            @RequestParam String identifier,
            @RequestParam String status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<CustomResponseEntity> responses = responseService.getFilteredResponses(identifier, status, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/delete")
    public boolean deleteResponseEntities(@RequestBody FilterByMultiFieldRequest request) {
        return responseService.deleteResponseEntities(request.getSite(), request.getStatus(), request.getHours(), request.getMinutes(), request.getSeconds());
    }

    @PostMapping("/combinationFilter")
    public ResponseEntity<List<CustomResponseEntity>> getDataByCombination(@RequestBody FilterByMultiFieldRequest request){
        List<CustomResponseEntity> responses = responseService.getDataByCombination(request);
        return ResponseEntity.ok(responses);
    }

}

/*
package com.rits.integration.controller;


import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/responses")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    @GetMapping("/getall")
    public ResponseEntity<List<CustomResponseEntity>> getAllResponses() {
        return ResponseEntity.ok(responseService.getAllResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<CustomResponseEntity>> getResponseById(@PathVariable String id) {
        return ResponseEntity.ok(responseService.getResponseById(id));
    }

    @PostMapping
    public ResponseEntity<CustomResponseEntity> createResponse(@RequestBody CustomResponseEntity responseEntity) {
        return ResponseEntity.ok(responseService.createResponse(responseEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomResponseEntity> updateResponse(@PathVariable String id, @RequestBody CustomResponseEntity updatedResponse) {
        CustomResponseEntity updated = responseService.updateResponse(id, updatedResponse);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponse(@PathVariable String id) {
        responseService.deleteResponse(id);
        return ResponseEntity.noContent().build();
    }
}


*/