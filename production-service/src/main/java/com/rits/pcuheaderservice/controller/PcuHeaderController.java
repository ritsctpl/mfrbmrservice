package com.rits.pcuheaderservice.controller;

import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.pcuheaderservice.dto.Response;
import com.rits.pcuheaderservice.exception.PcuHeaderException;
import com.rits.pcuheaderservice.model.BomHeaderMessageModel;
import com.rits.pcuheaderservice.model.PcuHeaderMessageModel;
import com.rits.pcuheaderservice.model.PcuHeader;
import com.rits.pcuheaderservice.service.PcuHeaderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/pcuheader-service")
public class PcuHeaderController {

    private final PcuHeaderServiceImpl pcuHeaderServiceImpl;


//     "required": [
//    "pcuNumberList",
//    "shopOrder"
//  ]

//    {
//        "pcuNumberList":[
//        {
//            "pcuNumber":"1"
//        },
//        {
//            "pcuNumber":"2"
//        },
//        {
//            "pcuNumber":"3"
//        }
//    ],
//        "shopOrder":{
//        "site":"RITS",
//                "shopOrder": "SO123",
//                "status": "Open",
//                "orderType": "Production",
//                "item": "item3",
//                "itemVersion": "C",
//                "bomType": "Single-Level",
//                "bom": "BOM1",
//                "bomVersion": "B",
//                "routing": "routing1",
//                "routingVersion": "A"
//    }
//    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public PcuHeaderMessageModel create(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getShopOrder().getSite() != null && !pcuHeaderRequest.getShopOrder().getSite().isEmpty()) {
        try {
            PcuHeaderMessageModel createdPcuHeaders = pcuHeaderServiceImpl.create(pcuHeaderRequest);
            return createdPcuHeaders;
        } catch (PcuHeaderException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getShopOrder().getSite());
    }

