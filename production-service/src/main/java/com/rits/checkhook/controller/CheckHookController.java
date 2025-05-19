package com.rits.checkhook.controller;

import com.rits.checkhook.dto.MessageModel;
import com.rits.checkhook.exception.CheckHookException;
import com.rits.checkhook.service.CheckHookService;
import com.rits.dccollect.exception.DcCollectException;
import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import com.rits.pcucompleteservice.dto.PcuCompleteRequestInfo;
import com.rits.pcucompleteservice.dto.RequestList;
import com.rits.signoffservice.dto.SignOffRequestList;
import com.rits.startservice.dto.StartRequestList;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
@Aspect
@Component
@RequiredArgsConstructor
@RestController
@RequestMapping("app/v1/checkhook-service")
public class CheckHookController {

    private final WebClient.Builder webClientBuilder;
    private final CheckHookService checkHookService;
    private static final Logger logger = LoggerFactory.getLogger(com.rits.hookservice.service.PreStartAspect.class);

    @Before("execution(* com.rits.pcucompleteservice.controller.PcuCompleteController.complete(com.rits.pcucompleteservice.dto.RequestList))")
    public void  beforeComplete(JoinPoint joinPoint) throws Exception {
        // Add custom pre-start logic here
        // Access the PcustartRequest argument if needed
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof RequestList) {
            RequestList pcuRequestList = (RequestList) args[0];

           /*for (StartRequest request : pcustartRequestList.getRequestList()) {
                // Modify request properties as needed
                request.setSite("RITS1");
            }*/

            try {
                RequestList requestList = checkHookService.completeCheckHook(pcuRequestList);

                //   boolean flag = checkHookService.completeCheckHooks(pcuRequestList);
            } catch (CheckHookException checkHookException) {
                throw checkHookException;
            } catch (Exception e) {
                throw e;
            }
            // Now you have access to the pcustartRequest object
//            logger.info("Went inside complete ---" + pcuRequestList.getRequestList().get(0).getPcuBO() + "  ----");
            logger.info("Went inside complete ---" + "  ----");
        }
    }

    @Before("execution(* com.rits.startservice.controller.StartController.start(com.rits.startservice.dto.StartRequestList))")
    public void  beforeStart(JoinPoint joinPoint) throws Exception {
        // Add custom pre-start logic here
        // Access the PcustartRequest argument if needed
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof StartRequestList) {
            StartRequestList pcuRequestList = (StartRequestList) args[0];


            try {
                boolean flag = checkHookService.startCheckHook(pcuRequestList);
            }catch (CheckHookException checkHookException){
                throw checkHookException;
            } catch(Exception  e){
                throw e;
            }
            // Now you have access to the pcustartRequest object
            logger.info("Went inside start ---" + pcuRequestList.getRequestListWithoutBO().get(0).getPcu() + "  ----");
        }
    }
    @Before("execution(* com.rits.signoffservice.controller.SignOffController.signOff(com.rits.signoffservice.dto.SignOffRequestList))")
    public void  beforeSignOff(JoinPoint joinPoint) throws Exception {
        // Add custom pre-start logic here
        // Access the PcustartRequest argument if needed
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof SignOffRequestList) {
            SignOffRequestList pcuRequestList = (SignOffRequestList) args[0];


            try {
                boolean flag = checkHookService.signOffCheckHook(pcuRequestList);
            }catch (CheckHookException checkHookException){
                throw checkHookException;
            } catch(Exception  e){
                throw e;
            }
            // Now you have access to the pcustartRequest object
            logger.info("Went inside signOff---" + pcuRequestList.getRequestList().get(0).getPcuBO() + "  ----");
        }
    }

    @PostMapping("dcCollectHook")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageModel> dcCollectCheck (@RequestBody PcuCompleteReq pcuCompleteReq)
    {
        try {
            MessageModel messageModel=checkHookService.isMandatoryDataCollected(pcuCompleteReq);
            return ResponseEntity.ok(messageModel);
        } catch (CheckHookException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
    @PostMapping("assemblyHook")
    @ResponseStatus(HttpStatus.OK)
    public MessageModel assemblyCheckHook (@RequestBody PcuCompleteReq pcuCompleteReq)
    {
        try {
            return checkHookService.isAllComponentAssembled(pcuCompleteReq);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("ncHook")
    @ResponseStatus(HttpStatus.OK)
    public MessageModel ncHook(@RequestBody PcuCompleteReq pcuCompleteReq)
    {
        try {
            return checkHookService.isAllNcClosed(pcuCompleteReq);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("buyOffHook")
    @ResponseStatus(HttpStatus.OK)
    public MessageModel buyOffHook(@RequestBody PcuCompleteReq pcuCompleteReq)
    {
        try {
            return checkHookService.isAllBuyOffApproved(pcuCompleteReq);
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("hookUserValidation")
    @ResponseStatus(HttpStatus.OK)
    public MessageModel userCertificationHook(@RequestBody PcuCompleteRequestInfo pcuCompleteReqWithBO)
    {
        try {
//            return checkHookService.hookUserValidation(user, operation, site);
            return checkHookService.userCertificationHook(pcuCompleteReqWithBO.getUserBO(), pcuCompleteReqWithBO.getOperationBO(), pcuCompleteReqWithBO.getSite());
        } catch (DcCollectException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}