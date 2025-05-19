package com.rits.mfrrecipesservice.controller;

import com.rits.mfrrecipesservice.dto.*;
import com.rits.mfrrecipesservice.exception.MfrRecipesException;
import com.rits.mfrrecipesservice.model.MfrRecipesServices;
import com.rits.mfrservice.dto.MfrRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/mfrrecipes-service")
public class MfrRecipesController {
    private final MfrRecipesServices mfrRecipesServices;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createMfr(@RequestBody RecordRequest mfrRequest){
        MfrMessageModel createResponse;
        if(mfrRequest.getSite()!=null && !mfrRequest.getSite().isEmpty()){
            try {
                createResponse= mfrRecipesServices.createMfr(mfrRequest);

                return ResponseEntity.ok(createResponse);
            }catch (MfrRecipesException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1302,mfrRequest.getSite());
    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.CREATED)
    public MfrRecipes retrieveMfrRecipes(@RequestBody MfrRecipesRequest mfrRecipesRequest){
        if(mfrRecipesRequest.getSite()!=null && !mfrRecipesRequest.getSite().isEmpty()){
            try {
                return mfrRecipesServices.retrieveMfrRecipes(mfrRecipesRequest);
            }catch (MfrRecipesException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1302,mfrRecipesRequest.getSite());
    }

//    @PostMapping("/getNewMfrBmrOrBpr")
    @PostMapping("/getNewMfr")
    @ResponseStatus(HttpStatus.CREATED)
    public MFRResponse getNewMfr(@RequestBody MfrRecipesRequest mfrRecipesRequest){
        if(mfrRecipesRequest.getSite() != null && !mfrRecipesRequest.getSite().isEmpty()){
            try {
                return mfrRecipesServices.getNewMfr(mfrRecipesRequest.getType(), mfrRecipesRequest.getSite());
            }catch (MfrRecipesException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1302,mfrRecipesRequest.getSite());
    }

    @PostMapping("/retrieveAll")
    public ResponseEntity<MFRResponseList> retrieveAllMfrRecipes(@RequestBody MfrRecipesRequest mfrRecipesRequest) {
        MFRResponseList retrieveAllMfr;
        if (mfrRecipesRequest.getSite() != null && !mfrRecipesRequest.getSite().isEmpty()) {
            try {
                retrieveAllMfr = mfrRecipesServices.retrieveAllMfrRecipes(mfrRecipesRequest.getSite());
                return ResponseEntity.ok(retrieveAllMfr);
            } catch (MfrRecipesException mfrException) {
                throw mfrException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1);

    }

    @PostMapping("/getMfrList")
    public List<String> getMfrList(@RequestBody MfrRecipesRequest mfrRecipesRequest){
        if (mfrRecipesRequest.getSite() != null && !mfrRecipesRequest.getSite().isEmpty()) {
            try {
                return mfrRecipesServices.getMfrList(mfrRecipesRequest);
            } catch (MfrRecipesException mfrException) {
                throw mfrException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1);
    }


//    @PostMapping("/populateBmrRecord")
    @PostMapping("/getSelectiveMfrData")
    public RMIResponseClass getSelectiveMfrData(@RequestBody MFRResponse mfrResponse){
        if (mfrResponse.getSite() != null && !mfrResponse.getSite().isEmpty()) {
            try {
                return mfrRecipesServices.getSelectiveMfrData(mfrResponse);
            } catch (MfrRecipesException mfrException) {
                throw mfrException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1);
    }

    @PostMapping("/getMfrToMfrConv")
    public MfrMessageModel getMfrToMfrConv(@RequestBody RecordRequest newMfrRequest){
        if (newMfrRequest.getSite() != null && !newMfrRequest.getSite().isEmpty()) {
            try {
                return mfrRecipesServices.getMfrToMfrConv(newMfrRequest);
            } catch (MfrRecipesException mfrException) {
                throw mfrException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1);
    }
}
