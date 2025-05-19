package com.rits.licencevalidationservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.licencevalidationservice.dto.*;
import com.rits.licencevalidationservice.exception.NCCodeException;
import com.rits.licencevalidationservice.model.DispositionRoutings;
import com.rits.licencevalidationservice.model.MessageModel;
import com.rits.licencevalidationservice.model.NCCode;
import com.rits.licencevalidationservice.service.NCCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("app/v1/nccode-service")
public class NCCodeController {
    private final NCCodeService ncCodeService;
    private final ObjectMapper objectMapper;

                    //    {
                    //        "site":"RITS",
                    //            "ncCode":"nc3",
                    //            "description":"",
                    //            "status":"",
                    //            "assignNCtoComponent":"",
                    //            "ncCategory":"",
                    //            "dpmoCategory":"",
                    //            "ncDatatype":"",
                    //            "collectRequiredNCDataonNC":"",
                    //            "messageType":"",
                    //            "ncPriority":  0  ,
                    //            "maximumNCLimit":0 ,
                    //            "ncSeverity":"",
                    //            "secondaryCodeSpecialInstruction":"",
                    //            "canBePrimaryCode":  true ,
                    //            "closureRequired":   true ,
                    //            "autoClosePrimaryNC":  true  ,
                    //            "autoCloseIncident":  true  ,
                    //            "secondaryRequiredForClosure":    true ,
                    //            "erpQNCode": true  ,
                    //            "erpCatalog":   "",
                    //            "erpCodeGroup":"",
                    //            "erpCode":"",
                    //            "oeeQualityKPIRelevant": true ,
                    //            "dispositionRoutingsList":[
                    //        {
                    //            "routing":"routing1"
                    //        },{
                    //        "routing":"routing2"
                    //    }
                    //			],
                    //        "operationGroupsList":[
                    //        {
                    //            "validOperations":"op1",
                    //                "dispositionGroups":"dp1",
                    //                "enabled":  true
                    //        }
                    //			],
                    //        "ncGroupsList":[
                    //        {
                    //            "ncGroup":"ncGroups1"
                    //        }
                    //		],
                    //        "secondariesGroupsList": [
                    //        {
                    //            "secondaries":"ncGroups1"
                    //        }
                    //			],
                    //        "customDataList":[
                    //        {
                    //            "customData":"",
                    //                "customField":""
                    //        }
                    //				]
                    //    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createNCCode(@RequestBody NCCodeRequest ncCodeRequest) throws JsonProcessingException {
//        MessageModel validationResponse = ncCodeService.validation( payload);
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        NCCodeRequest ncCodeRequest = new ObjectMapper().convertValue(payload, NCCodeRequest.class);
        MessageModel createResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(ncCodeRequest.getSite()).hookPoint("PRE").activity("nccode-service").hookableMethod("create").request(objectMapper.writeValueAsString(ncCodeRequest)).build();
            String preExtensionResponse = ncCodeService.callExtension(preExtension);
            NCCodeRequest preExtensionNCCode = objectMapper.readValue(preExtensionResponse, NCCodeRequest.class);

            try {
                createResponse = ncCodeService.createNCCode(preExtensionNCCode);
                Extension postExtension = Extension.builder().site(ncCodeRequest.getSite()).hookPoint("POST").activity("nccode-service").hookableMethod("create").request(objectMapper.writeValueAsString(createResponse.getResponse())).build();
                String postExtensionResponse = ncCodeService.callExtension(postExtension);
                NCCode postExtensionNCCode = objectMapper.readValue(postExtensionResponse, NCCode.class);
                return ResponseEntity.ok(MessageModel.builder().message_details(createResponse.getMessage_details()).response(postExtensionNCCode).build());
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }

