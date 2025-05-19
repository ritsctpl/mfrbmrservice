package com.rits.batchnohold.controller;

import com.rits.batchnohold.dto.BatchNoHoldRequest;
import com.rits.batchnohold.exception.BatchNoHoldException;
import com.rits.batchnohold.model.BatchNoHoldMessageModel;
import com.rits.batchnohold.service.BatchNoHoldService;
import com.rits.batchnoinwork.exception.BatchNoInWorkException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/batchnohold-service")
public class BatchNoHoldContoller {

    private final BatchNoHoldService batchNoHoldService;
    @PostMapping("hold")
    public BatchNoHoldMessageModel hold(@RequestBody BatchNoHoldRequest batchNoHoldRequest)
    {
        if(StringUtils.isEmpty(batchNoHoldRequest.getSite()))
            throw new BatchNoHoldException(113);

        try {
            return batchNoHoldService.hold(batchNoHoldRequest);
        }catch(BatchNoHoldException e){
            throw  e;
        } catch(BatchNoInWorkException e){
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("unhold")
    public BatchNoHoldMessageModel unHold(@RequestBody BatchNoHoldRequest batchNoHoldRequest)
    {
        if(StringUtils.isEmpty(batchNoHoldRequest.getSite()))
            throw new BatchNoHoldException(113);
        try {
            return batchNoHoldService.unhold(batchNoHoldRequest);
        } catch(BatchNoHoldException e){
            throw e;
        } catch(BatchNoInWorkException e){
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
