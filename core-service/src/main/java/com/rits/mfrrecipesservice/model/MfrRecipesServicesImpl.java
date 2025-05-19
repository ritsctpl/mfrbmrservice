package com.rits.mfrrecipesservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.mfrrecipesservice.dto.*;
import com.rits.mfrrecipesservice.exception.MfrRecipesException;
import com.rits.mfrrecipesservice.repository.*;
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
public class MfrRecipesServicesImpl implements MfrRecipesServices {
    private final MFRRepository mfrRepository;
    private final BomRecipeRepository bomRecipeRepository;
    private final DosAndDontsRepository dosAndDontsRepository;
    private final ManuFactureProRepository manuFactureProRepository;
    private final CriticalControlsRepository criticalControlsRepository;
    private final MessageSource localMessageSource;
    private final WebClient.Builder webClientBuilder;

    @Value("${nextnumbergenerator-service.url}/generateNextNumbers")
    private String newNxtNumberUrl;

    @Value("${nextnumbergenerator-service.url}/getAndUpdateCurrentSequence")
    private String updateNextNumberUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public MfrMessageModel createMfr(RecordRequest recordRequest) throws Exception {

        MfrRecipes mfrGetResponse;

        boolean mfrRecordIsExist = mfrRepository.existsByMfrNoAndVersionAndActiveAndSite(recordRequest.getMfrNo(), recordRequest.getVersion(), 1, recordRequest.getSite());
        if (mfrRecordIsExist) {//update
                MfrRecipes mfrRecipeResponse = mfrRepository.findByMfrNoAndVersionAndSiteAndActive(recordRequest.getMfrNo(), recordRequest.getVersion(), recordRequest.getSite(), 1);
                recordRequest.setHandle(mfrRecipeResponse.getHandle());
                mfrGetResponse = createUpdateProcess(mfrRecipeResponse, recordRequest, recordRequest.getSections());

            String createdMessage = getFormattedMessage(2, recordRequest.getMfrNo());
            return MfrMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(mfrRepository.save(mfrGetResponse)).build();
        } else {// create
            recordRequest.setHandle("MfrNO" + ":" + recordRequest.getSite() + "," + recordRequest.getMfrNo() + "," + recordRequest.getVersion());
                mfrGetResponse = createUpdateProcess(new MfrRecipes(), recordRequest, recordRequest.getSections());

            String createdMessage = getFormattedMessage(1, recordRequest.getMfrNo());
            return MfrMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(mfrRepository.save(mfrGetResponse)).build();
        }

    }
    public MfrRecipes createUpdateProcess(MfrRecipes mfrRecipes, RecordRequest recordRequest, Map<String, List<Map<String, Object>>> sections)throws Exception{

        BomPhaseSeperation bomPhaseSeperationObj;
        DosAndDonts mfrDosAndDontsObj;
        CriticalControlPoints controlPointsObj;
        ManufacturingProcedure manufacturingProcedureObj;
        String maunafturingHandleNm = "", criticalControlNm = "", dosAndDontsNm = "", bomPhaseSepNm = "";


        if(sections.containsKey("briefManufacturingAndPackingProcedure")){
            
            List<Map<String, Object>> briefManufacturingRecord = sections.get("briefManufacturingAndPackingProcedure");
            if(!briefManufacturingRecord.isEmpty()){
                for(Map<String, Object> getEachManufacturing: briefManufacturingRecord){
                    if (getEachManufacturing.get("title").equals("Phase Separation")) {
                        bomPhaseSepNm = "MfrNO"+":"+recordRequest.getSite()+","+"RAW_MATERIALS_INDENT"+","+recordRequest.getMfrNo()+","+recordRequest.getVersion();
                        boolean phaseSeperationIsExist = bomRecipeRepository.existsByHandleAndSiteAndActive(bomPhaseSepNm, recordRequest.getSite(), 1);

                        if(phaseSeperationIsExist)
                            bomPhaseSeperationObj = bomRecipeRepository.findByHandleAndActiveAndSite(bomPhaseSepNm, 1, recordRequest.getSite());
                        else
                            bomPhaseSeperationObj = new BomPhaseSeperation();

                        bomPhaseSeperationObj.setHandle(bomPhaseSepNm);
                        bomPhaseSeperationObj.setTitle(getEachManufacturing.get("title"));
                        bomPhaseSeperationObj.setPhaseSeperationData(getEachManufacturing.get("data"));
                        bomPhaseSeperationObj.setBatchSize(recordRequest.getHeaderDetails().get("batchSize"));
                        bomPhaseSeperationObj.setSite(recordRequest.getSite());
                        bomPhaseSeperationObj.setDataField("RAW_MATERIALS_INDENT");
                        bomPhaseSeperationObj.setActive(1);
                        bomRecipeRepository.save(bomPhaseSeperationObj);
                    }
                    if (getEachManufacturing.get("title").equals("General Dos and Donts") && getEachManufacturing.get("data") != null) {
                        dosAndDontsNm = "MfrNO"+":"+recordRequest.getSite()+","+"GENERAL_DOS_AND_DONTS"+","+recordRequest.getMfrNo()+","+recordRequest.getVersion();
                        boolean dosAndDontsIsExist = dosAndDontsRepository.existsByHandleAndSiteAndActive(dosAndDontsNm, recordRequest.getSite(), 1);

                        if(dosAndDontsIsExist)
                            mfrDosAndDontsObj = dosAndDontsRepository.findByHandleAndActiveAndSite(dosAndDontsNm, 1, recordRequest.getSite());
                        else {
                            mfrDosAndDontsObj= new DosAndDonts();
                        }
                        mfrDosAndDontsObj.setHandle(dosAndDontsNm);
                        mfrDosAndDontsObj.setTitle(getEachManufacturing.get("title"));
                        mfrDosAndDontsObj.setDosAndDontsData(getEachManufacturing.get("data"));
                        mfrDosAndDontsObj.setSite(recordRequest.getSite());
                        mfrDosAndDontsObj.setDataField("GENERAL_DOS_AND_DONTS");
                        mfrDosAndDontsObj.setActive(1);
                        dosAndDontsRepository.save(mfrDosAndDontsObj);
                    }
                    if (getEachManufacturing.get("title").equals("Critical Control Points") && getEachManufacturing.get("data") != null) {
                        criticalControlNm = "MfrNO"+":"+recordRequest.getSite()+","+"CRITICAL_CONTROL_POINTS"+","+recordRequest.getMfrNo()+","+recordRequest.getVersion();
                        boolean criticalControlIsExist = criticalControlsRepository.existsByHandleAndSiteAndActive(criticalControlNm, recordRequest.getSite(), 1);

                        if(criticalControlIsExist)
                            controlPointsObj = criticalControlsRepository.findByHandleAndActiveAndSite(criticalControlNm, 1,recordRequest.getSite());
                        else {
                            controlPointsObj = new CriticalControlPoints();
                        }
                        controlPointsObj.setHandle(criticalControlNm);
                        controlPointsObj.setTitle(getEachManufacturing.get("title"));
                        controlPointsObj.setCriticalControlPointsData(getEachManufacturing.get("data"));
                        controlPointsObj.setSite(recordRequest.getSite());
                        controlPointsObj.setDataField("CRITICAL_CONTROL_POINTS");
                        controlPointsObj.setActive(1);
                        criticalControlsRepository.save(controlPointsObj);
                    }
                    if (getEachManufacturing.get("title").equals("Manufacturing Procedure") && getEachManufacturing.get("data") != null) {

                        Set<String> material = getMaterialFromPhaseSeperation(briefManufacturingRecord);
                        if(material.isEmpty())
                            throw new Exception("Phase Seperation is empty, can't calculate Manufacturing Procedure");

                        maunafturingHandleNm = "MfrNO"+":"+recordRequest.getSite()+","+"MANUFACTURING"+","+recordRequest.getMfrNo()+","+recordRequest.getVersion();
                        boolean manufacturingProIsExist = manuFactureProRepository.existsByHandleAndSiteAndActive(maunafturingHandleNm, recordRequest.getSite(), 1);

                        if(manufacturingProIsExist)
                            manufacturingProcedureObj = manuFactureProRepository.findByHandleAndActiveAndSite(maunafturingHandleNm, 1,recordRequest.getSite());
                        else {
                            manufacturingProcedureObj = new ManufacturingProcedure();
                        }
                        manufacturingProcedureObj.setHandle(maunafturingHandleNm);
                        manufacturingProcedureObj.setTitle(getEachManufacturing.get("title"));
                        manufacturingProcedureObj.setBatchSize(recordRequest.getHeaderDetails().get("batchSize"));

                        List<Map<String, Object>> manufacturingProcedures = (List<Map<String, Object>>) getEachManufacturing.get("data");

                        List<Map<String, Object>> manufacturingDataList = new ArrayList<>();

                        for (int i = 0; i < manufacturingProcedures.size(); i++) { 
                            Map<String, Object> procedureDetails = manufacturingProcedures.get(i);
                            Map<String, Object> manufacturing = new HashMap<>();

                            manufacturing.put("phase", procedureDetails.get("phase"));
                            manufacturing.put("stepId", i + 1); 
                            manufacturing.put("observation", procedureDetails.get("observation"));
                            manufacturing.put("manufacturing", procedureDetails.get("procedureDescription"));

                            String procedureDescription = (String) procedureDetails.get("procedureDescription");
                            List<Map<String,String>> calcDescripList = calcManufacturingDescrip(procedureDescription, material);

                            Map<String, String> procedure;
                            List<Map<String, String>> procedureList = new ArrayList<>();
                            for (Map<String, String> parts : calcDescripList) {
                                procedure = new HashMap<>();
                                procedure.put("component", parts.get("component"));
                                procedure.put("stdQty", parts.get("stdQty"));
                                procedureList.add(procedure);
                            }
                            manufacturing.put("procedure", procedureList);

                            manufacturingDataList.add(manufacturing);
                        }
                        manufacturingProcedureObj.setManufacturingData(manufacturingDataList);
                        manufacturingProcedureObj.setSite(recordRequest.getSite());
                        manufacturingProcedureObj.setDataField("MANUFACTURING");
                        manufacturingProcedureObj.setActive(1);
                        manuFactureProRepository.save(manufacturingProcedureObj);
                    }
                }
            }
        }

        List<Object> transformedResult = new ArrayList<>();
        if (recordRequest.getResult()!=null && !recordRequest.getResult().isEmpty()) {

            for (Object getResult : recordRequest.getResult()) {
                Map<String, Object> resultMap = (Map<String, Object>) getResult;

                List<Map<String, Object>> procedures = (List<Map<String, Object>>) resultMap.get("procedures");
                if (procedures != null) {
                    for (Map<String, Object> procedure : procedures) {
                        String title = (String) procedure.get("title");
                        List<Map<String, Object>> items = (List<Map<String, Object>>) procedure.get("items");
                        List<Map<String, Object>> newItems = new ArrayList<>();

                        for (Map<String, Object> item : items) {
                            Map<String, Object> newItem = new HashMap<>();
                            newItem.put("step", item.get("title"));
                            newItem.put("phase", item.get("phase"));
                            newItems.add(newItem);
                        }

                        Map<String, Object> newProcedure = new HashMap<>();
                        newProcedure.put("title", title);
                        newProcedure.put("items", newItems);
                        transformedResult.add(newProcedure);
                    }
                }
            }

        }

        mfrRecipes.setSite(recordRequest.getSite());
        mfrRecipes.setHandle(recordRequest.getHandle());
        mfrRecipes.setMfrNo(recordRequest.getMfrNo());
        mfrRecipes.setHeaderDetails(recordRequest.getHeaderDetails());
        mfrRecipes.setVersion(recordRequest.getVersion());
        mfrRecipes.setConfiguration(recordRequest.getConfiguration());
        mfrRecipes.setSections(recordRequest.getSections());
        mfrRecipes.setFooterDetails(recordRequest.getFooterDetails());
        mfrRecipes.setBomPhaseSepHandle(bomPhaseSepNm);
        mfrRecipes.setCriticalControlHandle(criticalControlNm);
        mfrRecipes.setDosAndDontsHandle(dosAndDontsNm);
        mfrRecipes.setManufacturingProHandle(maunafturingHandleNm);
        mfrRecipes.setCreatedDateTime(LocalDateTime.now());
        mfrRecipes.setModifiedDateTime(LocalDateTime.now());
        mfrRecipes.setCreatedBy(recordRequest.getCreatedBy());
        if(!transformedResult.isEmpty())
            mfrRecipes.setRoutingSteps(transformedResult);
        mfrRecipes.setActive(1);
        return mfrRecipes;
    }

