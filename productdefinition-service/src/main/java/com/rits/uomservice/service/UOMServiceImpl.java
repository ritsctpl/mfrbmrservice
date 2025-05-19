package com.rits.uomservice.service;

import com.rits.uomservice.dto.*;
import com.rits.uomservice.exception.UomException;
import com.rits.uomservice.model.MessageDetails;
import com.rits.uomservice.model.UOMEntity;
import com.rits.uomservice.model.UomConvertionEntity;
import com.rits.uomservice.repository.UOMRepository;
import com.rits.uomservice.repository.UomConvertionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UOMServiceImpl implements UOMService {
    @Autowired
    private UOMRepository uomRepository;
    private final UomConvertionRepository uomConvertionRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }



    @Override
    public UOMMessageModel createUOM(UOMRequest uomRequest) throws Exception {
        try {
            if (uomRepository.existsByActiveAndSiteAndUomCode(1, uomRequest.getSite(), uomRequest.getUomCode())) {
                return UOMMessageModel.builder().message_details(new MessageDetails("UOM already exists", "E")).build();
            }
            validateUOMRequest(uomRequest);
            UOMEntity uomEntity = createUOMEntity(uomRequest);
            String createdMessage = getFormattedMessage(51, uomRequest.getUomCode());
            return UOMMessageModel.builder()
                    .message_details(new MessageDetails(createdMessage, "S"))
                    .response(uomRepository.save(uomEntity))
                    .build();
        } catch (UomException uomException) {
            throw new UomException(uomException.getCode(), uomException.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public UOMMessageModel updateUOM(UOMRequest uomRequest) throws Exception {
        UOMEntity existingUOM = uomRepository.findBySiteAndId(uomRequest.getSite(), uomRequest.getId());

        if (existingUOM == null) {
            return UOMMessageModel.builder().message_details(new MessageDetails("value doesn't exists", "E")).build();
        }
        if (!existingUOM.getUomCode().equals(uomRequest.getUomCode()) &&
                uomRepository.existsByActiveAndSiteAndUomCode(1, uomRequest.getSite(), uomRequest.getUomCode())) {
            throw new UomException(102, uomRequest.getUomCode());
        }
        try {
            validateUOMRequest(uomRequest);
        } catch (UomException uomException) {
            throw uomException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        existingUOM.setConversionFactor(uomRequest.getConversionFactor());
        existingUOM.setDescription(uomRequest.getDescription());
        existingUOM.setStatus(uomRequest.getStatus());

        String updatedMessage = getFormattedMessage(52, uomRequest.getUomCode());

        return UOMMessageModel.builder()
                .message_details(new MessageDetails(updatedMessage, "S"))
                .response(uomRepository.save(existingUOM))
                .build();
    }


    @Override
    public UOMMessageModel deleteUOM(UOMRequest uomRequest) throws Exception {
        UOMEntity existingUOM = uomRepository.findByIdAndSiteAndActiveEquals(uomRequest.getId(), uomRequest.getSite(), 1);
        if (existingUOM == null) {
            return UOMMessageModel.builder().message_details(new MessageDetails("UOM ID is not found", "E")).build();
        }
        existingUOM.setActive(0);
        existingUOM.setModifiedDateTime(LocalDateTime.now());
        String deletedMessage = getFormattedMessage(53, uomRequest.getId());
        return UOMMessageModel.builder().message_details(new MessageDetails(deletedMessage, "S")).response(uomRepository.save(existingUOM)).build();
    }


    @Override
    public UOMEntity retrieveUOM(UOMRequest uomRequest) throws Exception {
        if (uomRequest == null) {
            throw new UomException(2602, "UOMRequest cannot be null.");
        }
        if (uomRequest.getSite() == null || uomRequest.getId() == null) {
            throw new UomException(2603, "Site and ID must not be null.");
        }
        UOMEntity existingUOM = uomRepository.findBySiteAndId(uomRequest.getSite(), uomRequest.getId());
        if (existingUOM == null) {
            throw new UomException(2601, "UOM ID " + uomRequest.getId() + " does not exist.");
        }

        return existingUOM;
    }



    @Override
    public UOMResponseList retrieveAll(UOMRequest uomRequest) throws Exception {
        List<UOMResponse> uomResponse = uomRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, uomRequest.getSite());
        return UOMResponseList.builder().uomList(uomResponse).build();
    }

    @Override
    public UOMResponseList retrieveTop50(String site) throws Exception {
        List<UOMResponse> top50UOM = uomRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return UOMResponseList.builder().uomList(top50UOM).build();
    }

    private void validateUOMRequest(UOMRequest uomRequest) throws Exception {
        if (uomRequest.getUomCode() == null || uomRequest.getUomCode().isBlank()) {
            throw new UomException(201, uomRequest.getUomCode());
        }
//        if (uomRequest.getConversionFactor() == null) {
//            throw new UomException(202, uomRequest.getUomCode());
//        }
        if ( !(uomRequest.getStatus().equals("Active") || uomRequest.getStatus().equals("Inactive"))) {
            throw new UomException(203, uomRequest.getUomCode());
        }
        if (uomRequest.getSite() == null || uomRequest.getSite().isBlank()) {
            throw new UomException(204, uomRequest.getUomCode());
        }

    }

    @Override
    public UOMMessageModel createBaseUnitConvertion(UOMRequest uomRequest) throws Exception {
        UomConvertionEntity uomConvertionRecord = uomConvertionRepository.findByBaseUnitAndConversionUnitAndActiveAndSite(uomRequest.getBaseUnit(), uomRequest.getConversionUnit(), 1, uomRequest.getSite());

        if(uomConvertionRecord != null)
            throw new UomException(156, uomRequest.getBaseUnit());

        UomConvertionEntity uomConvertion = null;
        if(StringUtils.hasText(uomRequest.getMaterial()) && StringUtils.hasText(uomRequest.getMaterialVersion())){
            uomConvertion = UomConvertionEntity.builder()
                    .site(uomRequest.getSite())
                    .handle("UomConvertionBO:"+uomRequest.getSite()+","+uomRequest.getBaseUnit()+","+uomRequest.getConversionUnit())
                    .baseUnit(uomRequest.getBaseUnit())
                    .baseAmt(uomRequest.getBaseAmt())
                    .convertionItem(uomRequest.getConvertionItem())
                    .conversionUnit(uomRequest.getConversionUnit())
                    .material(uomRequest.getMaterial())
                    .materialVersion(uomRequest.getMaterialVersion())
                    .active(1)
                    .cretedBy(uomRequest.getUser())
                    .createdDateTime(LocalDateTime.now())
                    .build();
        } else {
            uomConvertion = UomConvertionEntity.builder()
                    .site(uomRequest.getSite())
                    .handle("UomConvertionBO:"+uomRequest.getSite()+","+uomRequest.getBaseUnit()+","+uomRequest.getConversionUnit())
                    .baseUnit(uomRequest.getBaseUnit())
                    .baseAmt(uomRequest.getBaseAmt())
                    .convertionItem(uomRequest.getConvertionItem())
                    .conversionUnit(uomRequest.getConversionUnit())
                    .active(1)
                    .cretedBy(uomRequest.getUser())
                    .createdDateTime(LocalDateTime.now())
                    .build();
        }
        String createdMessage = getFormattedMessage(51, uomRequest.getBaseUnit());
        return UOMMessageModel.builder()
                .message_details(new MessageDetails(createdMessage, "S"))
                .uomConvertionResponse(uomConvertionRepository.save(uomConvertion))
                .build();
    }

    @Override
    public UOMMessageModel updateBaseUnitConvertion(UOMRequest uomRequest) throws Exception {
        UomConvertionEntity uomConvertionRecord = uomConvertionRepository.findByBaseUnitAndConversionUnitAndActiveAndSite(uomRequest.getBaseUnit(), uomRequest.getConversionUnit(), 1, uomRequest.getSite());

        if(uomConvertionRecord == null)
            throw new UomException(157, uomRequest.getBaseUnit());

        if(StringUtils.hasText(uomRequest.getMaterial()) && StringUtils.hasText(uomRequest.getMaterialVersion())){
            uomConvertionRecord.setBaseAmt(uomRequest.getBaseAmt());
            uomConvertionRecord.setConvertionItem(uomRequest.getConvertionItem());
            uomConvertionRecord.setMaterial(uomRequest.getMaterial());
            uomConvertionRecord.setMaterialVersion(uomRequest.getMaterialVersion());
            uomConvertionRecord.setModifiedBy(uomRequest.getUser());
            uomConvertionRecord.setModifiedDateTime(LocalDateTime.now());

        } else {
            uomConvertionRecord.setBaseAmt(uomRequest.getBaseAmt());
            uomConvertionRecord.setConvertionItem(uomRequest.getConvertionItem());
            uomConvertionRecord.setModifiedBy(uomRequest.getUser());
            uomConvertionRecord.setModifiedDateTime(LocalDateTime.now());
        }
        String updatedMessage = getFormattedMessage(52, uomRequest.getBaseUnit());
        return UOMMessageModel.builder()
                .message_details(new MessageDetails(updatedMessage, "S"))
                .uomConvertionResponse(uomConvertionRepository.save(uomConvertionRecord))
                .build();
    }

    @Override
    public UOMMessageModel deleteBaseUnitConvertion(UOMRequest uomRequest) throws Exception {
        UomConvertionEntity uomConvertionRecord = uomConvertionRepository.findByBaseUnitAndConversionUnitAndActiveAndSite(uomRequest.getBaseUnit(), uomRequest.getConversionUnit(), 1, uomRequest.getSite());

        if(uomConvertionRecord == null)
            throw new UomException(157, uomRequest.getBaseUnit());

        uomConvertionRecord.setActive(0);
        uomConvertionRecord.setModifiedDateTime(LocalDateTime.now());
        String deletedMessage = getFormattedMessage(53, uomRequest.getBaseUnit());

        return UOMMessageModel.builder()
                .message_details(new MessageDetails(deletedMessage, "S"))
                .uomConvertionResponse(uomConvertionRepository.save(uomConvertionRecord))
                .build();
    }

    @Override
    public UOMMessageModel retrieveBaseUnitConvertion(UOMRequest uomRequest) throws Exception {

        UomConvertionEntity uomConvertionRecord = uomConvertionRepository.findByBaseUnitAndConversionUnitAndActiveAndSite(uomRequest.getBaseUnit(), uomRequest.getConversionUnit(), 1, uomRequest.getSite());

        if(uomConvertionRecord == null)
            throw new UomException(157, uomRequest.getBaseUnit());

        return UOMMessageModel.builder()
                .uomConvertionResponse(uomConvertionRecord)
                .build();
    }

    @Override
    public UOMMessageModel retrieveAllBaseUnitConvertion(UOMRequest uomRequest) throws Exception {

        List<UomConvertionEntity> uomConvertionEntities = uomConvertionRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, uomRequest.getSite());
        List<UomConversionResponse> uomConversionResponses = new ArrayList<>();
        if (uomConvertionEntities == null || uomConvertionEntities.isEmpty()) {
            uomConversionResponses = new ArrayList<>();
        } else {
            uomConversionResponses = uomConvertionEntities.stream()
                    .map(entity -> {
                        UomConversionResponse response = new UomConversionResponse();
                        response.setBaseUnit(entity.getBaseUnit());
                        response.setConvertionItem(entity.getConvertionItem());
                        response.setBaseAmt(entity.getBaseAmt());
                        response.setConversionUnit(entity.getConversionUnit());
                        response.setMaterial(entity.getMaterial());
                        response.setMaterialVersion(entity.getMaterialVersion());
                        return response;
                    })
                    .collect(Collectors.toList());

        }
        return UOMMessageModel.builder()
                .uomConvertions(uomConversionResponses)
                .build();
    }

    @Override
    public UOMMessageModel retrieveTop50BaseUnitConvertion(UOMRequest uomRequest) throws Exception {

        List<UomConvertionEntity> uomConvertionEntities = uomConvertionRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, uomRequest.getSite());
        List<UomConversionResponse> uomConversionResponses = new ArrayList<>();
        if (uomConvertionEntities == null || uomConvertionEntities.isEmpty()) {
            uomConversionResponses = new ArrayList<>();
        } else {
            uomConversionResponses = uomConvertionEntities.stream()
                .map(entity -> {
                    UomConversionResponse response = new UomConversionResponse();
                    response.setBaseUnit(entity.getBaseUnit());
                    response.setConvertionItem(entity.getConvertionItem());
                    response.setBaseAmt(entity.getBaseAmt());
                    response.setConversionUnit(entity.getConversionUnit());
                    response.setMaterial(entity.getMaterial());
                    response.setMaterialVersion(entity.getMaterialVersion());
                    return response;
                })
                .collect(Collectors.toList());

        }
        return UOMMessageModel.builder()
                .uomConvertions(uomConversionResponses)
                .build();
    }

    @Override
    public String unitConvertion(UOMRequest uomRequest) throws Exception {
        if (StringUtils.hasText(uomRequest.getBaseUnit()) || StringUtils.hasText(uomRequest.getConversionUnit()) || StringUtils.hasText(String.valueOf(uomRequest.getGivenValue()))) {
            throw new UomException(160);
        }

        UomConvertionEntity uomRecord = null;
        if(StringUtils.hasText(uomRequest.getMaterial()))
            uomRecord = uomConvertionRepository.findByBaseUnitAndConversionUnitAndMaterialAndMaterialVersionAndActiveAndSite(uomRequest.getBaseUnit(), uomRequest.getConversionUnit(), uomRequest.getMaterial(), uomRequest.getMaterialVersion(), 1, uomRequest.getSite());
        else
            uomRecord = uomConvertionRepository.findByBaseUnitAndConversionUnitAndActiveAndSite(uomRequest.getBaseUnit(), uomRequest.getConversionUnit(), 1, uomRequest.getSite());

        if (uomRecord == null) {
            throw new UomException(158, uomRequest.getBaseUnit(), uomRequest.getConversionUnit());
        }

        Double givenValue = uomRequest.getGivenValue();

        String conversionSign = uomRecord.getConvertionItem();
        if (conversionSign == null || conversionSign.isEmpty()) {
            throw new UomException(161);
        }

        if (uomRecord.getBaseAmt() == null) {
            throw new UomException(162);
        }
        Double baseAmount = Double.valueOf(uomRecord.getBaseAmt());

        String conversionUnit = uomRecord.getConversionUnit();
        if (conversionUnit == null || conversionUnit.isEmpty()) {
            throw new UomException(163);
        }

        Double result;
        switch (conversionSign) {
            case "*":
            case "ratio":
                result = givenValue * baseAmount;
                break;
            case "/":
                result = givenValue / baseAmount;
                break;
            default:
                throw new UomException(151, conversionSign);
        }

        return result + " " + conversionUnit;
    }

    @Override
    public UOMMessageModel getUomByFilteredName(UOMRequest uomRequest) throws Exception {

        List<UomConvertionEntity> uomConvertionEntities;
        if(StringUtils.hasText(uomRequest.getBaseUnit())) {
            uomConvertionEntities = uomConvertionRepository.findByActiveAndSiteAndBaseUnitContainingIgnoreCase(1, uomRequest.getSite(), uomRequest.getBaseUnit());
        } else {
            uomConvertionEntities = uomConvertionRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, uomRequest.getSite());
        }

        List<UomConversionResponse> uomConversionResponses = new ArrayList<>();
        if (uomConvertionEntities == null || uomConvertionEntities.isEmpty()) {
            uomConversionResponses = new ArrayList<>();
        } else {
            uomConversionResponses = uomConvertionEntities.stream()
                .map(entity -> {
                    UomConversionResponse response = new UomConversionResponse();
                    response.setBaseUnit(entity.getBaseUnit());
                    response.setConvertionItem(entity.getConvertionItem());
                    response.setBaseAmt(entity.getBaseAmt());
                    response.setConversionUnit(entity.getConversionUnit());
                    response.setMaterial(entity.getMaterial());
                    response.setMaterialVersion(entity.getMaterialVersion());
                    return response;
                })
                .collect(Collectors.toList());
        }

        return UOMMessageModel.builder()
                .uomConvertions(uomConversionResponses)
                .build();
    }


    private UOMEntity createUOMEntity(UOMRequest uomRequest) {
        return UOMEntity.builder()
                .uomCode(uomRequest.getUomCode())
                .description(uomRequest.getDescription())
                .conversionFactor(uomRequest.getConversionFactor())
                .status(uomRequest.getStatus())
                .site(uomRequest.getSite())
                .active(1)
                .createdDateTime(LocalDateTime.now())
                .modifiedDateTime(LocalDateTime.now())
                .build();
    }
}