                            //
                            //    {
                            //        "site":"RITS",
                            //            "ncCode":"nc3",
                            //            "description":"",
                            //            "status":"",
                            //            "assignNCtoComponent":"",
                            //            "ncCategory":"",
                            //            "dpmoCategory":"",
                            //            "ncDatatype":"",
                            //            "collectRequiredNCDataonNC":"",
                            //            "messageType":"",
                            //            "ncPriority":  0  ,
                            //            "maximumNCLimit":0 ,
                            //            "ncSeverity":"",
                            //            "secondaryCodeSpecialInstruction":"",
                            //            "canBePrimaryCode":  true ,
                            //            "closureRequired":   true ,
                            //            "autoClosePrimaryNC":  true  ,
                            //            "autoCloseIncident":  true  ,
                            //            "secondaryRequiredForClosure":    true ,
                            //            "erpQNCode": true  ,
                            //            "erpCatalog":   "",
                            //            "erpCodeGroup":"",
                            //            "erpCode":"",
                            //            "oeeQualityKPIRelevant": true ,
                            //            "dispositionRoutingsList":[
                            //        {
                            //            "routing":"routing1"
                            //        },{
                            //        "routing":"routing2"
                            //    }
                            //			],
                            //        "operationGroupsList":[
                            //        {
                            //            "validOperations":"op1",
                            //                "dispositionGroups":"dp1",
                            //                "enabled":  true
                            //        }
                            //			],
                            //        "ncGroupsList":[
                            //        {
                            //            "ncGroup":"ncGroups1"
                            //        }
                            //		],
                            //        "secondariesGroupsList": [
                            //        {
                            //            "secondaries":"ncGroups1"
                            //        }
                            //			],
                            //        "customDataList":[
                            //        {
                            //            "customData":"",
                            //                "customField":""
                            //        }
                            //				]
                            //    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateNCCode(@RequestBody NCCodeRequest ncCodeRequest) throws JsonProcessingException {
        MessageModel updateResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(ncCodeRequest.getSite()).hookPoint("PRE").activity("nccode-service").hookableMethod("update").request(objectMapper.writeValueAsString(ncCodeRequest)).build();
            String preExtensionResponse = ncCodeService.callExtension(preExtension);
            NCCodeRequest preExtensionNCCode = objectMapper.readValue(preExtensionResponse, NCCodeRequest.class);

            try {
                updateResponse = ncCodeService.updateNCCode(preExtensionNCCode);
                Extension postExtension = Extension.builder().site(ncCodeRequest.getSite()).hookPoint("POST").activity("nccode-service").hookableMethod("update").request(objectMapper.writeValueAsString(updateResponse.getResponse())).build();
                String postExtensionResponse = ncCodeService.callExtension(postExtension);
                NCCode postExtensionNCCode = objectMapper.readValue(postExtensionResponse, NCCode.class);
                return ResponseEntity.ok(MessageModel.builder().message_details(updateResponse.getMessage_details()).response(postExtensionNCCode).build());
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }


                //    {
                //        "site":"RITS"
                //    }
    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getNCCodeListByCreationDate(@RequestBody NCCodeRequest ncCodeRequest) {
        NCCodeResponseList top50Response=new NCCodeResponseList();
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                top50Response= ncCodeService.getNCCodeListByCreationDate(ncCodeRequest);
                return ResponseEntity.ok(top50Response);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }


