package com.rits.masterdataservice.controller;

import com.rits.masterdataservice.service.MasterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("app/v1/master-data")
public class MasterDataController {

    @Autowired
    private MasterDataService masterDataService;

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeMasterData() {
        try {
            masterDataService.initializeMasterData();
            return ResponseEntity.ok("Master data initialized successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/refresh/{collectionName}")
    public ResponseEntity<String> refreshMasterData(@PathVariable String collectionName) {
        try {
            masterDataService.refreshMasterDataForCollection(collectionName);
            return ResponseEntity.ok("Master data refreshed for collection: " + collectionName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