    private static Set<String> getMaterialFromPhaseSeperation(List<Map<String, Object>> briefManufacturingRequest) {
        Set<String> materialDescripList = new HashSet<>();
        for(Map<String, Object> getEachManufacturing: briefManufacturingRequest){
            if (getEachManufacturing.get("title").equals("Phase Separation")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) getEachManufacturing.get("data");
                for (Map<String, Object> data : dataList) {
                    materialDescripList.add((String) data.get("materialDescription"));
                }
            }
        }
        return materialDescripList;
    }

    private static List<Map<String,String>> calcManufacturingDescrip(String procedureDescription, Set<String> phaseSepList) {
        List<Map<String, String>> manufacturingList = new ArrayList<>();

        for (String phase : phaseSepList) {
            String phaseExtraction = String.format("%s\\s*\\((\\d+\\.\\d+%%)\\)", Pattern.quote(phase));
            Pattern pattern = Pattern.compile(phaseExtraction, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(procedureDescription);

            while (matcher.find()) {
                Map<String, String> parts = new HashMap<>();
                parts.put("component", phase);
                parts.put("stdQty", matcher.group(1));
                manufacturingList.add(parts);

                procedureDescription = matcher.replaceFirst("");
                matcher = pattern.matcher(procedureDescription);
            }
        }
        return manufacturingList;
    }

    @Override
    public MfrRecipes retrieveMfrRecipes(MfrRecipesRequest mfrRecipesRequest) throws Exception {
        boolean mfrRecordIsExist = mfrRepository.existsByMfrNoAndVersionAndActiveAndSite(mfrRecipesRequest.getMfrNo(), mfrRecipesRequest.getVersion(), 1, mfrRecipesRequest.getSite());
        if (!mfrRecordIsExist) {
            throw new MfrRecipesException(10002, mfrRecipesRequest.getMfrNo());
        } else {
            return mfrRepository.findByMfrNoAndVersionAndActive(mfrRecipesRequest.getMfrNo(), mfrRecipesRequest.getVersion(), 1);
        }
    }

    @Override
    public MFRResponseList retrieveAllMfrRecipes(String site) {
        List<MFRResponse> mfrResponseList = mfrRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return MFRResponseList.builder().mfrList(mfrResponseList).build();
    }

    @Override
    public MFRResponse getNewMfr(String type, String site) throws Exception {
        List<NextNumberResponse> getNewMfr = new ArrayList<>();
        NextNumberRequest numberRequest = NextNumberRequest.builder()
                .site(site)
                .batchQty(1)
                .numberType(type)
                .build();

        getNewMfr = webClientBuilder
                .build()
                .post()
                .uri(newNxtNumberUrl)
                .body(BodyInserters.fromValue(numberRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<NextNumberResponse>>() {
                })
                .block();

        if(getNewMfr.size()!=0) {
            GeneratePrefixAndSuffixRequest nextNumberRequest = GeneratePrefixAndSuffixRequest.builder()
                    .site(site)
                    .currentSequence(getNewMfr.get(getNewMfr.size() - 1).getCurrentSequence())
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
            MFRResponse mfrResponse = new MFRResponse();
            mfrResponse.setNewNumber(getNewMfr.get(0).getNextNumber());
            return mfrResponse;
        }
        return null;
    }

    @Override
    public List<String> getMfrList(MfrRecipesRequest mfrRecipesRequest) throws Exception {
        List<String> mfrList = new ArrayList<>();

        boolean exists = mfrRepository.existsByMfrNoAndActiveAndSite(mfrRecipesRequest.getMfrNo(), 1, mfrRecipesRequest.getSite());
        if (!exists) {
            mfrList.add(mfrRecipesRequest.getMfrNo());
        }
        return mfrList;
    }

    @Override
    public RMIResponseClass getSelectiveMfrData(MFRResponse mfrResponse) throws Exception{

        RMIResponseClass rmiResponseClass = new RMIResponseClass();
        Map<String,Object> trueDataFieldsRecord = new HashMap<>();

        MfrRecipes mfrRecipes = mfrRepository.findByMfrNoAndVersionAndSiteAndActive(mfrResponse.getMfrNo(), mfrResponse.getVersion(), mfrResponse.getSite(), 1);
        rmiResponseClass.setMfrRecipes(mfrRecipes);

        String[] phaseSeperationNm = null, manufacturingNm = null, criticalControlNm = null, dosDontsNm = null;

        if (mfrRecipes.getBomPhaseSepHandle() != null) {
            phaseSeperationNm = mfrRecipes.getBomPhaseSepHandle().split(",");
        }
        if (mfrRecipes.getManufacturingProHandle() != null) {
            manufacturingNm = mfrRecipes.getManufacturingProHandle().split(",");
        }
        if (mfrRecipes.getCriticalControlHandle() != null) {
            criticalControlNm = mfrRecipes.getCriticalControlHandle().split(",");
        }
        if (mfrRecipes.getDosAndDontsHandle() != null) {
            dosDontsNm = mfrRecipes.getDosAndDontsHandle().split(",");
        }

        if(phaseSeperationNm != null && mfrResponse.getDataFieldList().contains(phaseSeperationNm[1])){
            BomPhaseSeperation bomPhaseSeperation = bomRecipeRepository.findByHandleAndActiveAndSite(mfrRecipes.getBomPhaseSepHandle(), 1, mfrResponse.getSite());
            trueDataFieldsRecord.put(phaseSeperationNm[1], bomPhaseSeperation);
        }

        if(manufacturingNm != null && mfrResponse.getDataFieldList().contains(manufacturingNm[1])){
            ManufacturingProcedure manufacturingProcedure = manuFactureProRepository.findByHandleAndActiveAndSite(mfrRecipes.getManufacturingProHandle(), 1, mfrResponse.getSite());
            trueDataFieldsRecord.put(manufacturingNm[1], manufacturingProcedure);
        }

        if(criticalControlNm != null && mfrResponse.getDataFieldList().contains(criticalControlNm[1])){
            CriticalControlPoints criticalControlPoints = criticalControlsRepository.findByHandleAndActiveAndSite(mfrRecipes.getCriticalControlHandle(), 1, mfrResponse.getSite());
            trueDataFieldsRecord.put(criticalControlNm[1], criticalControlPoints);
        }

        if(dosDontsNm != null && mfrResponse.getDataFieldList().contains(dosDontsNm[1])){
            DosAndDonts mfrDosAndDonts = dosAndDontsRepository.findByHandleAndActiveAndSite(mfrRecipes.getDosAndDontsHandle(), 1, mfrResponse.getSite());
            trueDataFieldsRecord.put(dosDontsNm[1], mfrDosAndDonts);
        }
        rmiResponseClass.setTrueDataFieldsRecord(trueDataFieldsRecord);

        return rmiResponseClass;
    }

    public MfrMessageModel getMfrToMfrConv(RecordRequest newMfrRequest) throws Exception{
        MfrRecipes newMfrRecord = new MfrRecipes();

        MfrRecipes mfrRecipeResponse = mfrRepository.findByMfrNoAndVersionAndSiteAndActive(newMfrRequest.getMfrNo(), newMfrRequest.getVersion(), newMfrRequest.getSite(), 1);

        if(mfrRecipeResponse!=null) {
            double oldBatchSize = Double.parseDouble(mfrRecipeResponse.getHeaderDetails().get("batchSize").replaceAll("[^\\d.]", ""));
            double newBatchSize = Double.parseDouble(newMfrRequest.getNewBatchSize().replaceAll("[^\\d.]", ""));
            Map<String, List<Map<String, Object>>> sectionsMap = mfrRecipeResponse.getSections();

            List<Map<String, Object>> briefManufacturingAndPackingProcedure = sectionsMap.get("briefManufacturingAndPackingProcedure");

            List<Map<String, Object>> newBmpps = new ArrayList<>();

            //MANUFACTURING PROCEDURE
            String maunafaturingHandle = "MfrNO" + ":" + newMfrRequest.getSite() + "," + "MANUFACTURING" + "," + newMfrRequest.getNewMfr() + "," + newMfrRequest.getNewVersion();

            boolean manufacturingProIsExist = manuFactureProRepository.existsByHandleAndSiteAndActive(maunafaturingHandle, newMfrRequest.getSite(), 1);

            if (!manufacturingProIsExist) {
                ManufacturingProcedure manufacturingProObj = manuFactureProRepository.findByHandleAndActiveAndSite(mfrRecipeResponse.getManufacturingProHandle(), 1, newMfrRequest.getSite());

                if(manufacturingProObj!=null) {
                    newMfrRecord.setManufacturingProHandle(maunafaturingHandle);
                    ManufacturingProcedure manufacturingProcedure = new ManufacturingProcedure();
                    manufacturingProcedure.setHandle(maunafaturingHandle);
                    manufacturingProcedure.setTitle(manufacturingProObj.getTitle());
                    manufacturingProcedure.setBatchSize(newMfrRequest.getNewBatchSize());

                    List<Map<String, Object>> manufacturingProcedures = (List<Map<String, Object>>) manufacturingProObj.getManufacturingData();
                    List<Map<String, Object>> manufacturingDataList = new ArrayList<>();

                    for (Map<String, Object> procedureDetails : manufacturingProcedures) {
                        Map<String, Object> manufacturingRec = new HashMap<>();
                        manufacturingRec.put("phase", procedureDetails.get("phase"));
                        manufacturingRec.put("stepId", procedureDetails.get("stepId"));
                        manufacturingRec.put("observation", procedureDetails.get("observation"));
                        manufacturingRec.put("manufacturing", procedureDetails.get("manufacturing"));

                        List<Map<String, Object>> procedures = (List<Map<String, Object>>) procedureDetails.get("procedure");
                        List<Map<String, Object>> updatedProcedures = new ArrayList<>();

                        for (Map<String, Object> componentDetails : procedures) {
                            String component = (String) componentDetails.get("component");
                            String stdQty = (String) componentDetails.get("stdQty");

                            double quantity = Double.parseDouble(stdQty.replace("%", ""));
                            double newQuantity = (quantity / oldBatchSize) * newBatchSize;
                            String newStdQty = String.format("%.3f", newQuantity) + "%";

                            Map<String, Object> updatedComponent = new HashMap<>();
                            updatedComponent.put("component", component);
                            updatedComponent.put("stdQty", newStdQty);
                            updatedProcedures.add(updatedComponent);
                        }

                        manufacturingRec.put("procedure", updatedProcedures);
                        manufacturingDataList.add(manufacturingRec);
                    }
                    manufacturingProcedure.setManufacturingData(manufacturingDataList);
                    manufacturingProcedure.setSite(newMfrRequest.getSite());

                    Set<String> material = getMaterialFromPhaseSeperation(briefManufacturingAndPackingProcedure);
                    if(material.isEmpty())
                        throw new Exception("Phase Seperation is empty, can't calculate Manufacturing Procedure");

                    if (briefManufacturingAndPackingProcedure != null) {
//                        List<Map<String, Object>> newBmpp = new ArrayList<>();

                        for (Map<String, Object> procedure : briefManufacturingAndPackingProcedure) {
                            String title = (String) procedure.get("title");

                            if ("Manufacturing Procedure".equals(title)) {
                                List<Map<String, Object>> dataList = (List<Map<String, Object>>) procedure.get("data");

                                if (dataList != null) {
                                    List<Map<String, Object>> newDataList = new ArrayList<>();

                                    for (Map<String, Object> data : dataList) {
                                        String procedureDescription = (String) data.get("procedureDescription");
                                        String updatedDescription = updateProcedureDescription(procedureDescription, oldBatchSize, newBatchSize, material);

                                        data.put("procedureDescription", updatedDescription);
                                        newDataList.add(data);
                                    }
                                    procedure.put("data", newDataList);
                                }
                            }

                            newBmpps.add(procedure);
                        }
                        briefManufacturingAndPackingProcedure.removeIf(section -> "Manufacturing Procedure".equals(section.get("title")));
                    }

                    manufacturingProcedure.setDataField("MANUFACTURING");
                    manufacturingProcedure.setActive(1);
                    manuFactureProRepository.save(manufacturingProcedure);
                }
            }else{
                throw new MfrRecipesException(10005,newMfrRequest.getNewMfr());
            }

            //PHASE SEPERATION
            String bomPhaseSepHandle = "MfrNO" + ":" + newMfrRequest.getSite() + "," + "RAW_MATERIALS_INDENT" + "," + newMfrRequest.getNewMfr() + "," + newMfrRequest.getNewVersion();
            boolean phaseSeperationIsExist = bomRecipeRepository.existsByHandleAndSiteAndActive(bomPhaseSepHandle, newMfrRequest.getSite(), 1);

            if (!phaseSeperationIsExist){
                BomPhaseSeperation phaseSeperationObj = bomRecipeRepository.findByHandleAndActiveAndSite(mfrRecipeResponse.getBomPhaseSepHandle(), 1, newMfrRequest.getSite());

                if(phaseSeperationObj!=null) {
                    newMfrRecord.setBomPhaseSepHandle(bomPhaseSepHandle);

                    List<LinkedHashMap> rawPhaseSeperationData = (List<LinkedHashMap>) phaseSeperationObj.getPhaseSeperationData();
                    List<PhaseSeperationData> phaseSeperationData = new ArrayList<>();

                    for (LinkedHashMap data : rawPhaseSeperationData) {
                        PhaseSeperationData phaseData = objectMapper.convertValue(data, PhaseSeperationData.class);
                        phaseSeperationData.add(phaseData);
                    }

                    List<PhaseSeperationData> updatedPhaseSeperationData = new ArrayList<>();
                    for (PhaseSeperationData data : phaseSeperationData) {
                        double oldQuantity = Double.parseDouble(data.getQuantity());
                        double newQuantity = (oldQuantity / oldBatchSize) * newBatchSize;
                        data.setQuantity(String.format("%.3f", newQuantity));
                        updatedPhaseSeperationData.add(data);
                    }
                    BomPhaseSeperation newPhaseSep = new BomPhaseSeperation();
                    newPhaseSep.setHandle(bomPhaseSepHandle);
                    newPhaseSep.setTitle(phaseSeperationObj.getTitle());
                    newPhaseSep.setPhaseSeperationData(updatedPhaseSeperationData);
                    newPhaseSep.setBatchSize(newMfrRequest.getNewBatchSize());
                    newPhaseSep.setSite(newMfrRequest.getSite());
                    newPhaseSep.setDataField("RAW_MATERIALS_INDENT");
                    newPhaseSep.setActive(1);
                    bomRecipeRepository.save(newPhaseSep);

                    Map<String, Object> newPhaseSeparation = new HashMap<>();
                    newPhaseSeparation.put("title", "Phase Separation");
                    newPhaseSeparation.put("data", newPhaseSep.getPhaseSeperationData());
                    newPhaseSeparation.put("tableId", "briefmanufacturingandpackingprocedure_phase_separation");

                    newBmpps.removeIf(section ->
                            "Phase Separation".equals(section.get("title"))
                    );

                    newBmpps.add(newPhaseSeparation);
                }
            }else{
                throw new MfrRecipesException(10005,newMfrRequest.getNewMfr());
            }

            newMfrRecord.setSections(sectionsMap);

            //BILL OF MATERIAL
            List<Map<String, Object>> billOfMaterials = sectionsMap.get("billOfMaterials");
//            Map<String,List<Map<String, Object>>> sections = mfrRecipeResponse.getSections();
            sectionsMap.remove("billOfMaterials");
            if(billOfMaterials!= null) {
                List<Map<String, Object>> newBillOfMaterials = new ArrayList<>();
                for (Map<String, Object> material : billOfMaterials) {
                    List<Map<String, Object>> materialDataList = (List<Map<String, Object>>) material.get("data");

                    if (materialDataList != null) {
                        List<Map<String, Object>> newMaterialDataList = new ArrayList<>();

                        for (Map<String, Object> materialData : materialDataList) {
                            Map<String, Object> newMaterialData = new HashMap<>(materialData);

                            if (materialData.containsKey("addnWw")) {
                                double percentAddnWw = Double.parseDouble(materialData.get("addnWw").toString());
                                double newPercentAddnWw = (percentAddnWw / oldBatchSize) * newBatchSize;
                                newMaterialData.put("addnWw", newPercentAddnWw);
                            }

                            if (materialData.containsKey("qtyPerBatch")) {
                                double qtyPerBatch = Double.parseDouble(materialData.get("qtyPerBatch").toString());
                                double newQtyPerBatch = (qtyPerBatch / oldBatchSize) * newBatchSize;
                                newMaterialData.put("qtyPerBatch", newQtyPerBatch);
                                newMaterialData.put("qtyPerGInMg", newQtyPerBatch * 10);
                            }

                            newMaterialDataList.add(newMaterialData);
                        }

                        material.put("data", newMaterialDataList);
                    }

                    newBillOfMaterials.add(material);
                }

                sectionsMap.put("billOfMaterials", newBillOfMaterials);
                newMfrRecord.setSections(sectionsMap);
            }

            //DOSANDDONTS
            String dosAndDontsNm = "MfrNO"+":"+newMfrRequest.getSite()+","+"GENERAL_DOS_AND_DONTS"+","+newMfrRequest.getNewMfr()+","+newMfrRequest.getNewVersion();
            boolean dosAndDontsIsExist = dosAndDontsRepository.existsByHandleAndSiteAndActive(dosAndDontsNm, newMfrRequest.getSite(), 1);

            if(!dosAndDontsIsExist){
                DosAndDonts mfrDosAndDontsObj = dosAndDontsRepository.findByHandleAndActiveAndSite(mfrRecipeResponse.getDosAndDontsHandle(), 1, newMfrRequest.getSite());
                if(mfrDosAndDontsObj!=null) {
                    newMfrRecord.setDosAndDontsHandle(dosAndDontsNm);

                    DosAndDonts dosAndDonts = new DosAndDonts();
                    dosAndDonts.setHandle(dosAndDontsNm);
                    dosAndDonts.setActive(1);
                    dosAndDonts.setSite(newMfrRequest.getSite());
                    dosAndDonts.setDosAndDontsData(mfrDosAndDontsObj.getDosAndDontsData());
                    dosAndDonts.setTitle(mfrDosAndDontsObj.getTitle());
                    dosAndDonts.setDataField(mfrDosAndDontsObj.getDataField());
                    dosAndDontsRepository.save(dosAndDonts);

                    Map<String, Object> dosAndDontsSection = new HashMap<>();
                    dosAndDontsSection.put("title", "General Dos and Donts");
                    dosAndDontsSection.put("data", mfrDosAndDontsObj.getDosAndDontsData()); // Assuming `getData()` returns the relevant data for dos and don'ts
                    dosAndDontsSection.put("tableId", "briefmanufacturingandpackingprocedure_dos_and_donts");
                    newBmpps.removeIf(section ->
                            "General Dos and Donts".equals(section.get("title"))
                    );
                    newBmpps.add(dosAndDontsSection);
                }

            }

            //CRITICAL CONTROL POINTS
            String criticalControlNm = "MfrNO"+":"+newMfrRequest.getSite()+","+"CRITICAL_CONTROL_POINTS"+","+newMfrRequest.getNewMfr()+","+newMfrRequest.getNewVersion();
            boolean criticalControlIsExist = criticalControlsRepository.existsByHandleAndSiteAndActive(criticalControlNm, newMfrRequest.getSite(), 1);

            if(!criticalControlIsExist){
                CriticalControlPoints controlPointsObj = criticalControlsRepository.findByHandleAndActiveAndSite(mfrRecipeResponse.getCriticalControlHandle(), 1,newMfrRequest.getSite());

                if(controlPointsObj!=null) {
                    newMfrRecord.setCriticalControlHandle(criticalControlNm);
                    CriticalControlPoints controlPoints = new CriticalControlPoints();
                    controlPoints.setHandle(criticalControlNm);
                    controlPoints.setTitle(controlPointsObj.getTitle());
                    controlPoints.setSite(newMfrRequest.getSite());
                    controlPoints.setDataField(controlPointsObj.getDataField());
                    controlPoints.setCriticalControlPointsData(controlPointsObj.getCriticalControlPointsData());
                    controlPoints.setActive(1);

                    criticalControlsRepository.save(controlPoints);
                    Map<String, Object> criticalControls = new HashMap<>();
                    criticalControls.put("title", "Critical Control Points");
                    criticalControls.put("data", controlPointsObj.getCriticalControlPointsData());
                    criticalControls.put("tableId", "briefmanufacturingandpackingprocedure_dos_and_donts");
                    newBmpps.removeIf(section ->
                            "Critical Control Points".equals(section.get("title"))
                    );
                    newBmpps.add(criticalControls);
                }
            }
            sectionsMap.put("briefManufacturingAndPackingProcedure", newBmpps);
            newMfrRecord.setSite(newMfrRequest.getSite());
            newMfrRecord.setMfrNo(newMfrRequest.getNewMfr());
            newMfrRecord.setVersion(newMfrRequest.getNewVersion());
            newMfrRecord.setConfiguration(mfrRecipeResponse.getConfiguration());
            newMfrRecord.setFooterDetails(mfrRecipeResponse.getFooterDetails());

            // Update headerDetails
            Map<String, String> headerDetails = mfrRecipeResponse.getHeaderDetails();
            headerDetails.put("mfrNo", newMfrRequest.getNewMfr());
            headerDetails.put("version", newMfrRequest.getNewVersion());
            headerDetails.put("batchSize", newMfrRequest.getNewBatchSize());
            newMfrRecord.setHeaderDetails(headerDetails);

            newMfrRecord.setCreatedBy(newMfrRequest.getCreatedBy());
            newMfrRecord.setCreatedDateTime(LocalDateTime.now());
            newMfrRecord.setModifiedDateTime(LocalDateTime.now());
            newMfrRecord.setType("MFR");
            newMfrRecord.setActive(1);
            mfrRepository.save(newMfrRecord);
        }else{
            throw new MfrRecipesException(10004, newMfrRequest.getMfrNo());
        }
        String createdMessage = getFormattedMessage(1, newMfrRequest.getNewMfr()+","+newMfrRequest.getNewVersion());
        return MfrMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(newMfrRecord).build();
    }

    private static String updateProcedureDescription(String procedureDescription, double oldBatchSize, double newBatchSize, Set<String> phaseSeparationComponents) {
        String newProDesc = procedureDescription;

        for (String component : phaseSeparationComponents) {
            String patternStr = "(?i)" + Pattern.quote(component) + "\\s*\\(\\s*(\\d+\\.?\\d*)\\s*%\\s*\\)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(newProDesc);

            while (matcher.find()) {
                String oldQtyStr = matcher.group(1);
                double oldQty = Double.parseDouble(oldQtyStr);
                double newQty = (oldQty / oldBatchSize) * newBatchSize;
                String newQtyStr = String.format("%.3f", newQty);

                newProDesc = newProDesc.replaceFirst(Pattern.quote(matcher.group(0)), Matcher.quoteReplacement(component + " (" + newQtyStr + "%)"));
            }
        }

        return newProDesc;
    }

}