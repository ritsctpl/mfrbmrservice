package com.rits.ncgroupservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.ncgroupservice.dto.*;
import com.rits.ncgroupservice.exception.NcGroupException;
import com.rits.ncgroupservice.model.NcGroupMessageModel;
import com.rits.ncgroupservice.model.NcCodeDPMOCategory;
import com.rits.ncgroupservice.model.NcGroup;
import com.rits.ncgroupservice.model.Operation;
import com.rits.ncgroupservice.service.NcGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/ncgroup-service")
public class NcGroupController {
    private final NcGroupService ncGroupService;
    private final ObjectMapper objectMapper;

//
//
//    {
//        "site":"rits",
//        "ncGroup":"ncGroup"
//    }


    //{
    //    "site": "rits",
    //    "ncGroup": "ncGroup5",
    //    "description": "ncGroup 2",
    //    "ncGroupFilterPriority": "2",
    //    "ncCodeDPMOCategoryList": [
    //        {
    //            "ncCode": "nc1",
    //            "description": "desc"
    //        },
    //        {
    //            "ncCode": "code2",
    //            "description": "desc"
    //        },
    //        {
    //            "ncCode": "code3",
    //            "description": "desc"
    //        }
    //    ],
    //    "validAtAllOperations": true,
    //    "operationList": [
    //        {
    //            "operation": "OP1"
    //        },
    //        {
    //            "operation": "op2"
    //        },
    //        {
    //            "operation": "op3"
    //        }
    //    ]
    //}
    @PostMapping("create")
    public ResponseEntity<?> createNcGroup(@RequestBody NcGroupRequest ncGroupRequest) throws Exception {
//        MessageModel validationResponse = ncGroupService.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        NcGroupRequest ncGroupRequest = new ObjectMapper().convertValue(payload, NcGroupRequest.class);
            NcGroupMessageModel createNcGroup;

            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(ncGroupRequest.getSite()).hookPoint("PRE").activity("NcGroup-service").hookableMethod("create").request(objectMapper.writeValueAsString(ncGroupRequest)).build();
            String preExtensionResponse = ncGroupService.callExtension(preExtension);
            NcGroupRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, NcGroupRequest.class);

