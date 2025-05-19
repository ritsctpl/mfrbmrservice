package com.rits.toolnumberservice.service;


import com.rits.toolgroupservice.dto.ToolGroupRequest;
import com.rits.toolgroupservice.model.ToolGroup;
import com.rits.toolnumberservice.dto.*;
import com.rits.toolnumberservice.exception.ToolNumberException;
import com.rits.toolnumberservice.model.MessageDetails;
import com.rits.toolnumberservice.model.ToolNumber;
import com.rits.toolnumberservice.model.ToolNumberMessageModel;
import com.rits.toolnumberservice.repository.ToolNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ToolNumberServiceImpl implements ToolNumberService{
    private final ToolNumberRepository toolNumberRepository;

    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource localMessageSource;

    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;
    @Value("${toolgroup-service.url}/retrieve")
    private String retrieveToolGroup;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }


    @Override
    public ToolNumberMessageModel createToolNumber(ToolNumberRequest toolNumberRequest) throws Exception{

        long recordPresent = toolNumberRepository.countByToolNumberAndSiteAndActive(toolNumberRequest.getToolNumber(), toolNumberRequest.getSite(), 1);
        if (recordPresent > 0) {
            throw new ToolNumberException(1100,toolNumberRequest.getToolNumber());
        }
        ToolGroup retrievedToolGroupRecord = null;
        if(toolNumberRequest.getToolGroup()!=null && !toolNumberRequest.getToolGroup().isEmpty()) {
            ToolGroupRequest retrieveRequest = ToolGroupRequest.builder().site(toolNumberRequest.getSite()).toolGroup(toolNumberRequest.getToolGroup()).build();
            retrievedToolGroupRecord = retrieveToolGroup(retrieveRequest);
        }
        if(retrievedToolGroupRecord == null || retrievedToolGroupRecord.getToolGroup() == null)
        {
            throw new ToolNumberException(1106,toolNumberRequest.getToolGroup());
        }
        if(retrievedToolGroupRecord !=null && retrievedToolGroupRecord.getToolGroup()!=null){
            if(toolNumberRequest.getQtyAvailable() > retrievedToolGroupRecord.getToolQty())
            {
                throw new ToolNumberException(1104,toolNumberRequest.getToolGroup());
            }
        }
        if(toolNumberRequest.getDescription()==null || toolNumberRequest.getDescription().isEmpty())
        {
            toolNumberRequest.setDescription(toolNumberRequest.getToolNumber());
        }
        ToolNumber toolNumber= toolNumberbuilder(toolNumberRequest);ToolNumber.builder();
        toolNumber.setSite(toolNumberRequest.getSite());
        toolNumber.setToolNumber(toolNumberRequest.getToolNumber());
        toolNumber.setHandle("ToolNumberBO:" + toolNumberRequest.getSite() + "," + toolNumberRequest.getToolNumber());
        toolNumber.setToolGroup(toolNumberRequest.getToolGroup());
        if(toolNumberRequest.getCalibrationType().equalsIgnoreCase("count")) {
            toolNumber.setCurrentCalibrationCount(toolNumberRequest.getMaximumCalibrationCount() - toolNumberRequest.getCalibrationCount());
        }
        if(toolNumberRequest.getCalibrationType().equalsIgnoreCase("time")) {
           if(toolNumberRequest.getCalibrationPeriod().contains("Days"))
            {
                String remainingString = toolNumberRequest.getCalibrationPeriod().replace("Days", "");
                toolNumber.setExpirationDate(LocalDateTime.now().plusDays(Integer.parseInt(remainingString))+"");
            }
            if(toolNumberRequest.getCalibrationPeriod().contains("Months"))
            {
                String remainingString = toolNumberRequest.getCalibrationPeriod().replace("Months", "");
                toolNumber.setExpirationDate(LocalDateTime.now().plusMonths(Integer.parseInt(remainingString))+"");
            }
            if(toolNumberRequest.getCalibrationPeriod().contains("Years"))
            {
                String remainingString = toolNumberRequest.getCalibrationPeriod().replace("Years", "");
                toolNumber.setExpirationDate(LocalDateTime.now().plusYears(Integer.parseInt(remainingString))+"");
            }
        }
        toolNumber.setCreatedDateTime(LocalDateTime.now());
           toolNumber.setDurationExpiration(LocalDateTime.now().plusSeconds(toolNumberRequest.getDuration()));
        String createdMessage = getFormattedMessage(44, toolNumberRequest.getToolNumber());
        return ToolNumberMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(toolNumberRepository.save(toolNumber)).build();
    }

    public ToolGroup retrieveToolGroup(ToolGroupRequest toolGroupRequest)
    {
      ToolGroup  retrievedToolGroupRecord = webClientBuilder.build()
                .post()
                .uri(retrieveToolGroup)
                .bodyValue(toolGroupRequest)
                .retrieve()
                .bodyToMono(ToolGroup.class)
                .block();
      return retrievedToolGroupRecord;
    }

    @Override
    public ToolNumberMessageModel updateToolNumber(ToolNumberRequest toolNumberRequest) throws Exception {
        ToolNumber retrievedToolNumber = toolNumberRepository.findByToolNumberAndActiveAndSite(toolNumberRequest.getToolNumber(), 1,toolNumberRequest.getSite());
        if (retrievedToolNumber != null) {

            ToolGroup retrievedToolGroupRecord = null;
            if(toolNumberRequest.getToolGroup()!=null && !toolNumberRequest.getToolGroup().isEmpty()) {
                ToolGroupRequest retrieveRequest = ToolGroupRequest.builder().site(toolNumberRequest.getSite()).toolGroup(toolNumberRequest.getToolGroup()).build();
                retrievedToolGroupRecord = retrieveToolGroup(retrieveRequest);
            }
            if(retrievedToolGroupRecord !=null){
                if(toolNumberRequest.getQtyAvailable() > retrievedToolGroupRecord.getToolQty())
                {
                    throw new ToolNumberException(1104,toolNumberRequest.getToolGroup());
                }
            }
            if(toolNumberRequest.getDescription()==null || toolNumberRequest.getDescription().isEmpty())
            {
                toolNumberRequest.setDescription(toolNumberRequest.getToolNumber());
            }
           ToolNumber toolNumber = toolNumberbuilder(toolNumberRequest);
            toolNumber.setSite(retrievedToolNumber.getSite());
            toolNumber.setToolNumber(retrievedToolNumber.getToolNumber());
            toolNumber.setHandle(retrievedToolNumber.getHandle());
            toolNumber.setModifiedDateTime(LocalDateTime.now());
            toolNumber.setCreatedDateTime(retrievedToolNumber.getCreatedDateTime());
            if(toolNumberRequest.getCalibrationType().equalsIgnoreCase("count")) {
                toolNumber.setCurrentCalibrationCount(toolNumberRequest.getMaximumCalibrationCount() - toolNumberRequest.getCalibrationCount());
            }
            if(toolNumberRequest.getCalibrationType().equalsIgnoreCase("time")) {
                if(toolNumberRequest.getCalibrationPeriod().contains("Days"))
                {
                    String remainingString = toolNumberRequest.getCalibrationPeriod().replace("Days", "");
                    toolNumber.setExpirationDate(LocalDateTime.now().plusDays(Integer.parseInt(remainingString))+"");
                }
                if(toolNumberRequest.getCalibrationPeriod().contains("Months"))
                {
                    String remainingString = toolNumberRequest.getCalibrationPeriod().replace("Months", "");
                    toolNumber.setExpirationDate(LocalDateTime.now().plusMonths(Integer.parseInt(remainingString))+"");
                }
                if(toolNumberRequest.getCalibrationPeriod().contains("Years"))
                {
                    String remainingString = toolNumberRequest.getCalibrationPeriod().replace("Years", "");
                    toolNumber.setExpirationDate(LocalDateTime.now().plusYears(Integer.parseInt(remainingString))+"");
                }
            }
                toolNumber.setDurationExpiration(LocalDateTime.now().plusSeconds(toolNumberRequest.getDuration()));
            String createdMessage = getFormattedMessage(45, toolNumberRequest.getToolNumber());
            return ToolNumberMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(toolNumberRepository.save(toolNumber)).build();
        }
        throw new ToolNumberException(1101, toolNumberRequest.getToolNumber());
    }

    @Override
    public Boolean updateCurrentCount(ToolNumberRequest toolNumberRequest)
    {
        Boolean isUpdated = false;
        ToolNumber retrievedRecord = toolNumberRepository.findByToolNumberAndActiveAndSite(toolNumberRequest.getToolNumber(), 1,toolNumberRequest.getSite());
        if(retrievedRecord!=null && retrievedRecord.getToolNumber() != null) {
            retrievedRecord.setCurrentCount(retrievedRecord.getCurrentCount() + 1);
//            retrievedRecord.setQtyAvailable(retrievedRecord.getQtyAvailable() - toolNumberRequest.getToolQty());
            if(retrievedRecord.getCurrentCalibrationCount() != 0) {
                retrievedRecord.setCurrentCalibrationCount(retrievedRecord.getCurrentCalibrationCount() - 1);
            }
            retrievedRecord.setModifiedDateTime(LocalDateTime.now());
            toolNumberRepository.save(retrievedRecord);
            isUpdated = true;
        }
        return isUpdated;
    }

    public ToolNumber toolNumberbuilder(ToolNumberRequest toolNumberRequest)
    {
      ToolNumber toolNumber = ToolNumber.builder()
                .toolGroup(toolNumberRequest.getToolGroup())
                .qtyAvailable(toolNumberRequest.getQtyAvailable())
                .description(toolNumberRequest.getDescription())
                .status(toolNumberRequest.getStatus())
                .erpEquipmentNumber(toolNumberRequest.getErpEquipmentNumber())
                .erpPlanMaintenanceOrder(toolNumberRequest.getErpPlanMaintenanceOrder())
                .toolQty(toolNumberRequest.getToolQty())
                .location(toolNumberRequest.getLocation())
                .calibrationType(toolNumberRequest.getCalibrationType())
                .startCalibrationDate(toolNumberRequest.getStartCalibrationDate())
                .calibrationPeriod(toolNumberRequest.getCalibrationPeriod())
                .calibrationCount(toolNumberRequest.getCalibrationCount())
                .maximumCalibrationCount(toolNumberRequest.getMaximumCalibrationCount())
                .expirationDate(toolNumberRequest.getExpirationDate())
                .toolGroupSetting(toolNumberRequest.getToolGroupSetting())
                .measurementPointsList(toolNumberRequest.getMeasurementPointsList())
                .customDataList(toolNumberRequest.getCustomDataList())
                .duration(toolNumberRequest.getDuration())
                .active(1)
                .build();
      return toolNumber;
    }

    @Override
    public ToolNumberResponseList getToolNumberListByCreationDate(ToolNumberRequest toolNumberRequest) throws Exception {
        List<ToolNumberResponse> toolNumberResponses = toolNumberRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, toolNumberRequest.getSite());