//    {
//        "pcuNumberList":[
//        {
//            "pcuNumber":"1"
//        },
//        {
//            "pcuNumber":"2"
//        },
//        {
//            "pcuNumber":"3"
//        }
//    ],
//        "pcuRequest":{
//        "site": "RITS",
//                "pcuBO": "PcuBO: RITS , 1",
//                "shopOrderBO": "ShopOrderBO: RITS , SO123",
//                "itemBO": "ItemBO: item3 , C",
//                "qtyToWork": 0,
//                "qtyInQueue": 3,
//                "qtyInHold": 0,
//                "qtyDone": 0,
//                "router": [
//        {
//            "pcuRouterBO": "PcuRouterBO: routing1 , A",
//                "status": "New"
//        }
//        ],
//        "bom": [
//        {
//            "pcuBomBO": "PcuBomBO: BOM1 , B",
//                "status": "New"
//        }
//        ],
//        "dataCollection": null,
//                "toolList": null,
//                "certification": null,
//                "nc": null,
//                "workInstruction": null,
//                "labour": null,
//                "active": 1,
//                "createdDateTime": "2023-05-19T12:12:35.4996933",
//                "modifiedDateTime": "2023-05-19T12:12:35.4996933"
//    }
//    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<PcuHeader>> update(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
            try {
                List<PcuHeader> updatedPcuHeaders = pcuHeaderServiceImpl.update(pcuHeaderRequest);
                return ResponseEntity.ok(updatedPcuHeaders);
            } catch (PcuHeaderException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getShopOrder().getSite());
    }



    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PcuHeader>> retrieve(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        try {
            List<PcuHeader> retrievedRecords = pcuHeaderServiceImpl.retrieve(pcuHeaderRequest.getSite());
            return ResponseEntity.ok(retrievedRecords);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/retrieveAllPcu")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PcuHeader>> retrieveAll(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        try {
            List<PcuHeader> retrievedRecords = pcuHeaderServiceImpl.retrieveAll(pcuHeaderRequest.getSite());
            return ResponseEntity.ok(retrievedRecords);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrievePcuList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BomHeaderMessageModel> retrievePcuList(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        try {
            return ResponseEntity.ok(pcuHeaderServiceImpl.retrievePcuList(pcuHeaderRequest.getSite()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    {
//        "site": "RITS",
//            "pcuBO": "PcuBO: RITS , 51"
//    }

    @PostMapping("/readPcu")
    @ResponseStatus(HttpStatus.OK)
    public PcuHeader retrieveByPcuBO(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        try {
            PcuHeader retrievedRecord = pcuHeaderServiceImpl.retrieveByPcuBO(pcuHeaderRequest);
            return retrievedRecord;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveByShopOrder")
    @ResponseStatus(HttpStatus.OK)
    public List<PcuHeader> retrieveByShopOrder(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        try {
            List<PcuHeader> retrievedRecord = pcuHeaderServiceImpl.retrieveByShopOrder(pcuHeaderRequest);
            return retrievedRecord;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    {
//        "site":"RITS",
//            "pcuBO": "PcuBO: RITS , 1",
//            "pcuBomBO": "PcuBomBO: BOM1 , B"
//    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isExist(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
            try {
                Boolean existsByBom = pcuHeaderServiceImpl.isExist(pcuHeaderRequest);
                return ResponseEntity.ok(existsByBom);
            } catch (PcuHeaderException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "pcuBO":"PcuBO: RITS , 1",
//            "pcuRouterBO": "PcuRouterBO: routing1 , A"
//    }

    @PostMapping("/checkRouterStatus")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> CheckRouterReleased(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
        try {
            Boolean routerReleasedCheck = pcuHeaderServiceImpl.CheckRouterReleased(pcuHeaderRequest);
            return ResponseEntity.ok(routerReleasedCheck);
        } catch (PcuHeaderException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }  }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());

    }

//    {
//        "site":"RITS",
//            "pcuBO":"PcuBO: RITS , 1",
//            "pcuBomBO": "PcuBomBO: BOM1 , B"
//    }

    @PostMapping("/checkBomStatus")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> CheckBomReleased(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
        try {
            Boolean bomReleasedCheck = pcuHeaderServiceImpl.CheckBomReleased(pcuHeaderRequest);
            return ResponseEntity.ok(bomReleasedCheck);
        } catch (PcuHeaderException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "pcuBO":"PcuBO: RITS , 1",
//            "pcuRouterBO": "PcuRouterBO: routing1 , A"
//    }

    @PostMapping("/updateRouterStatus")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PcuHeader> updateStatusOfRouterToReleased(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
        try {
            PcuHeader updateRouterStatus =  pcuHeaderServiceImpl.updateStatusOfRouterToReleased(pcuHeaderRequest);
            return  ResponseEntity.ok(updateRouterStatus);
        } catch (PcuHeaderException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "pcuBO":"PcuBO: RITS , 1",
//            "pcuBomBO": "PcuBomBO: BOM1 , B"
//    }

    @PostMapping("/updateBomStatus")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PcuHeader> updateStatusOfBomToReleased(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
        try {
            PcuHeader updateBomStatus = pcuHeaderServiceImpl.updateStatusOfBomToReleased(pcuHeaderRequest);
            return ResponseEntity.ok(updateBomStatus);
        } catch (PcuHeaderException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());
    }

//    {
//        "site":"RITS",
//            "pcuBO":"PcuBO: RITS , 1",
//            "pcuRouterBO": "PcuRouterBO: routing1 , A",
//            "pcuBomBO": "PcuBomBO: BOM1 , B"
//
//    }


    @PostMapping("/checkBomAndRouterStatus")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> checkIfBomAndRouterReleased(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
        try {
            Boolean bomAndRouterReleasedCheck = pcuHeaderServiceImpl.checkIfBomAndRouterReleased(pcuHeaderRequest);
            return ResponseEntity.ok(bomAndRouterReleasedCheck);
        } catch (PcuHeaderException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());
    }


//    {
//        "site":"RITS",
//            "pcuBO":"PcuBO: RITS , 4"
//    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Response> delete(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
        try {
            Response deletedRecord = pcuHeaderServiceImpl.delete(pcuHeaderRequest);
            return ResponseEntity.ok(deletedRecord);
        } catch (PcuHeaderException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());
    }
    @PostMapping("unDelete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Response> unDelete(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
            try {
                Response deletedRecord = pcuHeaderServiceImpl.unDelete(pcuHeaderRequest);
                return ResponseEntity.ok(deletedRecord);
            } catch (PcuHeaderException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @PostMapping("/retrieveByItem")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PcuHeader>> retrieveByItem(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
            try {
                List<PcuHeader> retrieveByItem = pcuHeaderServiceImpl.retrieveByItem(pcuHeaderRequest.getSite(), pcuHeaderRequest.getItemBO());
                return ResponseEntity.ok(retrieveByItem);
            } catch (PcuHeaderException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());
    }

    @PostMapping("/retrievePcuHeaderList")
    @ResponseStatus(HttpStatus.OK)
    public List<String> retrievePcuHeaderList(@RequestBody PcuHeaderRequest pcuHeaderRequest)
    {
        if (pcuHeaderRequest.getSite() != null && !pcuHeaderRequest.getSite().isEmpty()) {
            try {
                return pcuHeaderServiceImpl.retrievePcuHeaderList(pcuHeaderRequest);
            } catch (PcuHeaderException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new PcuHeaderException(2403,pcuHeaderRequest.getSite());
    }
}