            try {
                createNcGroup = ncGroupService.createNcGroup(preExtensionRequest);
                Extension postExtension = Extension.builder().site(ncGroupRequest.getSite()).hookPoint("POST").activity("NcGroup-service").hookableMethod("create").request(objectMapper.writeValueAsString(createNcGroup.getResponse())).build();
                String postExtensionResponse = ncGroupService.callExtension(postExtension);
                NcGroup postExtensionNcGroup = objectMapper.readValue(postExtensionResponse, NcGroup.class);
                return ResponseEntity.ok( NcGroupMessageModel.builder().message_details(createNcGroup.getMessage_details()).response(postExtensionNcGroup).build());

            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

   //{
   //    "site": "rits",
   //    "ncGroup": "ncGroup5",
   //    "description": "ncGroup 2",
   //    "ncGroupFilterPriority": "2",
   //    "ncCodeDPMOCategoryList": [
   //        {
   //            "ncCode": "nc1",
   //            "description": "desc"
   //        },
   //        {
   //            "ncCode": "code2",
   //            "description": "desc"
   //        },
   //        {
   //            "ncCode": "code3",
   //            "description": "desc"
   //        }
   //    ],
   //    "validAtAllOperations": true,
   //    "operationList": [
   //        {
   //            "operation": "OP1"
   //        },
   //        {
   //            "operation": "op2"
   //        },
   //        {
   //            "operation": "op3"
   //        }
   //    ]
   //}
    @PostMapping("update")
    public ResponseEntity<NcGroupMessageModel> updateNcGroup(@RequestBody NcGroupRequest ncGroupRequest) throws Exception {

            NcGroupMessageModel updateNcGroup;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(ncGroupRequest.getSite()).hookPoint("PRE").activity("NcGroup-service").hookableMethod("update").request(objectMapper.writeValueAsString(ncGroupRequest)).build();
            String preExtensionResponse = ncGroupService.callExtension(preExtension);
            NcGroupRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, NcGroupRequest.class);

            try {
                updateNcGroup = ncGroupService.updateNcGroup(preExtensionRequest);
                Extension postExtension = Extension.builder().site(ncGroupRequest.getSite()).hookPoint("POST").activity("NcGroup-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateNcGroup.getResponse())).build();
                String postExtensionResponse = ncGroupService.callExtension(postExtension);
                NcGroup postExtensionNcGroup = objectMapper.readValue(postExtensionResponse, NcGroup.class);
                return ResponseEntity.ok( NcGroupMessageModel.builder().message_details(updateNcGroup.getMessage_details()).response(postExtensionNcGroup).build());

            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    //{
    //    "site":"rits",
    //    "ncGroup":"ncGroup3"
    //}
    @PostMapping("retrieve")
    public ResponseEntity<NcGroup> retrieveNcGroup(@RequestBody NcGroupRequest ncGroupRequest) throws Exception {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            NcGroup retrieveNcGroup;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(ncGroupRequest.getSite()).hookPoint("PRE").activity("NcGroup-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(ncGroupRequest)).build();
            String preExtensionResponse = ncGroupService.callExtension(preExtension);
            NcGroupRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, NcGroupRequest.class);

            try {
                retrieveNcGroup = ncGroupService.retrieveNcGroup(preExtensionRequest.getSite(), ncGroupRequest.getNcGroup());
                Extension postExtension = Extension.builder().site(ncGroupRequest.getSite()).hookPoint("POST").activity("NcGroup-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveNcGroup)).build();
                String postExtensionResponse = ncGroupService.callExtension(postExtension);
                NcGroup postExtensionNcGroup = objectMapper.readValue(postExtensionResponse, NcGroup.class);
                return ResponseEntity.ok(postExtensionNcGroup);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }

    //{
    //    "site":"rits",
    //    "ncGroup":"n"
    //}
    @PostMapping("retrieveAll")
    public ResponseEntity<NcGroupResponseList> getNcGroupList(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            NcGroupResponseList getNcGroupList;
            try {
                getNcGroupList = ncGroupService.getAllNCGroup(ncGroupRequest.getSite(), ncGroupRequest.getNcGroup());
                return ResponseEntity.ok(getNcGroupList);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }

    //{
    //    "site":"rits"
    //}
    @PostMapping("retrieveTop50")
    public ResponseEntity<NcGroupResponseList> getNcGroupListByCreationDate(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            NcGroupResponseList retrieveTop50NcGroup;
            try {
                retrieveTop50NcGroup = ncGroupService.getAllNCGroupByCreatedDate(ncGroupRequest.getSite());
                return ResponseEntity.ok(retrieveTop50NcGroup);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }

    //{
    //    "site":"rits",
    //    "ncGroup":"ncGroup"
    //}
    @PostMapping("delete")
    public ResponseEntity<NcGroupMessageModel> deleteNcGroup(@RequestBody NcGroupRequest ncGroupRequest) throws Exception {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            NcGroupMessageModel deleteResponse;
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(ncGroupRequest.getSite()).hookPoint("PRE").activity("NcGroup-service").hookableMethod("delete").request(objectMapper.writeValueAsString(ncGroupRequest)).build();
            String preExtensionResponse = ncGroupService.callExtension(preExtension);
            NcGroupRequest preExtensionRequest = objectMapper.readValue(preExtensionResponse, NcGroupRequest.class);

            try {
                deleteResponse = ncGroupService.deleteNcGroup(preExtensionRequest.getSite(), preExtensionRequest.getNcGroup(),preExtensionRequest.getUserId());
//                Extension postExtension= Extension.builder().site(ncGroupRequest.getSite()).hookPoint("POST").activity("NcGroup-service").hookableMethod("delete").request(objectMapper.writeValueAsString(deleteResponse.getResponse()).build();
//                String postExtensionResponse=certificateService.callExtension(postExtension);
//                Certificate postExtensionNcGroup=objectMapper.readValue(postExtensionResponse,Certificate.class);
//                return ResponseEntity.ok(postExtensionNcGroup);

                return ResponseEntity.ok(deleteResponse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }
    //{
    //    "site":"rits",
    //    "ncGroup": "ncGroup3"
    //}


    //{
    //    "site": "rits"
    //}
    @PostMapping("getAvailableNcCode")
    public ResponseEntity<List<NcCodeDPMOCategory>> getAvailableNcCode(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            List<NcCodeDPMOCategory> getAllNcCode;
            try {
                getAllNcCode = ncGroupService.getAllNcCode(ncGroupRequest.getSite(), ncGroupRequest.getNcGroup());
                return ResponseEntity.ok(getAllNcCode);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }

    //{
    //    "site": "rits",
    //    "ncGroup": "ncGroup3",
    //    "ncCodeDPMOCategoryList": [
    //        {
    //            "ncCode": "code1"
    //        }
    //    ]
    //}
    @PostMapping("addNcCode")
    public Boolean assignNcCode(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            Boolean assignNcCode;
            try {
                assignNcCode = ncGroupService.assignNcCode(ncGroupRequest.getSite(), ncGroupRequest.getNcGroup(), ncGroupRequest.getNcCodeDPMOCategoryList());
                return assignNcCode;
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }

    //{
    //    "site": "rits",
    //    "ncGroup": "ncGroup3",
    //    "ncCodeDPMOCategoryList": [
    //        {
    //            "ncCode": "code1"
    //        }
    //    ]
    //}
    @PostMapping("removeNcCode")
    public Boolean removeNcCode(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
           Boolean removeNcCode;
            try {
                removeNcCode = ncGroupService.removeNcCode(ncGroupRequest.getSite(), ncGroupRequest.getNcGroup(), ncGroupRequest.getNcCodeDPMOCategoryList());
                return (removeNcCode);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }
    //{
    //    "site":"rits"
    //}


    //
    @PostMapping("getAvailableOperation")
    public ResponseEntity<List<Operation>> getAvailableOperation(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            List<Operation> getAllOperation;
            try {
                getAllOperation = ncGroupService.getAllOperation(ncGroupRequest.getSite(), ncGroupRequest.getNcGroup());
                return ResponseEntity.ok(getAllOperation);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }

    //{
    //    "site": "rits",
    //    "ncGroup": "ncGroup1",
    //    "operationList": [
    //        {
    //            "operation": "op1"
    //        }
    //    ]
    //}
    @PostMapping("addOperation")
    public ResponseEntity<List<Operation>> assignOperation(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            List<Operation> assignOperation;
            try {
                assignOperation = ncGroupService.assignOperation(ncGroupRequest.getSite(), ncGroupRequest.getNcGroup(), ncGroupRequest.getOperationList());
                return ResponseEntity.ok(assignOperation);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }

    //{
    //    "site": "rits",
    //    "ncGroup": "ncGroup3",
    //    "operationList": [
    //        {
    //            "operation": "op1"
    //        }
    //    ]
    //}
    @PostMapping("removeOperation")
    public ResponseEntity<List<Operation>> removeOperation(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            List<Operation> removeOperation;
            try {
                removeOperation = ncGroupService.removeOperation(ncGroupRequest.getSite(), ncGroupRequest.getNcGroup(), ncGroupRequest.getOperationList());
                return ResponseEntity.ok(removeOperation);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }

    //{
    //    "site": "rits"
    //}
    @PostMapping("retrieveBySite")
    public ResponseEntity<List<NcGroupResponse>> getAvailableNcGroup(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            List<NcGroupResponse> getAvailableNcGroup;
            try {
                getAvailableNcGroup = ncGroupService.getAvailableNcGroup(ncGroupRequest.getSite());
                return ResponseEntity.ok(getAvailableNcGroup);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }
    @PostMapping("retrieveByOperation")
    public ResponseEntity<List<NcGroupResponse>> getNcGroupByOperation(@RequestBody NcGroupRequest ncGroupRequest) {
        if (ncGroupRequest.getSite() != null && !ncGroupRequest.getSite().isEmpty()) {
            List<NcGroupResponse> getAvailableNcGroup;
            try {
                getAvailableNcGroup = ncGroupService.getNcGroupByOperation(ncGroupRequest.getSite(),ncGroupRequest.getOperation());
                return ResponseEntity.ok(getAvailableNcGroup);
            } catch (NcGroupException ncGroupException) {
                throw ncGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NcGroupException(1);
    }


}
