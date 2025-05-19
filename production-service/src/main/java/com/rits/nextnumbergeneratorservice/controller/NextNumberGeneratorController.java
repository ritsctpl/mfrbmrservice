package com.rits.nextnumbergeneratorservice.controller;

import com.rits.nextnumbergeneratorservice.dto.*;
import com.rits.nextnumbergeneratorservice.exception.NextNumberGeneratorException;
import com.rits.nextnumbergeneratorservice.model.NextNumberMessageModel;
import com.rits.nextnumbergeneratorservice.model.NextNumberGenerator;
import com.rits.nextnumbergeneratorservice.service.NextNumberGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/nextnumbergenerator-service")
public class NextNumberGeneratorController {

    private final NextNumberGeneratorService nextNumberGeneratorService;

    @PostMapping("create")
    public NextNumberMessageModel create(@RequestBody NextNumberGeneratorRequest nextNumberGeneratorRequest)
    {
        try {
            return nextNumberGeneratorService.create(nextNumberGeneratorRequest);
        } catch (NextNumberGeneratorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("update")
    public NextNumberMessageModel update(@RequestBody NextNumberGeneratorRequest nextNumberGeneratorRequest)
    {
        try {
            return nextNumberGeneratorService.updateNextNumber(nextNumberGeneratorRequest);
        } catch (NextNumberGeneratorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    public ResponseEntity<?> retrieve(@RequestBody NextNumberGeneratorRequest nextNumberGeneratorRequest)
    {
        try {
            NextNumberGenerator retrievedRecord= nextNumberGeneratorService.retrieveNextNumber(nextNumberGeneratorRequest);
            return ResponseEntity.ok(retrievedRecord);
        } catch (NextNumberGeneratorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("delete")
    public NextNumberMessageModel delete(@RequestBody NextNumberGeneratorRequest nextNumberGeneratorRequest)
    {
        try {
            return nextNumberGeneratorService.delete(nextNumberGeneratorRequest);
        } catch (NextNumberGeneratorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("generateNextNumber")
    public NextNumberMessageModel generateNextNumber(@RequestBody GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)
    {
        try {
            return nextNumberGeneratorService.generateNextNumber(generatePrefixAndSuffixRequest);
        } catch (NextNumberGeneratorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("updateCurrentSequence")
    public GeneratedNextNumber updateCurrentSequence(@RequestBody GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)
    {
        try {
            return nextNumberGeneratorService.updateCurrentSequence(generatePrefixAndSuffixRequest);
        } catch (NextNumberGeneratorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getNewInventory")
    public List<NextnumberList> getNewInventory(@RequestBody InventoryNextNumberRequest inventoryNextNumberRequest)
    {
        try {
            return nextNumberGeneratorService.getNewInventory(inventoryNextNumberRequest.getSite(),
                    inventoryNextNumberRequest.getObject(),
                    inventoryNextNumberRequest.getObjectVersion(),
                    inventoryNextNumberRequest.getSize(),
                    inventoryNextNumberRequest.getUserBO(),
                    inventoryNextNumberRequest.getNextNumberActivity(),
                    inventoryNextNumberRequest.getNumberType());
        } catch (NextNumberGeneratorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/generateNextNumbers")
    public ResponseEntity<?> generateNextNumber(@RequestBody NextNumberRequest request) {
        try {
            List<NextNumberResponse> nextNumbers = nextNumberGeneratorService.createNextNumberList(
                    request.getNumberType(),
                    request.getSite(),
                    request.getObject(),
                    request.getObjectVersion(),
                    request.getShopOrder(),
                    request.getPcu(),
                    request.getNcBo(),
                    request.getUserBo(),
                    request.getBatchQty()
            );
            return ResponseEntity.ok(nextNumbers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/createNextNumbers")
    public ResponseEntity<?> generateNextNumbers(@RequestBody NextNumberRequest request) {
        try {
            List<NextNumberResponse> nextNumbers = nextNumberGeneratorService.createNextNumbers(
                    request.getNumberType(),
                    request.getSite(),
                    request.getObject(),
                    request.getObjectVersion(),
                    request.getShopOrder(),
                    request.getPcu(),
                    request.getNcBo(),
                    request.getUserBo(),
                    request.getBatchQty()
            );
            return ResponseEntity.ok(nextNumbers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("getAndUpdateCurrentSequence")
    public NextNumberMessageModel getAndUpdateCurrentSequence(@RequestBody GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)
    {
        try {
            return nextNumberGeneratorService.getAndUpdateCurrentSequence(generatePrefixAndSuffixRequest);
        } catch (NextNumberGeneratorException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
