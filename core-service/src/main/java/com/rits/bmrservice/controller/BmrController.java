package com.rits.bmrservice.controller;

import com.rits.bmrservice.dto.*;
import com.rits.bmrservice.model.BmrServices;
import com.rits.mfrrecipesservice.dto.*;
import com.rits.mfrrecipesservice.exception.MfrRecipesException;
import com.rits.mfrrecipesservice.model.MfrRecipesServices;
import com.rits.mfrservice.Exception.MfrException;
import com.rits.mfrservice.dto.MFRResponseList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/bmr-service")
public class BmrController {
    private final BmrServices bmrServices;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createBmr(@RequestBody BmrRecipes bmrRequest){
        BmrMessageModel createResponse;
        if(bmrRequest.getSite()!=null && !bmrRequest.getSite().isEmpty()){
            try {
                createResponse= bmrServices.createBmr(bmrRequest);

                return ResponseEntity.ok(createResponse);
            }catch (MfrRecipesException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1302,bmrRequest.getSite());
    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.CREATED)
    public BmrMessageModel retrieve(@RequestBody BmrRecipes bmrRequest){
        BmrMessageModel createResponse;
        if(bmrRequest.getSite()!=null && !bmrRequest.getSite().isEmpty()){
            try {
                return bmrServices.retrieve(bmrRequest);

            }catch (MfrRecipesException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1302,bmrRequest.getSite());
    }

    @PostMapping("/retrieveAll")
    @ResponseStatus(HttpStatus.CREATED)
    public BMRResponseList retrieveAll(@RequestBody BmrRecipes bmrRequest){
        BmrMessageModel createResponse;
        if(bmrRequest.getSite()!=null && !bmrRequest.getSite().isEmpty()){
            try {
                return bmrServices.retrieveAll(bmrRequest);

            }catch (MfrRecipesException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1302,bmrRequest.getSite());
    }

    @PostMapping("/bmrPopulate")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> bmrPopulate(@RequestBody BmrRequest bmrRequest){
        BmrMessageModel createResponse;
        if(bmrRequest.getSite()!=null && !bmrRequest.getSite().isEmpty()){
            try {
                createResponse= bmrServices.bmrPopulate(bmrRequest);

                return ResponseEntity.ok(createResponse);
            }catch (MfrRecipesException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1302,bmrRequest.getSite());
    }

    @PostMapping("/getNewBmr")
    @ResponseStatus(HttpStatus.CREATED)
    public BMRResponse getNewBmr(@RequestBody BmrRequest bmrRequest){
        if(bmrRequest.getSite() != null && !bmrRequest.getSite().isEmpty()){
            try {
                return bmrServices.getNewBmr(bmrRequest.getType(), bmrRequest.getSite());
            }catch (MfrRecipesException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1302,bmrRequest.getSite());
    }

    @PostMapping("/getBmrList")
    public List<String> getBmrList(@RequestBody BmrRequest bmrRequest){
        if (bmrRequest.getSite() != null && !bmrRequest.getSite().isEmpty()) {
            try {
                return bmrServices.getBmrList(bmrRequest);
            } catch (MfrRecipesException mfrException) {
                throw mfrException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrRecipesException(1);
    }
}
