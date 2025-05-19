package com.rits.dccollect.controller;

import com.rits.dccollect.dto.*;
import com.rits.dccollect.exception.DcCollectException;
import com.rits.dccollect.model.DcCollectMessageModel;
import com.rits.dccollect.model.ParametricMeasures;
import com.rits.dccollect.service.DcCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/dccollect-service")
public class DcCollectController {

    private final DcCollectService dcCollectService;

    @PostMapping("getDcCollectGroupList")
    @ResponseStatus(HttpStatus.OK)
    public List<DcGroupList> getDataCollectionGroupList(@RequestBody DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.getDataCollectionGroupList(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getParameterList")
    @ResponseStatus(HttpStatus.OK)
    public List<Parameter> getParameterList(@RequestBody DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.getParameterList(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//change method name to logDc
    @PostMapping("save")
    @ResponseStatus(HttpStatus.OK)
    public DcCollectMessageModel logDc(@RequestBody List<DcCollectRequest> dcCollectRequest)
    {
        try {
            return dcCollectService.logDc(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveByDcGroupAndVersion")
    @ResponseStatus(HttpStatus.OK)
    public  List<DcSaveParametricMeasures> retrieveByDcGroupAndVersion(@RequestBody DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.retrieveByDcGroupAndVersion(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("saveDraft")
    @ResponseStatus(HttpStatus.OK)
    public MessageModel saveDraft(@RequestBody List<DcCollectRequest> dcCollectRequestList)
    {
        try {
            return dcCollectService.saveDraft(dcCollectRequestList);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("isExists")
    public Boolean isExists(@RequestBody DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.isExists(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("parametricValidation")
    @ResponseStatus(HttpStatus.OK)
    public Boolean parameterValidation(@RequestBody DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.parameterValidation(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieve")
    @ResponseStatus(HttpStatus.OK)
    public List<DcSaveParametricMeasures> retrieve(@RequestBody DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.retrieve(dcCollectRequest.getSite(),dcCollectRequest.getPcu(),dcCollectRequest.getDataCollection(),dcCollectRequest.getVersion());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrievePreSaved")
    @ResponseStatus(HttpStatus.OK)
    public DcParametricPreSave retrievePreSaved (@RequestBody DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.retrievePreSaved(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByDcGroupName")
    @ResponseStatus(HttpStatus.OK)
    public DcGroupResponse findDcGroupNameByName(DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.findDcGroupNameByName(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByResource")
    @ResponseStatus(HttpStatus.OK)
    public List<DcGroupResponse> findDcGroupNameByResource(DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.findDcGroupNameByResource(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveCurrentDataCollection")
    @ResponseStatus(HttpStatus.OK)
    public List<DcGroupResponse> getCurrentDataCollection(DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.getCurrentDataCollection(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByPcuForDataCollection")
    @ResponseStatus(HttpStatus.OK)
    public List<ParametricMeasures> getCollectedDataListByPcu(@RequestBody DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.retrieveForDataCollection(dcCollectRequest.getSite(),dcCollectRequest.getPcu());
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAll")
    @ResponseStatus(HttpStatus.OK)
    public List<DcGroupResponse> getAllDataCollection(DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.getAllDataCollection(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveDcGroupByName")
    @ResponseStatus(HttpStatus.OK)
    public DcGroupResponse findCurrentDcGroupByName(DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.findCurrentDcGroupByName(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveDcGroupByNameAndVersion")
    @ResponseStatus(HttpStatus.OK)
    public DcGroupResponse findDcGroupByNameAndVersion(DcCollectRequest dcCollectRequest)
    {
        try {
            return dcCollectService.findDcGroupByNameAndVersion(dcCollectRequest);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
