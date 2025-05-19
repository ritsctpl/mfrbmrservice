package com.rits.bmrservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.bmrservice.dto.*;
import com.rits.bmrservice.dto.BomPhaseSeperation;
import com.rits.bmrservice.dto.DosAndDonts;
import com.rits.bmrservice.dto.MFRResponse;
import com.rits.bmrservice.dto.MessageDetails;
import com.rits.bmrservice.dto.MfrFullResponse;
import com.rits.bmrservice.exception.BmrException;
import com.rits.bmrservice.repository.BMRRepository;
import com.rits.dataFieldService.model.DataField;
import com.rits.dataFieldService.model.ListDetails;
import com.rits.mfrrecipesservice.dto.*;
import com.rits.mfrrecipesservice.exception.MfrRecipesException;
import com.rits.mfrscreenconfigurationservice.dto.MfrScreenConfigurationRequest;
import com.rits.mfrscreenconfigurationservice.model.MFRRefList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class BmrServicesImpl implements BmrServices {

    private final WebClient.Builder webClientBuilder;
    private final BMRRepository bmrRepository;
    private final MessageSource localMessageSource;
//    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mfrrecipe-service.url}/getSelectiveMfrData")
    private String mfrUrl;

    @Value("${mfrscreenconfiguration-service.url}/retrieve")
    private String mfrConfigUrl;

    @Value("${datafield-service.url}/retrieve")
    private String datafieldUrl;

    @Value("${nextnumbergenerator-service.url}/getNewInventory")
    private String newNxtNumberUrl;

    @Value("${nextnumbergenerator-service.url}/getAndUpdateCurrentSequence")
    private String updateNextNumberUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public BmrMessageModel createBmr(BmrRecipes bmrRequest) throws Exception{

        BmrRecipes bmrRecipes = bmrRepository.findByBmrNoAndVersionAndActiveAndSite(bmrRequest.getBmrNo(), bmrRequest.getVersion(), 1,bmrRequest.getSite());
        if (bmrRecipes!=null) {//update
            bmrRecipes.setSite(bmrRequest.getSite());
            bmrRecipes.setBmrNo(bmrRequest.getBmrNo());
            bmrRecipes.setVersion(bmrRequest.getVersion());
            bmrRecipes.setProductName(bmrRequest.getProductName());
            bmrRecipes.setConfiguration(bmrRequest.getConfiguration());
            bmrRecipes.setSections(bmrRequest.getSections());
            bmrRecipes.setHeaderDetails(bmrRequest.getHeaderDetails());
            bmrRecipes.setFooterDetails(bmrRequest.getFooterDetails());
            bmrRecipes.setActive(bmrRequest.getActive());
            bmrRecipes.setCreatedBy(bmrRequest.getCreatedBy());
            bmrRecipes.setModifiedBy(bmrRequest.getModifiedBy());
            bmrRecipes.setCreatedDateTime(LocalDateTime.now());
            bmrRecipes.setModifiedDateTime(LocalDateTime.now());
            bmrRecipes.setType(bmrRequest.getType());

            String createdMessage = getFormattedMessage(2, bmrRequest.getBmrNo());
            return BmrMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(bmrRepository.save(bmrRecipes)).build();
        }else{//create
            String createdMessage = getFormattedMessage(1, bmrRequest.getBmrNo());
            bmrRequest.setHandle("BmrNO" + ":" + bmrRequest.getSite() + "," + bmrRequest.getBmrNo() + "," + bmrRequest.getVersion());
            bmrRequest.setActive(1);

            return BmrMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(bmrRepository.save(bmrRequest)).build();
        }
    }

    @Override
    public BmrMessageModel retrieve(BmrRecipes bmrRequest) throws Exception{
        if(!bmrRequest.getBmrNo().isEmpty()&&bmrRequest.getBmrNo()!=null && !bmrRequest.getVersion().isEmpty()&&bmrRequest.getVersion()!=null) {
            boolean bomIsExist = bmrRepository.existsByBmrNoAndVersionAndActiveAndSite(bmrRequest.getBmrNo(), bmrRequest.getVersion(), 1, bmrRequest.getSite());
            if (!bomIsExist) {
//                throw new MfrRecipesException(10002, bmrRequest.getBmrNo());
                return BmrMessageModel.builder().message_details(new MessageDetails("something went wrong", "E")).build();
            } else {
                BmrRecipes bmrRes = bmrRepository.findByBmrNoAndVersionAndActiveAndSite(bmrRequest.getBmrNo(), bmrRequest.getVersion(), 1, bmrRequest.getSite());
                return BmrMessageModel.builder().message_details(new MessageDetails("BMR retrieved", "S")).response(bmrRes).build();
            }
        }
        return BmrMessageModel.builder().message_details(new MessageDetails("BMR no or BMR version is missing", "E")).response(bmrRepository.save(bmrRequest)).build();
    }
    @Override
    public BMRResponseList retrieveAll(BmrRecipes bmrRequest) throws Exception{

        List<BMRResponse> bmrResponse = bmrRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, bmrRequest.getSite());
        return BMRResponseList.builder().bmrList(bmrResponse).build();
    }

    @Override
    public BmrMessageModel bmrPopulate(BmrRequest bmrRequest) throws Exception {

        List<String> dataFieldList = new ArrayList<>();
        List<ListDetails> headerDataFieldList = new ArrayList<>();
        BmrMessageModel bmrMessageModel = new BmrMessageModel();
        MfrFullResponse mfrRecipes = new MfrFullResponse();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Map<String, Object> recipeProductDetails = new HashMap<>();
        Map<String, Object> recipeHeaderDetails = new HashMap<>();
        String productName = "";
        double resMfr = 0.0;
        double resBmr = 0.0;


        BmrRecipes bmrRecipesResponse = bmrRepository.findByBmrNoAndVersionAndActiveAndSite(bmrRequest.getBmrNo(), bmrRequest.getVersion(), 1,bmrRequest.getSite());
        if(bmrRecipesResponse!=null) {
            throw new BmrException(11002, bmrRequest.getBmrNo());
        }

        MfrScreenConfigurationRequest mfrScreenConfigurationRequest = MfrScreenConfigurationRequest.builder().site(bmrRequest.getSite()).productName(bmrRequest.getConfiguration()).build();
        MFRScreenConfiguration mfrScreenConfiguration = webClientBuilder
                .build()
                .post()
                .uri(mfrConfigUrl)
                .body(BodyInserters.fromValue(mfrScreenConfigurationRequest))
                .retrieve()
                .bodyToMono(MFRScreenConfiguration.class)
                .block();

        DataFieldRequest dataFieldRequest = DataFieldRequest.builder().dataField(mfrScreenConfiguration.getProduct().getHeader().getReference()).site(bmrRequest.getSite()).build();
        DataField datFieldResponse = webClientBuilder
                .build()
                .post()
                .uri(datafieldUrl)
                .body(BodyInserters.fromValue(dataFieldRequest))
                .retrieve()
                .bodyToMono(DataField.class)
                .block();

        headerDataFieldList = datFieldResponse.getListDetails();
        for(ListDetails headerField : headerDataFieldList){
            dataFieldList.add(headerField.getFieldValue());
        }

        List<String> getTrueValueList = new ArrayList<>();
        for (MFRRefList ref : mfrScreenConfiguration.getMrfRefList()) {
            if (ref.getMfrRef().equals("true")) {
                getTrueValueList.add(ref.getDataField());
            }
        }


        if (!getTrueValueList.isEmpty()) {
            MFRResponse mfrRes = MFRResponse.builder().site(bmrRequest.getSite()).mfrNo(bmrRequest.getMfrNo()).version(bmrRequest.getMfrVersion()).dataFieldList(getTrueValueList).build();
            mfrRecipes = webClientBuilder
                    .build()
                    .post()
                    .uri(mfrUrl)
                    .body(BodyInserters.fromValue(mfrRes))
                    .retrieve()
                    .bodyToMono(MfrFullResponse.class)
                    .block();

            Map<String, List<Map<String, Object>>> sections = mfrRecipes.getMfrRecipes().getSections();

            if (sections != null) {
                if (sections.containsKey("productDetails")) {
                    List<Map<String, Object>> productDetails = sections.get("productDetails");
                    for (Map<String, Object> detail : productDetails) {
                        Map<String, Object> productData = (Map<String, Object>) detail.get("data");
                        productName = (String) productData.get("productName");
                        for (Map.Entry<String,Object> prod : productData.entrySet()) {
                            recipeProductDetails.put(prod.getKey(),prod.getValue());
                        }
                        recipeProductDetails.put("mfrNo",mfrRecipes.getMfrRecipes().getMfrNo());
                    }
                }
            }
            Map<String, String> headerData = mfrRecipes.getMfrRecipes().getHeaderDetails();
            for (Map.Entry<String,String> head : headerData.entrySet()) {
                recipeHeaderDetails.put(head.getKey(),head.getValue());
            }

        }

        bmrMessageModel.setMfrFullRecord(mfrRecipes.getMfrRecipes());

            if (!(bmrRequest.getBatchSize().isEmpty())) {
                String regex = "\\d+\\.?\\d*";

                Pattern pattern = Pattern.compile(regex);

                Matcher matcher = pattern.matcher(bmrRequest.getBatchSize());

                if (matcher.find()) {
                    String numericValue = matcher.group();
                    resBmr = Double.parseDouble(numericValue);
                }
            }

            if (mfrRecipes.getMfrRecipes() != null) {

                if (mfrRecipes.getMfrRecipes().getHeaderDetails() != null) {
                    if (mfrRecipes.getMfrRecipes().getHeaderDetails().containsKey("batchSize")) {
                        ;
                        String regex = "\\d+\\.?\\d*";

                        Pattern pattern = Pattern.compile(regex);

                        Matcher matcher = pattern.matcher(mfrRecipes.getMfrRecipes().getHeaderDetails().get("batchSize"));

                        if (matcher.find()) {
                            String numericValue = matcher.group();
                            resMfr = Double.parseDouble(numericValue);
                        }
                    }

                }
            }

        Map<String, Map<String, Object>> phaseSeparationMap = new HashMap<>();

        List<Map<String, Object>> briefManufacturingAndPackingProcedure = (List<Map<String, Object>>) mfrRecipes.getTrueDataFieldsRecord().get("briefManufacturingAndPackingProcedure");
        if (briefManufacturingAndPackingProcedure != null) {
            for (Map<String, Object> section : briefManufacturingAndPackingProcedure) {
                if ("Phase Separation".equals(section.get("title"))) {
                    List<Map<String, Object>> phaseDataList = (List<Map<String, Object>>) section.get("data");
                    for (Map<String, Object> phaseData : phaseDataList) {
                        String materialDescription = (String) phaseData.get("materialDescription");
                        phaseSeparationMap.put(materialDescription.trim(), phaseData);
                    }
                    break;
                }
            }
        }

        BmrRecipes bmr = new BmrRecipes();
        bmr.setHandle("BmrNO" + ":" + bmrRequest.getSite() + "," + bmrRequest.getBmrNo() + "," + bmrRequest.getVersion());
        bmr.setSite(bmrRequest.getSite());
        bmr.setBmrNo(bmrRequest.getBmrNo());
        bmr.setVersion(bmrRequest.getVersion());
        bmr.setProductName(productName);
        bmr.setConfiguration(bmrRequest.getConfiguration());
        bmr.setActive(1);
        bmr.setCreatedBy(bmrRequest.getCreatedBy());
        bmr.setCreatedDateTime(LocalDateTime.now());
        bmr.setModifiedDateTime(LocalDateTime.now());
        bmr.setType(bmrRequest.getConfigType());
        if (bmr.getSections() == null) {
            bmr.setSections(new HashMap<>());
        }

        Map<String, Object> trueDataFieldsRecord = mfrRecipes.getTrueDataFieldsRecord();
        List<Map<String, Object>> criticalControlPointsList = new ArrayList<>();
        List<Map<String, Object>> dosAndDontsList = new ArrayList<>();
        List<Map<String, Object>> productDetailList = new ArrayList<>();
        List<Map<String, Object>> manufacturingProcedureList = new ArrayList<>();
        List<Map<String, Object>> manufacturingList = new ArrayList<>();
        List<Map<String, Object>> rawMaterialIndentList = new ArrayList<>();
        List<Map<String, Object>> rawMaterialIndentsList = new ArrayList<>();
        String criticalControlTableId = "", dosAndDontsTableId = "", manufacturingTableId = "", rawMaterialIndentTableId = "", productDetailLabel = "";
        List<String> productDetailTableId = new ArrayList<>();
        List<String> extractedValues = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : bmrRequest.getReferenceId().entrySet()) {
            String category = entry.getKey();
            List<String> values = entry.getValue();

            if(category.equals("productDetails")){
                if(values.size()>1){
                    productDetailLabel = "formFieldIds";
                }else{
                    productDetailLabel = "tableId";
                }
                for (String value : values) {
                    String extractedValue = value.substring(value.indexOf('_') + 1);
                    extractedValues.add(extractedValue);
                    productDetailTableId.add(value);
                }
            }

            for (String value : values) {
                if (value.contains("critical_control_points")) {
                    criticalControlTableId = value;
                } else if (value.contains("general_dos_and_donts")) {
                    dosAndDontsTableId = value;
                } else if (value.contains("manufacturing")) {
                    manufacturingTableId = value;
                } else if (value.contains("raw_materials_indent")) {
                    rawMaterialIndentTableId = value;
                }
            }
        }


        Map<String, Object> productDetailsData = new HashMap<>();
        for (String key : extractedValues) {
            if (recipeProductDetails.containsKey(key)) {
                String value = (String) recipeProductDetails.get(key);
                productDetailsData.put(key, value);
            }
        }

        Map<String, Object> productDetails = new HashMap<>();
        productDetails.put("title", "Product Details");
        productDetails.put("data", productDetailsData);
        productDetails.put(productDetailLabel,productDetailTableId);
        productDetailList.add(productDetails);

        bmr.getSections().put("productDetails", productDetailList);

        for (Map.Entry<String, Object> entry : trueDataFieldsRecord.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "CRITICAL_CONTROL_POINTS":
                    if (value instanceof CriticalControlPoints) {
                        CriticalControlPoints controlPoints = (CriticalControlPoints) value;
                        Map<String, Object> ccpData = new HashMap<>();
                        ccpData.put("title", "Critical Control Points");
                        ccpData.put("data", controlPoints.getCriticalControlPointsData());
                        ccpData.put("tableId",criticalControlTableId);
                        criticalControlPointsList.add(ccpData);
                    } else if (value instanceof LinkedHashMap) {
                        LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) value;
                        CriticalControlPoints controlPoints = new CriticalControlPoints();
                        controlPoints.setCriticalControlPointsData((List<Map<String, Object>>) map.get("criticalControlPointsData"));
                        Map<String, Object> ccpData = new HashMap<>();
                        ccpData.put("title", "Critical Control Points");
                        ccpData.put("data", controlPoints.getCriticalControlPointsData());
                        ccpData.put("tableId",criticalControlTableId);
                        criticalControlPointsList.add(ccpData);
                    }
                    break;
                case "GENERAL_DOS_AND_DONTS":
                    if (value instanceof DosAndDonts) {
                        DosAndDonts dosAndDonts = (DosAndDonts) value;
                        Map<String, Object> dosAndDontsData = new HashMap<>();
                        dosAndDontsData.put("title", "General Dos and Don'ts");
                        dosAndDontsData.put("data", dosAndDonts.getDosAndDontsData());
                        dosAndDontsData.put("tableId",dosAndDontsTableId);
                        dosAndDontsList.add(dosAndDontsData);
                    } else if (value instanceof LinkedHashMap) {
                        LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) value;
                        DosAndDonts dosAndDonts = new DosAndDonts();
                        dosAndDonts.setDosAndDontsData((List<Map<String, Object>>) map.get("dosAndDontsData"));
                        Map<String, Object> dosAndDontsData = new HashMap<>();
                        dosAndDontsData.put("title", "General Dos and Don'ts");
                        dosAndDontsData.put("data", dosAndDonts.getDosAndDontsData());
                        dosAndDontsData.put("tableId",dosAndDontsTableId);
                        dosAndDontsList.add(dosAndDontsData);
                    }
                    break;
                case "MANUFACTURING":
                    if (value instanceof ManufacturingProcedure) {
                        ManufacturingProcedure manufacturingProcedure = (ManufacturingProcedure) value;
                        List<Map<String, Object>> manufacturingData = (List<Map<String, Object>>) manufacturingProcedure.getManufacturingData();
                        List<Map<String, Object>> updatedManufacturingData = new ArrayList<>();
                        Set<String> phaseSeparationComponents1 = getPhaseSeparationComponents(trueDataFieldsRecord);
                        for (Map<String, Object> phaseData : manufacturingData) {
                            phaseData.put("manufacturing", updateProcedureDescription(phaseData.get("manufacturing").toString(), resMfr, resBmr, phaseSeparationComponents1));
                            updatedManufacturingData.add(phaseData);
                        }
                        manufacturingProcedureList.addAll(updatedManufacturingData);
                    } else if (value instanceof LinkedHashMap) {
                        LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) value;
                        ManufacturingProcedure manufacturingProcedure = new ManufacturingProcedure();
                        manufacturingProcedure.setManufacturingData((List<Map<String, Object>>) map.get("manufacturingData"));
                        List<Map<String, Object>> manufacturingData = (List<Map<String, Object>>) manufacturingProcedure.getManufacturingData();
                        List<Map<String, Object>> updatedManufacturingData = new ArrayList<>();
                        Set<String> phaseSeparationComponents1 = getPhaseSeparationComponents(trueDataFieldsRecord);
                        for (Map<String, Object> phaseData : manufacturingData) {
                            String updatedDescription = updateProcedureDescription(
                                    phaseData.get("manufacturing").toString(), resMfr, resBmr, phaseSeparationComponents1
                            );
                            phaseData.put("manufacturing", updatedDescription);
                            updatedManufacturingData.add(phaseData);
                        }
                        manufacturingProcedureList.addAll(updatedManufacturingData);
                    }
                    Map<String, Object> manufacturingDataMap = new HashMap<>();
                    manufacturingDataMap.put("title", "Manufacturing");
                    manufacturingDataMap.put("data", manufacturingProcedureList);
                    manufacturingDataMap.put("tableId", manufacturingTableId);
                    manufacturingList.add(manufacturingDataMap);
                    break;
                case "RAW_MATERIALS_INDENT":
                    if (value instanceof BomPhaseSeperation) {
                        BomPhaseSeperation bomPhaseSeperation = (BomPhaseSeperation) value;
                        List<Map<String, Object>> phaseSeperationData = (List<Map<String, Object>>) bomPhaseSeperation.getPhaseSeperationData();
                        for (Map<String, Object> phaseData : phaseSeperationData) {
                            String materialDescription = (String) phaseData.get("materialDescription");

                            String grade = "";
                            String materialCode = "";
                            String uom = "";

                            List<Map<String, Object>> billOfMaterials = (List<Map<String, Object>>) mfrRecipes.getMfrRecipes().getSections().get("billOfMaterials");
                            if (billOfMaterials != null) {
                                for (Map<String, Object> bom : billOfMaterials) {
                                    List<Map<String, Object>> bomData = (List<Map<String, Object>>) bom.get("data");
                                    for (Map<String, Object> bomItem : bomData) {
                                        if (materialDescription.equals(bomItem.get("materialDescription"))) {
                                            grade = (String) bomItem.get("grade");
                                            materialCode = (String) bomItem.get("materialCode");
                                            uom = (String) bomItem.get("uom");
                                            break;
                                        }
                                    }
                                }
                            }

                            double quantity = Double.parseDouble(phaseData.get("quantity").toString());
                            double calculatedQuantity = (quantity / resMfr) * resBmr;

                            Map<String, Object> rawMaterialIndentRecord = new HashMap<>();
                            rawMaterialIndentRecord.put("phase", phaseData.get("phase"));
                            rawMaterialIndentRecord.put("materialCode", materialCode);
                            rawMaterialIndentRecord.put("materialDescription", materialDescription + " " + grade);
                            rawMaterialIndentRecord.put("uom", uom);
                            rawMaterialIndentRecord.put("standardQty", String.format("%.3f", calculatedQuantity));

                            rawMaterialIndentList.add(rawMaterialIndentRecord);
                        }
                    } else if (value instanceof LinkedHashMap) {
                        LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) value;
                        BomPhaseSeperation bomPhaseSeperation = new BomPhaseSeperation();
                        bomPhaseSeperation.setPhaseSeperationData(map.get("phaseSeperationData"));
                        List<Map<String, Object>> phaseSeperationData = (List<Map<String, Object>>) bomPhaseSeperation.getPhaseSeperationData();
                        for (Map<String, Object> phaseData : phaseSeperationData) {
                            String materialDescription = (String) phaseData.get("materialDescription");

                            String grade = "";
                            String materialCode = "";
                            String uom = "";

                            List<Map<String, Object>> billOfMaterials = mfrRecipes.getMfrRecipes().getSections().get("billOfMaterials");
                            if (billOfMaterials != null) {
                                for (Map<String, Object> bom : billOfMaterials) {
                                    List<Map<String, Object>> bomData = (List<Map<String, Object>>) bom.get("data");
                                    for (Map<String, Object> bomItem : bomData) {
                                        if (materialDescription.equals(bomItem.get("materialDescription"))) {
                                            grade = (String) bomItem.get("grade");
                                            materialCode = (String) bomItem.get("materialCode");
                                            uom = (String) bomItem.get("uom");
                                            break;
                                        }
                                    }
                                }
                            }

                            double quantity = Double.parseDouble(phaseData.get("quantity").toString());
                            double calculatedQuantity = (quantity / resMfr) * resBmr;

                            Map<String, Object> rawMaterialIndentRecord = new HashMap<>();
                            rawMaterialIndentRecord.put("phase", phaseData.get("phase"));
                            rawMaterialIndentRecord.put("materialCode", materialCode);
                            rawMaterialIndentRecord.put("materialDescription", materialDescription + " " + grade);
                            rawMaterialIndentRecord.put("uom", uom);
                            rawMaterialIndentRecord.put("standardQty", calculatedQuantity);

                            rawMaterialIndentList.add(rawMaterialIndentRecord);
                        }
                    }
                    Map<String, Object> rawMaterialIndent = new HashMap<>();
                    rawMaterialIndent.put("title", "Raw Material Indent");
                    rawMaterialIndent.put("data", rawMaterialIndentList);
                    rawMaterialIndent.put("tableId", rawMaterialIndentTableId);
                    rawMaterialIndentsList.add(rawMaterialIndent);
                    break;
                default:
                    break;
            }
        }

        bmr.getSections().put("criticalControlPoints", criticalControlPointsList);
        bmr.getSections().put("generalDos&Donts", dosAndDontsList);
        bmr.getSections().put("manufacturing", manufacturingList);
        bmr.getSections().put("rawMaterialIndent", rawMaterialIndentsList);

        Map<String, String> headerDetails1 = new HashMap<>();
        for (String key : dataFieldList) {
            if (recipeHeaderDetails.containsKey(key)) {
                String value = (String) recipeHeaderDetails.get(key);
                headerDetails1.put(key, value);
            }
        }

        bmr.setHeaderDetails(headerDetails1);

        List<Map<String, String>> footerDetailsList = mfrRecipes.getMfrRecipes().getFooterDetails();

        bmr.setFooterDetails(footerDetailsList);

        createBmr(bmr);

        bmrMessageModel.setMrfRefList(mfrScreenConfiguration.getMrfRefList());
        bmrMessageModel.setBmrFullRecord(bmr);
        bmrMessageModel.setReferenceId(bmrRequest.getReferenceId());

        String createdMessage = getFormattedMessage(1, bmrRequest.getBmrNo());
        bmrMessageModel.setMessage_details(new MessageDetails(createdMessage, "S"));

        return bmrMessageModel;
    }
    private Set<String> getPhaseSeparationComponents(Map<String, Object> trueDataFieldsRecord) {
        Set<String> phaseSeparationComponents = new HashSet<>();

        Object value = trueDataFieldsRecord.get("RAW_MATERIALS_INDENT");

        if (value instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) value;

            BomPhaseSeperation bomPhaseSeperation = new BomPhaseSeperation();
            bomPhaseSeperation.setPhaseSeperationData(map.get("phaseSeperationData"));

            List<Map<String, Object>> phaseSeperationData = (List<Map<String, Object>>) bomPhaseSeperation.getPhaseSeperationData();
            for (Map<String, Object> phaseData : phaseSeperationData) {
                phaseSeparationComponents.add((String) phaseData.get("materialDescription"));
            }
        } else if (value instanceof BomPhaseSeperation) {
            BomPhaseSeperation bomPhaseSeperation = (BomPhaseSeperation) value;
            List<Map<String, Object>> phaseSeperationData = (List<Map<String, Object>>) bomPhaseSeperation.getPhaseSeperationData();
            for (Map<String, Object> phaseData : phaseSeperationData) {
                phaseSeparationComponents.add((String) phaseData.get("materialDescription"));
            }
        }

        return phaseSeparationComponents;
    }
    private static String updateProcedureDescription(String procedureDescription, double resMfr, double resBmr, Set<String> phaseSeparationComponents) {
        String newProDesc = procedureDescription;

        for (String component : phaseSeparationComponents) {
            String patternStr = "(?i)" + Pattern.quote(component) + "\\s*\\(\\s*(\\d+\\.?\\d*)\\s*%\\s*\\)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(newProDesc);

            while (matcher.find()) {
                String oldQtyStr = matcher.group(1);
                double oldQty = Double.parseDouble(oldQtyStr);
                double newQty = (oldQty / resMfr) * resBmr;
                String newQtyStr = String.format("%.3f", newQty);

                newProDesc = newProDesc.replaceFirst(Pattern.quote(matcher.group(0)), Matcher.quoteReplacement(component + " (" + newQtyStr + "%)"));
            }
        }
        return newProDesc;
    }

    @Override
    public BMRResponse getNewBmr(String type, String site) throws Exception {

        InventoryNextNumberRequest inventoryNextNumberRequest = InventoryNextNumberRequest.builder()
                .site(site)
                .size(1)
                .numberType(type)
                .build();

        List<InventoryList> newBmr = webClientBuilder
                .build()
                .post()
                .uri(newNxtNumberUrl)
                .body(BodyInserters.fromValue(inventoryNextNumberRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<InventoryList>>() {
                })
                .block();

        GeneratePrefixAndSuffixRequest nextNumberRequest = GeneratePrefixAndSuffixRequest.builder()
                .site(site)
                .currentSequence(newBmr.get(newBmr.size() - 1).getCurrentSequence())
                .numberType(type)
                .object("")
                .objectVersion("")
                .userBO("")
                .build();

        try {
            NextNumberMessageModel updateCurrentSequence = webClientBuilder.build()
                    .post()
                    .uri(updateNextNumberUrl)
                    .bodyValue(nextNumberRequest)
                    .retrieve()
                    .bodyToMono(NextNumberMessageModel.class)
                    .block();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        BMRResponse bmrResponse = new BMRResponse();
        bmrResponse.setNewNumber(newBmr.get(0).getInventory());
        return bmrResponse;
    }

    @Override
    public List<String> getBmrList(BmrRequest bmrRequest) throws Exception {
        List<String> bmrList = new ArrayList<>();

        boolean exists = bmrRepository.existsByBmrNoAndActiveAndSite(bmrRequest.getBmrNo(), 1, bmrRequest.getSite());
        if (!exists) {
            bmrList.add(bmrRequest.getMfrNo());
        }
        return bmrList;
    }

}