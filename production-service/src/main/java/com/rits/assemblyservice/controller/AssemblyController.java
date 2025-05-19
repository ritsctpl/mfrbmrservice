package com.rits.assemblyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.assemblyservice.dto.*;
import com.rits.assemblyservice.exception.AssemblyException;
import com.rits.assemblyservice.model.Assembly;
import com.rits.assemblyservice.model.Component;
import com.rits.assemblyservice.service.AssemblyService;
import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import com.rits.pcucompleteservice.dto.PcuCompleteRequestInfo;
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
@RequestMapping("app/v1/assembly-service")
public class AssemblyController {
    private final AssemblyService assemblyService;
    private final ObjectMapper objectMapper;
    //{
    //    "site": "rits",
    //    "pcuBO": "PcuBO: rits,pcuBo",
    //    "componentList": [
    //        {
    //            "stepId": "50",
    //            "bomSequence": "",
    //            "component": "item11",
    //            "qty": "5",
    //            "inventoryBO": "INV001",
    //            "assembledDate": "",
    //            "assembledBy": "priya",
    //            "removed": "",
    //            "removedDate": "",
    //            "removedBY": "",
    //            "operationBO": "OP1",
    //            "resourceBO": "Res1",
    //            "assemblyDataList": [
    //                {
    //                    "sequence": "1",
    //                    "dataField": "inventory",
    //                    "dataAttribute": "INV001"
    //                },
    //                {
    //                    "sequence":"2",
    //                    "dataField":"component",
    //                    "dataAttribute":"item1"
    //                }
    //            ]
    //        }
    //    ]
    //}
    //
    @PostMapping("addComponent")
    public ResponseEntity<?> addComponent(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        boolean addComponent;


        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("PRE").activity("assembly-service").hookableMethod("addComponent").request(objectMapper.writeValueAsString(assemblyRequest)).build();
        String preExtensionResponse = assemblyService.callExtension(preExtension);
        AssemblyRequest preExtensionAssembly = objectMapper.readValue(preExtensionResponse, AssemblyRequest.class);

        try {
            addComponent = assemblyService.addComponent(preExtensionAssembly.getSite(),preExtensionAssembly.getParentPcuBO(),preExtensionAssembly.getUserId(), preExtensionAssembly.getPcuBO(), preExtensionAssembly.getComponentList().get(0));
//            Extension postExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("POST").activity("assembly-service").hookableMethod("addComponent").build();
//            String postExtensionResponse = bomService.callExtension(postExtension);
//            Bom postExtensionBom = objectMapper.readValue(postExtensionResponse, Bom.class);
//            return ResponseEntity.ok( MessageModel.builder().message_details(createBom.getMessage_details()).response(postExtensionBom).build());

            return ResponseEntity.ok(addComponent);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* Added for PCU add or Update Component.  - Senthil POC.addOrUpdateComponent,getAssemblyHierarchy
     * */
    @PostMapping("/assemblyComponent")
    public ResponseEntity<String> addOrUpdateAssembly(@RequestBody AssemblyRequest assemblyRequest) {
        try {
            for (Component component : assemblyRequest.getComponentList()) {
                // Assuming addOrUpdateComponent method handles a single component at a time.
                // You might need to adjust this logic based on your service method's capabilities.
                boolean result = assemblyService.addOrUpdateComponent(
                        assemblyRequest.getSite(),
                        assemblyRequest.getParentPcuBO(),
                        assemblyRequest.getUserId(),
                        assemblyRequest.getPcuBO(),
                        component,
                        assemblyRequest.getItemBO()
                );

                // Handle result as needed. Here, it's assumed that we proceed with all components.
            }
            return ResponseEntity.ok("Assembly updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating assembly: " + e.getMessage());
        }
    }

    @PostMapping("/hierarchy")
    public ResponseEntity<Assembly> getAssemblyHierarchy(@RequestBody AssemblyRequest request) {
        try {
            Assembly assemblyHierarchy = assemblyService.buildAssemblyHierarchy(request.getPcuBO());
            return new ResponseEntity<>(assemblyHierarchy, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("removeComponent")
    public ResponseEntity<?> removeComponent(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        boolean removeComponent;


        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("PRE").activity("assembly-service").hookableMethod("addComponent").request(objectMapper.writeValueAsString(assemblyRequest)).build();
        String preExtensionResponse = assemblyService.callExtension(preExtension);
        AssemblyRequest preExtensionAssembly = objectMapper.readValue(preExtensionResponse, AssemblyRequest.class);

        try {
            removeComponent = assemblyService.removeComponent(preExtensionAssembly.getSite(),preExtensionAssembly.getParentPcuBO(), preExtensionAssembly.getPcuBO(), preExtensionAssembly.getComponentList().get(0),preExtensionAssembly.isInventoryReturn(), preExtensionAssembly.isInventoryScrap());
//            Extension postExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("POST").activity("assembly-service").hookableMethod("addComponent").build();
//            String postExtensionResponse = bomService.callExtension(postExtension);
//            Bom postExtensionBom = objectMapper.readValue(postExtensionResponse, Bom.class);
//            return ResponseEntity.ok( MessageModel.builder().message_details(createBom.getMessage_details()).response(postExtensionBom).build());

            return ResponseEntity.ok(removeComponent);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("groupAddComponent")
    public ResponseEntity<?> groupAddComponent(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        boolean groupAddComponent;


        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("PRE").activity("assembly-service").hookableMethod("addComponent").request(objectMapper.writeValueAsString(assemblyRequest)).build();
        String preExtensionResponse = assemblyService.callExtension(preExtension);
        AssemblyRequest preExtensionAssembly = objectMapper.readValue(preExtensionResponse, AssemblyRequest.class);

        try {
            groupAddComponent = assemblyService.groupAddComponent(preExtensionAssembly.getSite(), preExtensionAssembly.getPcuBO(), preExtensionAssembly.getComponentList());
//            Extension postExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("POST").activity("assembly-service").hookableMethod("addComponent").build();
//            String postExtensionResponse = bomService.callExtension(postExtension);
//            Bom postExtensionBom = objectMapper.readValue(postExtensionResponse, Bom.class);
//            return ResponseEntity.ok( MessageModel.builder().message_details(createBom.getMessage_details()).response(postExtensionBom).build());

            return ResponseEntity.ok(groupAddComponent);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("groupRemoveComponent")
    public ResponseEntity<?> groupRemoveComponent(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        boolean groupRemoveComponent;


        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("PRE").activity("assembly-service").hookableMethod("addComponent").request(objectMapper.writeValueAsString(assemblyRequest)).build();
        String preExtensionResponse = assemblyService.callExtension(preExtension);
        AssemblyRequest preExtensionAssembly = objectMapper.readValue(preExtensionResponse, AssemblyRequest.class);

        try {
            groupRemoveComponent = assemblyService.groupRemoveComponent(preExtensionAssembly.getSite(), preExtensionAssembly.getPcuBO(), preExtensionAssembly.getComponentList(),preExtensionAssembly.isInventoryReturn(), preExtensionAssembly.isInventoryScrap());
//            Extension postExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("POST").activity("assembly-service").hookableMethod("addComponent").build();
//            String postExtensionResponse = bomService.callExtension(postExtension);
//            Bom postExtensionBom = objectMapper.readValue(postExtensionResponse, Bom.class);
//            return ResponseEntity.ok( MessageModel.builder().message_details(createBom.getMessage_details()).response(postExtensionBom).build());

            return ResponseEntity.ok(groupRemoveComponent);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //{
    //    "site": "rits",
    //    "pcuBO": "PcuBO: rits,pcuBo",
    //    "componentList": [
    //        {
    //            "stepID": "20",
    //            "bomSequence": "",
    //            "component": "item1",
    //            "qty": "4",
    //            "inventoryBO": "",
    //            "assembledDate": "",
    //            "assembledBY": "priya",
    //            "removed": "",
    //            "removedDate": "",
    //            "removedBY": "",
    //            "operationBO": "OP1",
    //            "resourceBO": "Res1",
    //            "assemblyDataList": [
    //                {
    //                    "sequence": "1",
    //                    "dataField": "inventory",
    //                    "dataAttribute": "item1"
    //                }
    //            ]
    //        }
    //    ]
    //}
    //
    @PostMapping("addNonBomComponent")
    public ResponseEntity<?> addNonBomComponent(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        boolean addNonBomComponent;


        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("PRE").activity("assembly-service").hookableMethod("addComponent").request(objectMapper.writeValueAsString(assemblyRequest)).build();
        String preExtensionResponse = assemblyService.callExtension(preExtension);
        AssemblyRequest preExtensionAssembly = objectMapper.readValue(preExtensionResponse, AssemblyRequest.class);

        try {
            addNonBomComponent = assemblyService.addNonBomComponents(preExtensionAssembly.getSite(),preExtensionAssembly.getParentPcuBO(), preExtensionAssembly.getPcuBO(), preExtensionAssembly.getComponentList().get(0));
//            Extension postExtension = Extension.builder().site(assemblyRequest.getSite()).hookPoint("POST").activity("assembly-service").hookableMethod("addComponent").build();
//            String postExtensionResponse = bomService.callExtension(postExtension);
//            Bom postExtensionBom = objectMapper.readValue(postExtensionResponse, Bom.class);
//            return ResponseEntity.ok( MessageModel.builder().message_details(createBom.getMessage_details()).response(postExtensionBom).build());

            return ResponseEntity.ok(addNonBomComponent);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //{
    //    "site":"rits",
    //    "pcuBO":"PcuBO: rits,pcuBo"
    //}
    @PostMapping("findAssembledComponent")
    public ResponseEntity<List<Component>> findAssembledComponent(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        List<Component> findAssembledComponent;
        try{
            findAssembledComponent=assemblyService.findAssembledComponent(assemblyRequest.getSite(), assemblyRequest.getPcuBO());
            return ResponseEntity.ok(findAssembledComponent);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //{
    //    "site":"rits",
    //    "pcuBO":"PcuBO: rits,pcuBo"
    //}
    @PostMapping("findAssembledComponentHistory")
    public ResponseEntity<List<Component>> findAssembledComponentHistory(@RequestBody RetrieveRequest retrieveRequest) throws Exception {
        List<Component> findAssembledComponentHistory;
        try{
            findAssembledComponentHistory=assemblyService.findAssembledComponentHistory(retrieveRequest.getSite(), retrieveRequest.getPcuBO());
            return ResponseEntity.ok(findAssembledComponentHistory);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //{
    //    "site":"rits",
    //    "pcuBO":"PcuBO: rits,pcuBo",
    //    "operationBO":"OP1",
    //    "removed":false
    //}
    @PostMapping("findAssembledComponentByOperation")
    public ResponseEntity<List<Component>> findAssembledComponentByOperation(@RequestBody RetrieveRequest retrieveRequest) throws Exception {
        List<Component> findAssembledComponentByOperation;
        try{
            findAssembledComponentByOperation=assemblyService.findAssembledComponentByOperation(retrieveRequest.getSite(), retrieveRequest.getPcuBO(),retrieveRequest.getOperationBO(),retrieveRequest.isRemoved());
            return ResponseEntity.ok(findAssembledComponentByOperation);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //{
    //    "site":"rits",
    //    "pcuBO":"PcuBO: rits,pcuBo",
    //    "dataField":"component",
    //    "dataAttribute":"item1"
    //}
    @PostMapping("findAssembledComponentByCustomField")
    public ResponseEntity<List<Component>> findAssembledComponentByCustomField(@RequestBody RetrieveRequest retrieveRequest) throws Exception {
        List<Component> findAssembledComponentByCustomField;
        try{
            findAssembledComponentByCustomField=assemblyService.findAssembledComponentByCustomField(retrieveRequest.getSite(), retrieveRequest.getPcuBO(),retrieveRequest.getDataAttribute(),retrieveRequest.getDataField());
            return ResponseEntity.ok(findAssembledComponentByCustomField);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //{
    //    "site":"rits",
    //    "pcuBO":"PcuBO: rits,pcuBo",
    //    "uniqueId":"helo"
    //}
    @PostMapping("findAssembledComponentById")
    public ResponseEntity<Component> findAssembledComponentById(@RequestBody RetrieveRequest retrieveRequest) throws Exception {
        Component findAssembledComponentById;
        try{
            findAssembledComponentById=assemblyService.findAssembledComponentById(retrieveRequest.getSite(), retrieveRequest.getPcuBO(),retrieveRequest.getUniqueId());
            return ResponseEntity.ok(findAssembledComponentById);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//{
//    "site":"rits",
//    "pcuBO":"PcuBO: rits,pcuBo",
//    "component": "item11",
//    "removed":false
//}
    @PostMapping("findAssembledByComponent")
    public ResponseEntity<List<Component>> findAssembledByComponent(@RequestBody RetrieveRequest retrieveRequest) throws Exception {
        List<Component> findAssembledByComponent;
        try{
            findAssembledByComponent=assemblyService.findAssembledByComponent(retrieveRequest.getSite(), retrieveRequest.getPcuBO(),retrieveRequest.getComponent(),retrieveRequest.isRemoved());
            return ResponseEntity.ok(findAssembledByComponent);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getDataType")
    public ResponseEntity<DataType> getDataType(@RequestBody IsExist isExist) throws Exception {
        DataType getDataType;
        try{
            getDataType=assemblyService.retrieveDataType(isExist.getSite(), isExist.getBom(), isExist.getRevision(), isExist.getItem());
            return ResponseEntity.ok(getDataType);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("findComponent")
    public ResponseEntity<ComponentResponse> findComponent(@RequestBody IsExist isExist) throws Exception {
        ComponentResponse findComponent;
        try{
            findComponent=assemblyService.findComponent(isExist.getSite(), isExist.getDatastring());
            return ResponseEntity.ok(findComponent);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getAssembly")
    public ResponseEntity<Assembly> getAssembly(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        Assembly getAssembly;
        try{
            getAssembly=assemblyService.getAssembly(assemblyRequest.getSite(), assemblyRequest.getPcuBO());
            return ResponseEntity.ok(getAssembly);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveAssembly")
    public ResponseEntity<Assembly> retrieveAssembly(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        Assembly retrieveAssembly;
        try{
            retrieveAssembly=assemblyService.retrieveAssembly(assemblyRequest.getSite(), assemblyRequest.getPcuBO());
            return ResponseEntity.ok(retrieveAssembly);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getSerializedAssembly")
    public ResponseEntity<Assembly> getSerializedAssembly(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        Assembly retrieveAssembly;
        try{
            retrieveAssembly=assemblyService.getSerializedAssembly(assemblyRequest.getSite(), assemblyRequest.getPcuBO());
            return ResponseEntity.ok(retrieveAssembly);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getSerializedAssemblyByPcuItem")
    public Assembly getSerializedAssemblyByPcuItem(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        Assembly retrieveAssembly;
        try{
            retrieveAssembly=assemblyService.getSerializedAssemblyByPcuAndItem(assemblyRequest.getSite(), assemblyRequest.getPcuBO(),assemblyRequest.getItemBO());
            return retrieveAssembly;
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getChildAssembly")
    public ResponseEntity<List<Assembly>> getChildAssembly(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        List<Assembly> getChildAssembly;
        try{
            getChildAssembly=assemblyService.getChildAssembly(assemblyRequest.getSite(), assemblyRequest.getPcuBO());
            return ResponseEntity.ok(getChildAssembly);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("isAllQuantityAssembled")
    public Boolean isAllQuantityAssembled(@RequestBody PcuCompleteReq pcuCompleteReq) throws Exception {
       Boolean isAllQuantityAssembled;
        try{
            isAllQuantityAssembled=assemblyService.isAllQuantityAssembled(pcuCompleteReq);
            return isAllQuantityAssembled;
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveSerializedAssembly")
    public ResponseEntity<Assembly> retrieveSerializedAssemblyForReturnReplace(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        Assembly retrievedAssembly;
        try{
            retrievedAssembly=assemblyService.retrieveSerializedComponentListForReturnAndReplace(assemblyRequest.getSite(), assemblyRequest.getPcuBO());
            return ResponseEntity.ok(retrievedAssembly);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveNonSerializedAssembly")
    public ResponseEntity<Assembly> retrieveNonSerializedAssemblyForReturnReplace(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        Assembly retrievedAssembly;
        try{
            retrievedAssembly=assemblyService.retrieveNonSerializedComponentListForReturnAndReplace(assemblyRequest.getSite(), assemblyRequest.getPcuBO());
            return ResponseEntity.ok(retrievedAssembly);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieve")
    public ResponseEntity<Assembly> retrieveAssemlbyRecord(@RequestBody AssemblyRequest assemblyRequest) throws Exception {
        Assembly retrievedAssembly;
        try{
            retrievedAssembly=assemblyService.retrieveAssemblyAggr(assemblyRequest.getSite(), assemblyRequest.getPcuBO());
            return ResponseEntity.ok(retrievedAssembly);
        } catch (AssemblyException assemblyException) {
            throw assemblyException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
