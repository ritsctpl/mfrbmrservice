package com.rits.reasoncodeservice.service;

import com.rits.reasoncodeservice.dto.ReasonCodeRequest;
import com.rits.reasoncodeservice.dto.ReasonCodeResponse;
import com.rits.reasoncodeservice.dto.ResponseList;
import com.rits.reasoncodeservice.exception.ReasonCodeException;
import com.rits.reasoncodeservice.model.MessageDetails;
import com.rits.reasoncodeservice.model.ReasonCode;
import com.rits.reasoncodeservice.model.ReasonCodeMessageModel;
import com.rits.reasoncodeservice.repository.ReasonCodeServiceRepository;
import com.rits.resourceservice.dto.Extension;
import com.rits.resourceservice.exception.ResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReasonCodeServiceImpl implements ReasonCodeService{
    private final ReasonCodeServiceRepository reasonCodeServiceRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;


    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public ReasonCodeMessageModel create(ReasonCodeRequest reasonCodeRequest)throws Exception
    {
        Boolean isExist = reasonCodeServiceRepository.existsBySiteAndActiveAndReasonCode(reasonCodeRequest.getSite(),1,reasonCodeRequest.getReasonCode());
        if(isExist)
        {
            throw new ReasonCodeException(6001, reasonCodeRequest.getReasonCode());
        }
        if(reasonCodeRequest.getDescription() == null || reasonCodeRequest.getDescription().isEmpty())
        {
            reasonCodeRequest.setDescription(reasonCodeRequest.getReasonCode());
        }
        ReasonCode newReasonCode = reasonCodeBuilder(reasonCodeRequest);
        newReasonCode.setHandle("ReasonCodeBO:"+reasonCodeRequest.getSite()+","+reasonCodeRequest.getReasonCode());
        newReasonCode.setCreatedBy(reasonCodeRequest.getUserId());
        newReasonCode.setCreatedDateTime(LocalDateTime.now());
        String createdMessage = getFormattedMessage(34, reasonCodeRequest.getReasonCode());
        return ReasonCodeMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(reasonCodeServiceRepository.save(newReasonCode)).build();
    }

    @Override
    public ReasonCodeMessageModel update(ReasonCodeRequest reasonCodeRequest)throws Exception
    {
        ReasonCode existingRecord = reasonCodeServiceRepository.findBySiteAndActiveAndReasonCode(reasonCodeRequest.getSite(),1,reasonCodeRequest.getReasonCode());
        if(existingRecord == null)
        {
            throw new ReasonCodeException(6002, reasonCodeRequest.getReasonCode());
        }
        if(reasonCodeRequest.getDescription() == null || reasonCodeRequest.getDescription().isEmpty())
        {
            reasonCodeRequest.setDescription(reasonCodeRequest.getReasonCode());
        }
        ReasonCode updatedReasonCode = reasonCodeBuilder(reasonCodeRequest);
        updatedReasonCode.setHandle(existingRecord.getHandle());
        updatedReasonCode.setReasonCode(existingRecord.getReasonCode());
        updatedReasonCode.setModifiedBy(reasonCodeRequest.getUserId());
        updatedReasonCode.setModifiedDateTime(LocalDateTime.now());
        String createdMessage = getFormattedMessage(35, reasonCodeRequest.getReasonCode());
        return ReasonCodeMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(reasonCodeServiceRepository.save(updatedReasonCode)).build();
    }


    @Override
    public ReasonCodeMessageModel delete(String site, String reasonCode,String userId)throws Exception
    {
        ReasonCode existingRecord = reasonCodeServiceRepository.findBySiteAndActiveAndReasonCode(site,1,reasonCode);
        if(existingRecord == null)
        {
            throw new ReasonCodeException(6002, reasonCode);
        }
        existingRecord.setModifiedDateTime(LocalDateTime.now());
        existingRecord.setModifiedBy(userId);
        existingRecord.setActive(0);
        String createdMessage = getFormattedMessage(36, reasonCode);
        return ReasonCodeMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(reasonCodeServiceRepository.save(existingRecord)).build();
    }

    @Override
    public ReasonCode retrieve(String site, String reasonCode)throws Exception
    {

        ReasonCode existingRecord = reasonCodeServiceRepository.findBySiteAndActiveAndReasonCode(site,1,reasonCode);
        if(existingRecord == null)
        {
            throw new ReasonCodeException(6002, reasonCode);
        }
        return existingRecord;
    }

    @Override
    public ResponseList retrieveAll(String site) throws Exception
    {
        List<ReasonCodeResponse> reasonCodeList = reasonCodeServiceRepository.findBySiteAndActive(site,1);
        return ResponseList.builder().reasonCodeResponseList(reasonCodeList).build();
    }

    @Override
    public ResponseList retrieveTop50(String site) throws Exception
    {
        List<ReasonCodeResponse> reasonCodeList = reasonCodeServiceRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1,site);
        return ResponseList.builder().reasonCodeResponseList(reasonCodeList).build();
    }

    public ReasonCode reasonCodeBuilder(ReasonCodeRequest reasonCodeRequest) throws Exception
    {
        ReasonCode reasonCode = ReasonCode.builder()
                .site(reasonCodeRequest.getSite())
                .reasonCode(reasonCodeRequest.getReasonCode())
                .description(reasonCodeRequest.getDescription())
                .messageType(reasonCodeRequest.getMessageType())
                .status(reasonCodeRequest.getStatus())
                .category(reasonCodeRequest.getCategory())
                .customDataList(reasonCodeRequest.getCustomDataList())
                .resource(reasonCodeRequest.getResource())
                .workCenter(reasonCodeRequest.getWorkCenter())
                .active(1)
                .build();
        return reasonCode;
    }

    @Override
    public String callExtension(Extension extension) {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new ResourceException(800);
        }
        return extensionResponse;
    }
}
