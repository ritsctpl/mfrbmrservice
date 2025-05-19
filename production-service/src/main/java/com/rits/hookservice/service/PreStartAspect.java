package com.rits.hookservice.service;

import com.rits.startservice.dto.StartRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.rits.startservice.dto.StartRequestList;

@Aspect
@Component
@RequiredArgsConstructor
public class PreStartAspect {
    private final WebClient.Builder webClientBuilder;
    private static final Logger logger = LoggerFactory.getLogger(PreStartAspect.class);
    @Before("execution(* com.rits.startservice.controller.StartController.start(com.rits.startservice.dto.StartRequestList))")
    public void beforeStart(JoinPoint joinPoint) {
        // Add custom pre-start logic here
        // Access the PcustartRequest argument if needed
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof StartRequestList) {
            StartRequestList pcustartRequestList = (StartRequestList) args[0];

           /*for (StartRequest request : pcustartRequestList.getRequestList()) {
                // Modify request properties as needed
                request.setSite("RITS1");
            }*/

            StartRequestList copy = pcustartRequestList.deepCopy();

            // Modify the copy here if needed
            for (StartRequest request : copy.getRequestList()) {
                // Modify request properties as needed
                request.setSite("RITS1");
            }
            // Now you have access to the pcustartRequest object
            logger.info("Went inside ---"+ pcustartRequestList.getRequestList().get(0).getPcuBO()+"  ----");
        }
    }

}
