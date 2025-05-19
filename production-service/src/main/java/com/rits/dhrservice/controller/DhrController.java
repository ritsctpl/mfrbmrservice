package com.rits.dhrservice.controller;

import com.rits.dhrservice.dto.*;
import com.rits.dhrservice.exception.DhrException;
import com.rits.dhrservice.service.DhrService;
import com.rits.pcucompleteservice.exception.PcuCompleteException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/dhr-service")
public class DhrController {

    private final DhrService dhrService;

    @PostMapping("retrieveDataCollection")
    public List<ParametricMeasures> retrievedDataCollection(@RequestBody DhrRequest dhrRequest)
    {
        try {
            List<ParametricMeasures> retrievedList = dhrService.retrieveFromDataCollection(dhrRequest.getSite(), dhrRequest.getPcu());
            return retrievedList;
        }catch (DhrException dhrException) {
            throw dhrException;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveToolLog")
    public List<ToolLog> retrievedToolLog(@RequestBody DhrRequest dhrRequest)
    {
        try {
            List<ToolLog> retrievedList = dhrService.retrieveFromToolLog(dhrRequest.getSite(), dhrRequest.getPcu());
            return retrievedList;
        }catch (DhrException dhrException) {
            throw dhrException;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveLoggedNc")
    public List<NcData> retrievedLoggedNc(@RequestBody DhrRequest dhrRequest)
    {
        try {
            List<NcData> retrievedList = dhrService.retrieveFromLoggedNc(dhrRequest.getSite(), dhrRequest.getPcu());
            return retrievedList;
        }catch (DhrException dhrException) {
            throw dhrException;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveAssembledComponents")
    public List<Component> retrievedAssembledComponents(@RequestBody DhrRequest dhrRequest)
    {
        try {
            List<Component> retrievedList = dhrService.retrieveFromAssembly(dhrRequest.getSite(), dhrRequest.getPcu());
            return retrievedList;
        }catch (DhrException dhrException) {
            throw dhrException;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveWorkInstruction")
    public List<ProductionLogMongo> retrievedWorkInstruction(@RequestBody DhrRequest dhrRequest)
    {
        try {
            List<ProductionLogMongo> retrievedList = dhrService.retrieveForWorkInstruction(dhrRequest.getSite(), dhrRequest.getPcu());
            return retrievedList;
        }catch (DhrException dhrException) {
            throw dhrException;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAssembly")
    public Assembly retrievedAssembly(@RequestBody DhrRequest dhrRequest)
    {
        try {
            Assembly retrievedList = dhrService.retrieveAssemblyByPcy(dhrRequest.getSite(), dhrRequest.getPcu());
            return retrievedList;
        }catch (DhrException dhrException) {
            throw dhrException;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }


}
