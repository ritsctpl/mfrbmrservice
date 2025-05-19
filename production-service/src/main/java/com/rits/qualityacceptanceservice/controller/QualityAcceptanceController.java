package com.rits.qualityacceptanceservice.controller;


import com.rits.qualityacceptanceservice.dto.QualityAcceptanceRequest;
import com.rits.qualityacceptanceservice.exception.QualityAcceptanceException;
import com.rits.qualityacceptanceservice.model.MessageModel;
import com.rits.qualityacceptanceservice.service.QualityAcceptanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/qualityacceptance-service")
public class QualityAcceptanceController {
    private final QualityAcceptanceService qualityAcceptanceService;
//
    @PostMapping("approve")
    public MessageModel create(@RequestBody QualityAcceptanceRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return qualityAcceptanceService.create(request);
            } catch (QualityAcceptanceException qualityAcceptanceException) {
                throw qualityAcceptanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new QualityAcceptanceException(7001);
    }

    @PostMapping("reject")
    public MessageModel update(@RequestBody QualityAcceptanceRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return qualityAcceptanceService.create(request);
            } catch (QualityAcceptanceException qualityAcceptanceException) {
                throw qualityAcceptanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new QualityAcceptanceException(7001);
    }
}