//        List<ToolNumberResponse> enabledToolNumberResponses = new ArrayList<>();
//        for(ToolNumberResponse toolNumberResponse : toolNumberResponses)
//        {
//            if(toolNumberResponse.getStatus().equalsIgnoreCase("enabled"))
//            {
//                enabledToolNumberResponses.add(toolNumberResponse);
//            }
//        }
            return ToolNumberResponseList.builder().toolNumberList(toolNumberResponses).build();
    }

    @Override
    public ToolNumberResponseList getEnabledToolNumber(ToolNumberRequest toolNumberRequest) throws Exception {
        List<ToolNumberResponse> toolNumberResponses = toolNumberRepository.findByActiveAndSiteAndStatus(1, toolNumberRequest.getSite(),"Enabled");
        List<ToolNumberResponse> toolNumberResponse = toolNumberRepository.findByActiveAndSiteAndStatus(1, toolNumberRequest.getSite(),"Productive");
        toolNumberResponses.addAll(toolNumberResponse);
        return ToolNumberResponseList.builder().toolNumberList(toolNumberResponses).build();
    }


    @Override
    public ToolNumberResponseList getToolNumberList(ToolNumberRequest toolNumberRequest) throws Exception {
        if (toolNumberRequest.getToolNumber() == null || toolNumberRequest.getToolNumber().isEmpty()) {
            return  getToolNumberListByCreationDate(toolNumberRequest);
        } else {
            List<ToolNumberResponse> toolNumberResponseLists = toolNumberRepository.findByToolNumberContainingIgnoreCaseAndSiteAndActive(toolNumberRequest.getToolNumber(), toolNumberRequest.getSite(), 1);
            if (toolNumberResponseLists != null && !toolNumberResponseLists.isEmpty()) {
                return ToolNumberResponseList.builder().toolNumberList(toolNumberResponseLists).build();
            } else {
                throw new ToolNumberException(1101, toolNumberRequest.getToolNumber());
            }
        }
    }

    @Override
    public ToolNumber retrieveToolNumber(ToolNumberRequest toolNumberRequest) throws Exception {
        ToolNumber toolNumberList = toolNumberRepository.findByToolNumberAndSiteAndActive(toolNumberRequest.getToolNumber(), toolNumberRequest.getSite(), 1);
        if (toolNumberList != null ) {
            return toolNumberList;
        } else {
            throw new ToolNumberException(1101, toolNumberRequest.getToolNumber());
        }
    }

    @Override
    public List<ToolNumber> retrieveAllByToolGroup(String site, String toolGroup)throws Exception{
        List<ToolNumber> retrievedList = new ArrayList<>();
        if(toolGroup!=null && !toolGroup.isEmpty()) {
             retrievedList = toolNumberRepository.findBySiteAndActiveAndToolGroup(site, 1, toolGroup);
        }
            return retrievedList;
    }

    @Override
    public ToolNumberMessageModel deleteToolNumber(ToolNumberRequest toolNumberRequest) throws Exception {
        if (toolNumberRepository.existsByToolNumberAndSiteAndActive(toolNumberRequest.getToolNumber(), toolNumberRequest.getSite(), 1)) {
            ToolNumber existingToolNumber = toolNumberRepository.findByToolNumberAndActive(toolNumberRequest.getToolNumber(),1);
            existingToolNumber.setActive(0);
            existingToolNumber.setModifiedDateTime(LocalDateTime.now());
            toolNumberRepository.save(existingToolNumber);

            String createdMessage = getFormattedMessage(46, toolNumberRequest.getToolNumber());
            return ToolNumberMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).build();
        } else {
            throw new ToolNumberException(1101, toolNumberRequest.getToolNumber());
        }
    }

    @Override
    public Boolean isToolNumberExist(ToolNumberRequest toolNumberRequest) throws Exception {
      return toolNumberRepository.existsByToolNumberAndSiteAndActive(toolNumberRequest.getToolNumber(),toolNumberRequest.getSite() ,1);

    }

    @Override
    public MeasurementPointsResponseList getMeasurementPointsList(ToolNumberRequest toolNumberRequest) throws Exception {
        ToolNumber toolNumber = toolNumberRepository.findByToolNumberAndActiveAndSite(toolNumberRequest.getToolNumber(), 1, toolNumberRequest.getSite());
        if (toolNumber == null) {
            throw new ToolNumberException(1101, toolNumberRequest.getToolNumber());
        }
        else if (toolNumber.getMeasurementPointsList() != null && !toolNumber.getMeasurementPointsList().isEmpty()) {

            List<MeasurementPointsResponse> measurementPointsResponses = toolNumber.getMeasurementPointsList()
                    .stream()
                    .map(toolMeasurementPoints -> MeasurementPointsResponse.builder()
                            .measurementPoint(toolMeasurementPoints.getMeasurementPoint())
                            .build())
                    .collect(Collectors.toList());

            return MeasurementPointsResponseList.builder()
                    .measurementPointsList(measurementPointsResponses)
                    .build();
        } else {
            throw new ToolNumberException(1103, toolNumberRequest.getToolNumber());
        }
    }
}