                //    {
                //        "site":"RITS",
                //            "ncCode":"n"
                //    }
    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getNCCodeList(@RequestBody NCCodeRequest ncCodeRequest ) {
        NCCodeResponseList ncCodeListResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                ncCodeListResponse= ncCodeService.getNCCodeList(ncCodeRequest);
                return ResponseEntity.ok(ncCodeListResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }


                //    {
                //        "site":"RITS",
                //            "ncCode":"nc1"
                //    }
    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveNCCode(@RequestBody NCCodeRequest ncCodeRequest) throws JsonProcessingException {
        NCCode retrieveNCCodeResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(ncCodeRequest.getSite()).hookPoint("PRE").activity("nccode-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(ncCodeRequest)).build();
            String preExtensionResponse = ncCodeService.callExtension(preExtension);
            NCCodeRequest preExtensionNCCode = objectMapper.readValue(preExtensionResponse, NCCodeRequest.class);

            try {
                retrieveNCCodeResponse = ncCodeService.retrieveNCCode(preExtensionNCCode);
                Extension postExtension = Extension.builder().site(ncCodeRequest.getSite()).hookPoint("POST").activity("nccode-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveNCCodeResponse)).build();
                String postExtensionResponse = ncCodeService.callExtension(postExtension);
                NCCode postExtensionNCCode = objectMapper.readValue(postExtensionResponse, NCCode.class);
                return ResponseEntity.ok(postExtensionNCCode);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }

    //    {

    //        "site":"RITS",
    //            "ncCode":"nc1"

    //    }
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteNCCode(@RequestBody NCCodeRequest ncCodeRequest) throws JsonProcessingException {
        MessageModel deleteResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            objectMapper.registerModule(new JavaTimeModule());
            Extension preExtension = Extension.builder().site(ncCodeRequest.getSite()).hookPoint("PRE").activity("nccode-service").hookableMethod("delete").request(objectMapper.writeValueAsString(ncCodeRequest)).build();
            String preExtensionResponse = ncCodeService.callExtension(preExtension);
            NCCodeRequest preExtensionNCCode = objectMapper.readValue(preExtensionResponse, NCCodeRequest.class);

            try {
                deleteResponse = ncCodeService.deleteNCCode(preExtensionNCCode);
                return ResponseEntity.ok(deleteResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }



    //    {
    //        "site":"RITS",
    //            "ncCode":"nc1"
    //    }
    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isNCCodeExist(@RequestBody NCCodeRequest ncCodeRequest){
        Boolean isExistResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                isExistResponse= ncCodeService.isNCCodeExist(ncCodeRequest);
                return ResponseEntity.ok(isExistResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }



    //    {
    //        "site":"RITS",
    //    }

    @PostMapping("/retrieveBySite")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> retrieveAllBySite(@RequestBody NCCodeRequest ncCodeRequest){
        List<NCCodeResponse> retrieveAllBySiteResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                retrieveAllBySiteResponse= ncCodeService.retrieveAllBySite(ncCodeRequest);
                return ResponseEntity.ok(retrieveAllBySiteResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }



        //    {
        //        "site":"RITS",
        //            "ncCode":"nc1",
        //            "routing":["routing1","routing2"]
        //    }
    @PostMapping("/addRouting")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> associateRoutingToDispositionRoutingList(@RequestBody DispositionRoutingListRequest dispositionRoutingListRequest){
        NCCode associateRoutingResponse;
        if(dispositionRoutingListRequest.getSite()!=null && !dispositionRoutingListRequest.getSite().isEmpty()) {
            try {
                associateRoutingResponse= ncCodeService.associateRoutingToDispositionRoutingList(dispositionRoutingListRequest);
                return ResponseEntity.ok(associateRoutingResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, dispositionRoutingListRequest.getSite());
    }



            //
            //    {
            //        "site":"RITS",
            //            "ncCode":"nc1xx",
            //            "routing":["routing3"]
            //    }
    @PostMapping("/removeRouting")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> removeRoutingFromDispositionRoutingList(@RequestBody DispositionRoutingListRequest dispositionRoutingListRequest){
        NCCode removeRoutingResponse;
        if(dispositionRoutingListRequest.getSite()!=null && !dispositionRoutingListRequest.getSite().isEmpty()) {
            try {
                removeRoutingResponse =ncCodeService.removeRoutingFromDispositionRoutingList(dispositionRoutingListRequest);
                return ResponseEntity.ok(removeRoutingResponse);
            }
            catch(NCCodeException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, dispositionRoutingListRequest.getSite());
    }


//  to retrieve the only available routing excluding the assigned ones
//    {
//        "site":"RITS",
//            "ncCode":"nc1"
//    }

    // to retreiev alll routing
    // {
    //      "site":"RITS",
    //   }
    @PostMapping("/retrieveAvailableRoutings")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getAvailableActivities(@RequestBody NCCodeRequest ncCodeRequest) {
        AvailableRoutingList availableRoutingsResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                availableRoutingsResponse= ncCodeService.getAvailableRoutings(ncCodeRequest);
                return ResponseEntity.ok(availableRoutingsResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }



        //    {
        //        "site":"RITS",
        //            "ncCode":"nc1",
        //            "secondaries":["secondaries2","secondaries1"]
        //    }
    @PostMapping("/addSecondaries")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> associateSecondariesToSecondariesGroupsList(@RequestBody SecondariesGroupsListRequest secondariesGroupsListRequest){
        NCCode associateSecondariesResponse;
        if(secondariesGroupsListRequest.getSite()!=null && !secondariesGroupsListRequest.getSite().isEmpty()) {
            try {
                associateSecondariesResponse= ncCodeService.associateSecondariesToSecondariesGroupsList(secondariesGroupsListRequest);
                return ResponseEntity.ok(associateSecondariesResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, secondariesGroupsListRequest.getSite());
    }



            //    {
            //        "site":"RITS",
            //            "ncCode":"nc1xxx",
            //            "secondaries":["secondaries2","secondaries1"]
            //    }
    @PostMapping("/removeSecondaries")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> removeRoutingFromDispositionRoutingList(@RequestBody SecondariesGroupsListRequest secondariesGroupsListRequest){
        NCCode removeSecondariesResponse;
        if(secondariesGroupsListRequest.getSite()!=null && !secondariesGroupsListRequest.getSite().isEmpty()) {
            try {
                removeSecondariesResponse =ncCodeService.removeSecondariesFromSecondariesGroupsList(secondariesGroupsListRequest);
                return ResponseEntity.ok(removeSecondariesResponse);
            } catch(NCCodeException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, secondariesGroupsListRequest.getSite());
    }

    @PostMapping("/retrieveAvailableSecondaries")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getAvailableSecondaries(@RequestBody NCCodeRequest ncCodeRequest) {
        AvailableSecondariesGroupsList availableSecondariesResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                availableSecondariesResponse= ncCodeService.getAvailableSecondaries(ncCodeRequest);
                return ResponseEntity.ok(availableSecondariesResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }


        //    {
        //        "site":"RITS",
        //            "ncCode":"nc1",
        //            "ncGroups":["ncGroups1","ncGroups2"]
        //    }
    @PostMapping("/addNCGroups")
    @ResponseStatus(HttpStatus.OK)
    public Boolean associateNCGroupsToNCGroupsList(@RequestBody NCGroupsListRequest ncGroupsListRequest){
        Boolean associateNCGroupsResponse;
        if(ncGroupsListRequest.getSite()!=null && !ncGroupsListRequest.getSite().isEmpty()) {
            try {
                associateNCGroupsResponse= ncCodeService.associateNCGroupsToNCGroupsList(ncGroupsListRequest);
                return (associateNCGroupsResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncGroupsListRequest.getSite());
    }


        //    {
        //        "site":"RITS",
        //            "ncCode":"nc1xxxx",
        //            "ncGroups":["ncGroups2"]
        //    }
    @PostMapping("/removeNCGroups")
    @ResponseStatus(HttpStatus.OK)
    public Boolean removeNCGroupsToNCGroupsList(@RequestBody NCGroupsListRequest ncGroupsListRequest){
        Boolean removeNCGroupsResponse;
        if(ncGroupsListRequest.getSite()!=null && !ncGroupsListRequest.getSite().isEmpty()) {
            try {
                removeNCGroupsResponse =ncCodeService.removeNCGroupsToNCGroupsList(ncGroupsListRequest);
                return (removeNCGroupsResponse);
            } catch(NCCodeException e){
                throw e;
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncGroupsListRequest.getSite());
    }

    @PostMapping("/retrieveAvailableNCGroups")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getAvailableNCGroups(@RequestBody NCCodeRequest ncCodeRequest) {
        AvailableNCGroupsList availableSNCGroupsResponse;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                availableSNCGroupsResponse= ncCodeService.getAvailableNCGroups(ncCodeRequest);
                return ResponseEntity.ok(availableSNCGroupsResponse);
            }catch(NCCodeException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCCodeException(5002, ncCodeRequest.getSite());
    }
    @PostMapping("getDispositionRouting")
    public List<DispositionRoutings>  getDispositionRouting(@RequestBody NCCodeRequest ncCodeRequest){
        List<DispositionRoutings>  dispositionRoutingList=new ArrayList<DispositionRoutings>();
        dispositionRoutingList=ncCodeService.getAllDispositionRouting(ncCodeRequest);
        return dispositionRoutingList;
    }
}
