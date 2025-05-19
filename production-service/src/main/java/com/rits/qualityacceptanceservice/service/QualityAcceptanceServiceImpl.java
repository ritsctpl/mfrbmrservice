package com.rits.qualityacceptanceservice.service;

import com.rits.qualityacceptanceservice.dto.QualityAcceptanceRequest;
import com.rits.qualityacceptanceservice.exception.QualityAcceptanceException;
import com.rits.qualityacceptanceservice.model.*;
import com.rits.qualityacceptanceservice.repository.QualityAcceptanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualityAcceptanceServiceImpl implements QualityAcceptanceService {

    private final QualityAcceptanceRepository qualityAcceptanceRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    @Override
    public MessageModel create(QualityAcceptanceRequest request) {
        String handle = createHandle(request);
        QualityAcceptance existingQualityAcceptance = qualityAcceptanceRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingQualityAcceptance != null) {
            throw new QualityAcceptanceException(9002, request.getBatchNo());
        }
        QualityAcceptance qualityAcceptance = qualityAcceptanceBuilder(request);
        qualityAcceptance.setHandle(handle);
        qualityAcceptance.setCreatedBy(request.getUser());
        qualityAcceptance.setCreatedDateTime(LocalDateTime.now());

        qualityAcceptanceRepository.save(qualityAcceptance);

        String createMessage = getFormattedMessage(1, request.getBatchNo());
        return MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).qualityAcceptance(qualityAcceptance).build();
    }

    private String createHandle(QualityAcceptanceRequest qualityAcceptanceRequest){
        validateRequest(qualityAcceptanceRequest);
        String handle = "Site:" + qualityAcceptanceRequest.getSite() + "," + "Batchno:" + qualityAcceptanceRequest.getBatchNo() + "," + "Recipe:" + qualityAcceptanceRequest.getRecipe();
        return handle;
    }

    public boolean validateRequest(QualityAcceptanceRequest request){
        if(!StringUtils.hasText(request.getSite())){
            throw new QualityAcceptanceException(7001);
        }
        if(!StringUtils.hasText(request.getBatchNo())) {
            throw new QualityAcceptanceException(9001);
        }
        if(!StringUtils.hasText(request.getRecipe())) {
            throw new QualityAcceptanceException(9001);
        }
        return true;
    }

    private QualityAcceptance qualityAcceptanceBuilder(QualityAcceptanceRequest request) {
        QualityAcceptance qualityAcceptance = QualityAcceptance.builder()
                .site(request.getSite())
                .batchNo(request.getBatchNo())
                .phaseId(request.getPhaseId())
                .operation(request.getOperation())
                .resource(request.getResource())
                .orderNumber(request.getOrderNumber())
                .recipe(request.getRecipe())
                .recipeVersion(request.getRecipeVersion())
                .user(request.getUser())
                .active(1)
                .build();

        return qualityAcceptance;
    }

}
