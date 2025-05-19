package com.rits.uomservice.controller;

import com.rits.uomservice.dto.UOMMessageModel;
import com.rits.uomservice.dto.UOMRequest;
import com.rits.uomservice.dto.UOMResponseList;
import com.rits.uomservice.exception.UomException;
import com.rits.uomservice.model.UOMEntity;
import com.rits.uomservice.service.UOMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/app/v1/uom-service")
public class UOMController {

    @Autowired
    private UOMService uomService;

    @PostMapping("/create")
    public ResponseEntity<?> createUOM(@RequestBody UOMRequest uomRequest) throws Exception {
        UOMMessageModel createUOM;
        try {
            createUOM = uomService.createUOM(uomRequest);
            return ResponseEntity.ok(UOMMessageModel.builder()
                    .message_details(createUOM.getMessage_details())
                    .response(createUOM.getResponse())
                    .build());
        } catch (UomException uomException) {
            throw uomException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<UOMMessageModel> updateUOM(@RequestBody UOMRequest uomRequest) throws Exception {
        UOMMessageModel updateUOM;
        try {
            updateUOM = uomService.updateUOM(uomRequest);
            return ResponseEntity.ok(UOMMessageModel.builder()
                    .message_details(updateUOM.getMessage_details())
                    .response(updateUOM.getResponse())
                    .build());
        } catch (UomException uomException) {
            throw uomException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("delete")
    public ResponseEntity<UOMMessageModel> deleteUOM(@RequestBody UOMRequest uomRequest) throws Exception {
        UOMMessageModel deleteResponse;
            try {
                deleteResponse = uomService.deleteUOM(uomRequest);
                return ResponseEntity.ok(deleteResponse);

            } catch (UomException uomException) {
                throw uomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

//        @GetMapping("retrieve")
//        public ResponseEntity<UOMEntity> retrieveUOM(@RequestBody UOMRequest uomRequest) throws Exception {
//            UOMEntity retrieveUOM;
//
//                try {
//                    retrieveUOM = uomService.retrieveUOM(uomRequest);
//                    return ResponseEntity.ok(retrieveUOM);
//                } catch (UomException uomException) {
//                    throw uomException;
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
@GetMapping("/retrieve")
public ResponseEntity<?> retrieveUOM(@RequestBody UOMRequest uomRequest) {
    try {
        UOMEntity uomEntity = uomService.retrieveUOM(uomRequest);
        return ResponseEntity.ok(uomEntity);
    } catch (UomException e) {
        if (e.getCode() == 2601) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("UOM ID " + uomRequest.getId() + " doesn't exist.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred.");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred.");
    }
}

    @PostMapping("/retrieveAll")
    public ResponseEntity<UOMResponseList> retrieveAll(@RequestBody UOMRequest uomRequest) {
        UOMResponseList retrieveAllUOM;
            try {
                retrieveAllUOM = uomService.retrieveAll(uomRequest);
                return ResponseEntity.ok(retrieveAllUOM);
            } catch (UomException uomException) {
                throw uomException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    @PostMapping("/retrieveTop50")
    public ResponseEntity<UOMResponseList> retrieveTop50(@RequestBody UOMRequest uomRequest) {
        UOMResponseList top50UOMs;
        try {
            top50UOMs = uomService.retrieveTop50(uomRequest.getSite());
            return ResponseEntity.ok(top50UOMs);
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/createBaseUnitConvertion")
    public ResponseEntity<UOMMessageModel> createBaseUnitConvertion(@RequestBody UOMRequest uomRequest) {
        if(!StringUtils.hasText(uomRequest.getSite()))
            throw new UomException(1);

        try {
            return ResponseEntity.ok(uomService.createBaseUnitConvertion(uomRequest));
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/updateBaseUnitConvertion")
    public ResponseEntity<UOMMessageModel> updateBaseUnitConvertion(@RequestBody UOMRequest uomRequest) {
        if(!StringUtils.hasText(uomRequest.getSite()))
            throw new UomException(1);

        try {
            return ResponseEntity.ok(uomService.updateBaseUnitConvertion(uomRequest));
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/deleteBaseUnitConvertion")
    public ResponseEntity<UOMMessageModel> deleteBaseUnitConvertion(@RequestBody UOMRequest uomRequest) {
        if(!StringUtils.hasText(uomRequest.getSite()))
            throw new UomException(1);

        try {
            return ResponseEntity.ok(uomService.deleteBaseUnitConvertion(uomRequest));
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveBaseUnitConvertion")
    public ResponseEntity<UOMMessageModel> retrieveBaseUnitConvertion(@RequestBody UOMRequest uomRequest) {
        if(!StringUtils.hasText(uomRequest.getSite()))
            throw new UomException(1);

        try {
            return ResponseEntity.ok(uomService.retrieveBaseUnitConvertion(uomRequest));
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveAllBaseUnitConvertion")
    public ResponseEntity<UOMMessageModel> retrieveAllBaseUnitConvertion(@RequestBody UOMRequest uomRequest) {
        if(!StringUtils.hasText(uomRequest.getSite()))
            throw new UomException(1);

        try {
            return ResponseEntity.ok(uomService.retrieveAllBaseUnitConvertion(uomRequest));
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveTop50BaseUnitConvertion")
    public ResponseEntity<UOMMessageModel> retrieveTop50BaseUnitConvertion(@RequestBody UOMRequest uomRequest) {
        if(!StringUtils.hasText(uomRequest.getSite()))
            throw new UomException(1);

        try {
            return ResponseEntity.ok(uomService.retrieveTop50BaseUnitConvertion(uomRequest));
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @PostMapping("/unitConvertion")
    public ResponseEntity<String> unitConvertion(@RequestBody UOMRequest uomRequest) {
        if(!StringUtils.hasText(uomRequest.getSite()))
            throw new UomException(1);

        try {
            return ResponseEntity.ok(uomService.unitConvertion(uomRequest));
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getUomByFilteredName")
    public ResponseEntity<UOMMessageModel> getUomByFilteredName(@RequestBody UOMRequest uomRequest) {
        if(!StringUtils.hasText(uomRequest.getSite()))
            throw new UomException(1);

        try {
            return ResponseEntity.ok(uomService.getUomByFilteredName(uomRequest));
        } catch (UomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}





