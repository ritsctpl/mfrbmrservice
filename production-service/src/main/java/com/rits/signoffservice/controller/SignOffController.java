package com.rits.signoffservice.controller;

import com.rits.signoffservice.dto.SignOffRequestList;
import com.rits.signoffservice.dto.SignOffRequestListDetails;
import com.rits.signoffservice.exception.SignOffException;
import com.rits.signoffservice.model.MessageModel;
import com.rits.signoffservice.service.SignOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/signoff-service")
public class SignOffController {
    private final SignOffService signOffService;

    @PostMapping("/signoff")
    @ResponseStatus(HttpStatus.OK)
    public MessageModel signOff(@RequestBody SignOffRequestListDetails signOffRequestListNoBO)
    {
        try {
            SignOffRequestList signOffRequestList = signOffService.convertToSignOffRequestList(signOffRequestListNoBO);
            return signOffService.signOff(signOffRequestList);
        } catch (SignOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
