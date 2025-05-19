package com.rits.assyservice.service;

import com.rits.assemblyservice.exception.AssemblyException;
import com.rits.assyservice.dto.*;
import com.rits.assyservice.model.AssyData;
import com.rits.assyservice.repository.AssyRepository;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.pcuheaderservice.model.BomHeaderMessageModel;
import com.rits.pcuheaderservice.model.PcuHeader;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import com.rits.startservice.model.PcuInWork;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssyServiceImpl implements AssyService {
    @Value("${pcuheader-service.url}/readPcu")
    private String readPcuUrl;
    @Value("${start-service.url}/retrieve")
    private String retrieveInWorkUrl;
    @Value("${pcuheader-service.url}/isExist")
    private String isExistPcuUrl;
    @Value("${bom-service.url}/retrieve")
    private String retrieveBomUrl;
    @Value("${inventory-service.url}/retrieve")
    private String retrieveInventoryUrl;
    @Value("${inventory-service.url}/retrieveInventoryReturn")
    private String retrieveInventoryReturnUrl;

    @Value("${datafield-service.url}/isTrackable")
    private String isTrackableUrl;
    @Value("${customdataformat-service.url}/isDataFormatPresent")
    private String retrieveIsDataFormatPresentUrl;
    @Value("${inventory-service.url}/update")
    private String updateInventoryUrl;
    @Value("${bomheader-service.url}/update")
    private String updateBomHeaderUrl;
    @Value("${item-service.url}/retrieve")
    private String retrieveItemUrl;
    @Value("${datatype-service.url}/retrieve")
    private String retrieveDataTypeUrl;
    @Value("${bomheader-service.url}/retrieve")
    private String retrieveBomHeaderUrl;
    @Value("${pcudone-service.url}/retrieve")
    private String retrieveDoneUrl;

    private final AssyRepository assyRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    private WebClient.Builder webClientBuilder;



    @Override
public boolean assemble(AssyRequest request) {
    AssyData assyDatareq = new AssyData();

    AssyData savedAssyData = saveAssyData(request);
//
//    MessageModel messageModel = new MessageModel();
//    MessageDetails messageDetails = new MessageDetails();
    if (savedAssyData != null) {
        return true;
    } else {
       return false;
    }
//    List<MessageDetails> messageDetailsList = new ArrayList<>();
//    messageDetailsList.add(messageDetails);
//    messageModel.setMessageDetails(messageDetailsList);
//
//    return messageModel;
}
public PcuHeader getPcuHeader(String site, String pcuBO) {
    PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
            .site(site)
            .pcuBO(pcuBO)
            .build();
    PcuHeader pcuHeaderResponse= makeWebClientCall(readPcuUrl, pcuHeaderRequest, PcuHeader.class);
    if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO()==null) {
        throw new AssemblyException(2701, pcuBO);
    }
    return pcuHeaderResponse;
}
public PcuInWork getPcuInWork(AssyRequest request, AssyData.Component component) {
    PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
            .site(request.getSite())
            .pcuBO(request.getPcuBO())
            .operationBO("OperationBO:" + request.getSite() + "," + component.getOperation() + ",")
            .build();
    PcuInWork pcuInWork= makeWebClientCall(retrieveInWorkUrl, pcuHeaderRequest, PcuInWork.class);
    if (pcuInWork == null || pcuInWork.getPcuBO()==null) {
//        PcuDone pcuDone=makeWebClientCall(retrieveDoneUrl,pcuHeaderRequest,PcuDone.class);
//        if(pcuDone== null || pcuDone.getPcuBO()==null) {
//            throw new AssemblyException(170, request.getPcuBO());
//        }
    }
    return pcuInWork;
}
public Bom getBom(PcuHeader pcuHeader) {
    String pcuBom = createPcuBom(pcuHeader);
    BomHeaderRequest bomHeaderRequest = createBomHeaderRequest(pcuHeader, pcuBom);
    Bom bom= makeWebClientCall(retrieveBomHeaderUrl, bomHeaderRequest, Bom.class);
    if (bom == null&& bom.getBom()==null) {
        throw new AssemblyException(800);
    }
    return bom;
}

private String createPcuBom(PcuHeader pcuHeader) {
    String[] bom = pcuHeader.getBomList().get(0).getPcuBomBO().split(",");
    return String.format("PcuBomBO:%s,%s,%s,%s", pcuHeader.getSite(), bom[1], bom[2], pcuHeader.getPcuBO());
}

private BomHeaderRequest createBomHeaderRequest(PcuHeader pcuHeader, String pcuBom) {
    return BomHeaderRequest.builder()
            .site(pcuHeader.getSite())
            .pcuBomBO(pcuBom)
            .build();
}
public <T, R> R makeWebClientCall(String url, T requestBody, Class<R> responseType) {
    return webClientBuilder.build()
            .post()
            .uri(url)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseType)
            .block();
}
@Override
public AssyData saveAssyData(AssyRequest assyData) {
    List<AssyData.Ancestry> ancestryList = new ArrayList<>();
    if (assyData.getParentPcuBO() != null) {
        AssyData.Ancestry ancestry = new AssyData.Ancestry(assyData.getItemBO(), assyData.getParentPcuBO());
        ancestryList.add(ancestry);
    }
    PcuHeader pcuHeader;
    Item item;
    Bom bom;
    double requiredQty=0.0;
    String tags = null;
    AssyData savedAssyData;


try {


      pcuHeader = getPcuHeader(assyData.getSite(),assyData.getPcuBO());
      getPcuInWork(assyData,assyData.getComponentList().get(0));
      item = retrieveItem(assyData);
      bom = getBom(pcuHeader);
      setSequence(assyData.getComponentList().get(0),bom);
      requiredQty = getRequiredQtyAndCheckRequiredFields(bom,assyData.getComponentList().get(0),item);
      List<AssyData.AssembledPcu> assembledPcus=AreAssemblyDataValueValid(assyData.getComponentList().get(0),requiredQty,assyData.getSite());
     Bom updatedBom= checkAndFormatQuantity(assyData.getComponentList().get(0),bom,requiredQty);
    List<String> updatedTagsList = generateCombinations(assyData.getSite(),assyData.getComponentList().get(0).getAssemblyDataList());
    if(updatedTagsList!= null && !updatedTagsList.isEmpty()) {
        tags = String.join(",", updatedTagsList);
    }
    AssyData existingAssembly=assyRepository.findByActiveAndSiteAndPcuBO(1, assyData.getSite(), assyData.getPcuBO());
    assembledPcus=assembledPcus.stream().distinct().collect(Collectors.toList());
    if(existingAssembly!=null && existingAssembly.get_id()!=null){
        savedAssyData = updatedAssyData(pcuHeader, existingAssembly,assyData,tags,assembledPcus);
    }else{
        savedAssyData =createAssyData(pcuHeader,assyData,ancestryList,tags,assembledPcus);
    }
    updateBomHeader(updatedBom,assyData.getSite(),assyData.getPcuBO(),createPcuBom(pcuHeader));

    }catch(AssemblyException e ){
        throw e;
    }catch(Exception e){
        throw e;
    }


    // New logic
    for (AssyData.Component component : assyData.getComponentList()) {
        if (component.getAssembledPcu() != null) { // Add this null check
            for (AssyData.AssembledPcu assembledPcu : component.getAssembledPcu()) {
                if (assembledPcu != null) { // Add this null check
                    AssyData existingAssyData = assyRepository.findById(assembledPcu.getPcuBo()).orElse(null);
                    if (existingAssyData != null) {
                        existingAssyData.setParentPcuBO(assembledPcu.getPcuBo()); // Set parentPcuBO as assembledPcu's pcuBo
                        AssyData.Ancestry newAncestry = new AssyData.Ancestry(savedAssyData.getItemBO(), assyData.getPcuBO());
                        updateAncestry(existingAssyData, newAncestry);
                    }
                }
            }
        }
    }


    return savedAssyData;
}

    private void setSequence(AssyData.Component component, Bom bom) {
        boolean present=false;
        for(BomComponent bomComponent:bom.getBomComponentList()){
            if(bomComponent.getComponent().equals(component.getComponent()) && bomComponent.getAssyOperation().equals(component.getOperation())){
                if(bomComponent.getAssyQty().equals(bomComponent.getAssembledQty())){
                    component.setSequence(bomComponent.getAssySequence());
                    continue;
            }
                present=true;
                component.setSequence(bomComponent.getAssySequence());
                break;
            }
        }
        if(!present){
            //for non bom components
            component.setSequence("1");
        }

    }

    @Override
public AssyData setAncestry(AssyData assyData){
    AssyData.Ancestry newAncestry = null;
    List<AssyData.Ancestry> ancestryList= new ArrayList<>();
    for (AssyData.Component component : assyData.getComponentList()) {
        if (component.getAssembledPcu() != null) { // Add this null check
            for (AssyData.AssembledPcu assembledPcu : component.getAssembledPcu()) {
                if (assembledPcu != null) { // Add this null check
                    AssyData existingAssyData = assyRepository.findById(assembledPcu.getPcuBo()).orElse(null);
                    if (existingAssyData != null) {
                        existingAssyData.setParentPcuBO(assembledPcu.getPcuBo()); // Set parentPcuBO as assembledPcu's pcuBo
                       newAncestry = new AssyData.Ancestry(assyData.getItemBO(), assyData.getPcuBO());
                        updateAncestry(existingAssyData, newAncestry);
                        ancestryList.add(newAncestry);
                    }
                }
            }
        }
    }
    assyData.setAncestry(ancestryList);
    assyRepository.save(assyData);

    return assyData;

}

    private void updateBomHeader(Bom updatedBom, String site, String pcuBO, String pcuBomBO) {
        BomHeaderRequest bomHeaderRequest = BomHeaderRequest.builder()
                .site(site)
                .pcuBomBO(pcuBomBO)
                .build();
        List<Pcu> pcus = new ArrayList<>();
        Pcu pcu= new Pcu(pcuBO);
        pcus.add(pcu);

        bomHeaderRequest.setBom(updatedBom);
        bomHeaderRequest.setPcuBos(pcus);
        BomHeaderMessageModel bomHeaderResponse=makeWebClientCall(updateBomHeaderUrl,bomHeaderRequest,BomHeaderMessageModel.class);
        if(bomHeaderResponse==null || bomHeaderResponse.getMessage_details().getMsg_type().isEmpty()){
            throw new AssemblyException(4305,bomHeaderRequest.getPcuBomBO());
        }

    }

    private AssyData createAssyData(PcuHeader pcuHeader, AssyRequest assyData, List<AssyData.Ancestry> ancestryList, String tags, List<AssyData.AssembledPcu> assembledPcus) {
        assembledPcus=assembledPcus.stream().distinct().collect(Collectors.toList());

        if (tags.startsWith(",")) {
           tags=(tags.substring(1));
        }

        assyData.getComponentList().get(0).setAssembledPcu(assembledPcus);
        AssyData newAssyData=AssyData.builder()
                ._id(assyData.getPcuBO())
                .pcuBO(assyData.getPcuBO())
                .site(assyData.getSite())
                .pcuBomBO(createPcuBom(pcuHeader))
                .shopOrderBO(pcuHeader.getShopOrderBO())
                .itemBO(pcuHeader.getItemBO())
                .pcuRouterBO(pcuHeader.getRouterList().get(0).getPcuRouterBO())
                .tags(tags)
                .parentOrderBO(pcuHeader.getParentOrderBO())
                .parentPcuBO(assyData.getParentPcuBO())
                .level(1)
                .componentList(assyData.getComponentList())
                .ancestry(ancestryList)
                .active(1)
                .build();
        assyRepository.save(newAssyData);
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(assyData.getSite())
                .eventType("POD_ASSEMBLY")
                .userId("")//assyData.getUserId()
                .pcu(assyData.getPcuBO())
                .shopOrderBO(pcuHeader.getShopOrderBO())
                .operation_bo(assyData.getComponentList().get(0).getOperation())
                .routerBO(pcuHeader.getRouterList().get(0).getPcuRouterBO())
//                .workCenterBO("WorkCenterBO:")
//                .resourceBO(assyData.getComponentList().get(0).getResourceBO())
                .itemBO(assyData.getItemBO())
                .qty(Integer.parseInt(assyData.getComponentList().get(0).getQty()))
//                .data_field()
//                .data_value()
//                .component()
                .topic("production-log")
                .status("Active")
                .eventData(assyData.getPcuBO())
                .build();
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return newAssyData;
    }

    private AssyData updatedAssyData(PcuHeader pcuHeader, AssyData existingAssembly, AssyRequest assyData, String tags, List<AssyData.AssembledPcu> assembledPcus) {
        existingAssembly.getComponentList();
        assyData.getComponentList().get(0).setCreatedDateTime(LocalDateTime.now());
        List<AssyData.Component> updatedComponentList = updateOrCreateComponent(existingAssembly.getComponentList(), assyData.getComponentList().get(0),assembledPcus);
        if (existingAssembly.getTags() != null && !existingAssembly.getTags().isEmpty()) {
            existingAssembly.setTags(existingAssembly.getTags() + (tags.startsWith(",") ? "" : ",") + tags);
        } else {
            existingAssembly.setTags(tags);
        }

        if (existingAssembly.getTags().startsWith(",")) {
            existingAssembly.setTags(existingAssembly.getTags().substring(1));
        }


        existingAssembly.setComponentList(updatedComponentList);
        existingAssembly.setActive(1);
        assyRepository.save(existingAssembly);
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .site(assyData.getSite())
                .eventType("POD_ASSEMBLY")
                .userId("")//assyData.getUserId()
                .pcu(assyData.getPcuBO())
                .shopOrderBO(pcuHeader.getShopOrderBO())
                .operation_bo(assyData.getComponentList().get(0).getOperation())
                .routerBO(pcuHeader.getRouterList().get(0).getPcuRouterBO())
//                .workCenterBO("WorkCenterBO:")
//                .resourceBO(assyData.getComponentList().get(0).getResourceBO())
                .itemBO(assyData.getItemBO())
                .qty(Integer.parseInt(assyData.getComponentList().get(0).getQty()))
                .topic("production-log")
                .status("Active")
                .eventData(assyData.getPcuBO())
                .build();
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));

        return existingAssembly;
    }

    private List<AssyData.Component> updateOrCreateComponent(List<AssyData.Component> componentList, AssyData.Component newComponent, List<AssyData.AssembledPcu> assembledPcus) {
        boolean componentExists = false;
        for (AssyData.Component existingComponent : componentList) {
            if (existingComponent.getSequence().equals(newComponent.getSequence()) &&
                    existingComponent.getComponent().equals(newComponent.getComponent()) &&
                    existingComponent.getOperation().equals(newComponent.getOperation()) &&
                    existingComponent.getAssembledBy().equals(newComponent.getAssembledBy()) && !existingComponent.isRemoved() ) {

                // Update existing component's quantity
                String updatedQuantity = String.valueOf(Double.parseDouble(existingComponent.getQty()) + Double.parseDouble(newComponent.getQty()));
                existingComponent.setQty(updatedQuantity);
                if (existingComponent.getAssembledPcu() != null) {
                    Set<AssyData.AssembledPcu> uniquePcus = new HashSet<>();
                    for (AssyData.AssembledPcu assembledPcu : assembledPcus) {
                        if (!existingComponent.getAssembledPcu().stream()
                                .anyMatch(existingPcu -> existingPcu.getPcuBo().equals(assembledPcu.getPcuBo()))) {
                            uniquePcus.add(assembledPcu);
                        }
                    }
                    newComponent.setAssembledPcu(assembledPcus);
                    existingComponent.getAssembledPcu().addAll(uniquePcus);
                } else {
                  newComponent.setAssembledPcu(assembledPcus);
                    existingComponent.setAssembledPcu(new ArrayList<>(assembledPcus));
                }

                // Add assembly data to the existing component's assembly data list
                existingComponent.getAssemblyDataList().addAll(newComponent.getAssemblyDataList());


                componentExists = true;
                break; // No need to continue searching
            }
            newComponent.setAssembledPcu(assembledPcus);
        }

        // If a matching component doesn't exist, add the new component to the list
        if (!componentExists) {
            componentList.add(newComponent);
        }

        return componentList;
    }

    private List<String> generateCombinations(String site, List<AssyData.AssemblyData> assemblyDataList) {
        List<String> elements = new ArrayList<>();

        for (AssyData.AssemblyData assemblyData : assemblyDataList) {
            String dataField = assemblyData.getDataField();
            String dataValue = assemblyData.getValue();
            if(dataValue!=null&&!dataValue.isEmpty()) {
                IsExist isExist = IsExist.builder().site(site).dataField(dataField).build();
                if (isTrackable(isExist)) {
                    elements.add(dataField + ":" + dataValue);
                }
            }
        }

        List<String> result = new ArrayList<>();
        generateCombinationsHelper(elements, "", 0, result);
        return result;
    }

    public void generateCombinationsHelper(List<String> elements, String prefix, int startIndex, List<String> result) {
        result.add(prefix);
        for (int i = startIndex; i < elements.size(); i++) {
            generateCombinationsHelper(elements, prefix.isEmpty() ? elements.get(i) : prefix + "_" + elements.get(i), i + 1, result);
        }
    }

    private boolean isTrackable(IsExist isExist) {
        Boolean isTrackable = webClientBuilder.build()
                .post()
                .uri(isTrackableUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        return isTrackable;
    }
    private Bom checkAndFormatQuantity(AssyData.Component component, Bom bom, double requiredQty) {
        for (BomComponent bomComponent : bom.getBomComponentList()) {
            if (component.getComponent().equalsIgnoreCase(bomComponent.getComponent())&&component.getSequence().equals(bomComponent.getAssySequence())) {
                String qty = String.format("%.6f", (Double.parseDouble(bomComponent.getAssembledQty()) + Double.parseDouble(component.getQty())));
                if(Double.parseDouble(qty)>(requiredQty)){
                    throw new AssemblyException(1705);
                }
                bomComponent.setAssembledQty(qty);
            }
        }
        return bom;

    }

    private List<AssyData.AssembledPcu> AreAssemblyDataValueValid(AssyData.Component component, double requiredQty,String site) {
        List<AssyData.AssembledPcu> assembledPcus=new ArrayList<>();
        for (AssyData.AssemblyData assemblyData : component.getAssemblyDataList()) {
            if ((assemblyData.getDataField().equalsIgnoreCase("inventory")||assemblyData.getDataField(). equalsIgnoreCase("inv"))||(assemblyData.getDataField().equalsIgnoreCase("PCU"))) {
                if (assemblyData.getValue() != null && !assemblyData.getValue().isEmpty()){
                    component.setInventoryBO(assemblyData.getValue());
                    Inventory inventoryRequest = Inventory.builder().site(site).inventoryId(assemblyData.getValue()).build();
                    Inventory inventory = webClientBuilder.build()
                            .post()
                            .uri(retrieveInventoryUrl)
                            .bodyValue(inventoryRequest)
                            .retrieve()
                            .bodyToMono(Inventory.class)
                            .block();
                    if (inventory == null || inventory.getInventoryId() == null) {
                        throw new AssemblyException(1700, assemblyData.getValue());
                    }
                    if (Double.parseDouble(component.getQty()) > Double.parseDouble(inventory.getRemainingQty())) {
                        throw new AssemblyException(1701);
                    }
                    inventory.setRemainingQty(String.valueOf(Double.parseDouble(inventory.getRemainingQty()) - Double.parseDouble(component.getQty())));
                    InventoryMessageModel inventoryUpdate = webClientBuilder.build()
                            .post()
                            .uri(updateInventoryUrl)
                            .bodyValue(inventory)
                            .retrieve()
                            .bodyToMono(InventoryMessageModel.class)
                            .block();
                    if (inventoryUpdate == null || inventoryUpdate.getResponse()==null||inventoryUpdate.getResponse().getInventoryId()==null) {
                        throw new AssemblyException(1702, assemblyData.getValue());
                    }

                    String pcuBO="PcuBO:"+site+","+assemblyData.getValue();
                    AssyData data=null;
                    try {
                       data  = retrieve(site, pcuBO);
                    }catch(AssemblyException e){
                    }
                    if(data!=null && data.getPcuBO()!=null && !data.getPcuBO().isEmpty()) {
                        AssyData.AssembledPcu assembledPcu = AssyData.AssembledPcu.builder().pcuBo(pcuBO).build();
                        assembledPcus.add(assembledPcu);
                    }
                }
            }if ((assemblyData.getDataField().equalsIgnoreCase("PlannedMaterial"))||(assemblyData.getDataField().equalsIgnoreCase("planned material"))||(assemblyData.getDataField().equalsIgnoreCase("material"))){
                if(assemblyData.getValue()!=null&&!assemblyData.getValue().isEmpty()) {
                    IsExist isExistDataField=IsExist.builder().site(site).item(assemblyData.getValue()).build();
                    Item itemDataField=retrieveItemForDataValue(isExistDataField);
                    if(itemDataField==null||itemDataField.getItem()==null||itemDataField.getItem().isEmpty()){
                        throw new AssemblyException(3209,assemblyData.getValue());
                    }
                }

            }
        }
        return assembledPcus;
    }

    private Item retrieveItemForDataValue(IsExist isExistDataField) {
        Item item = makeWebClientCall(retrieveItemUrl,isExistDataField,Item.class);
        if(item==null || item.getItem()==null){
            throw new AssemblyException(4306,isExistDataField.getItem());
        }
        return item;
    }

    private double getRequiredQtyAndCheckRequiredFields(Bom bom, AssyData.Component component,Item item) {
        double requiredQty =0.0;
        String dataType=null;
        component.setNonBom(true);
        for (BomComponent bomComponent : bom.getBomComponentList()) {
            if (bomComponent.getComponent().equals(component.getComponent()) && bomComponent.getAssySequence().equals(component.getSequence())) {
                requiredQty=Double.parseDouble(bomComponent.getAssyQty());
                if (bomComponent.getAssemblyDataTypeBo() == null || bomComponent.getAssemblyDataTypeBo().isEmpty()) {
                    dataType = item.getAssemblyDataType();
                } else {
                    dataType = bomComponent.getAssemblyDataTypeBo();
                }
                if(dataType!=null&&!dataType.isEmpty()) {
                    checkRequiredFields(bom.getSite(), dataType, component);
                }
                component.setNonBom(false);

                break;
            }
        }
        return requiredQty;

    }

    private void checkRequiredFields(String site, String dataType, AssyData.Component component) {
        IsExist isExist = IsExist.builder().site(site).dataType(dataType).category("Assembly").build();
        DataType dataTypeResponse = webClientBuilder.build()
                .post()
                .uri(retrieveDataTypeUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(DataType.class)
                .block();

        if (dataTypeResponse == null || dataTypeResponse.getDataType() == null) {
            throw new AssemblyException(4307, isExist.getItem());
        }

        if (!dataTypeResponse.getDataFieldList().isEmpty()) {
            for (DataField dataField : dataTypeResponse.getDataFieldList()) {
                for (AssyData.AssemblyData assemblyData : component.getAssemblyDataList()) {
                    if (dataField.getDataField().equals(assemblyData.getDataField())) {
                        if (dataField.isRequired() && (assemblyData.getValue()==null|| assemblyData.getValue().isEmpty()) ){
                            throw new AssemblyException(4308, dataField.getDataField());
                        }
                        break;
                    }
                }
            }
        }
    }

    private Item retrieveItem(AssyRequest assyData) {
        IsExist isExist = IsExist.builder().site(assyData.getSite()).item(assyData.getComponentList().get(0).getComponent()).build();
        Item item = makeWebClientCall(retrieveItemUrl,isExist,Item.class);
        if(item==null || item.getItem()==null){
            throw new AssemblyException(4306,isExist.getItem());
        }
        return item;
    }

    private void updateAncestry(AssyData assyData, AssyData.Ancestry ancestry) {
        AssyData.Ancestry newAncestry = new AssyData.Ancestry(ancestry.getItem(), ancestry.getPcuBO());

        boolean exists = assyData.getAncestry().stream()
                .anyMatch(a -> a.getItem() != null && a.getItem().equals(ancestry.getItem()) &&
                        a.getPcuBO() != null && a.getPcuBO().equals(ancestry.getPcuBO()));

        // If the ancestry does not exist, add it to the beginning of the list
        if (!exists) {
            assyData.getAncestry().add(0, newAncestry);
        }
    assyRepository.save(assyData);
    for (AssyData.Component component : assyData.getComponentList()) {
        if (component.getAssembledPcu() != null) {
            for (AssyData.AssembledPcu assembledPcu : component.getAssembledPcu()) {
                if (assembledPcu != null) {
                    AssyData existingAssyData = assyRepository.findById(assembledPcu.getPcuBo()).orElse(null);
                    if (existingAssyData != null) {
                        updateAncestry(existingAssyData, newAncestry);
                    }
                }
            }
        }
    }
}

    @Override
    public AssyData getTreeStructure(String site, String itemBO, String pcuBO) throws Exception {
        if (isValidInput(site, itemBO, pcuBO)) {
           AssyData assembly = assyRepository.findByActiveAndSiteAndPcuBOAndItemBO(1, site, pcuBO, itemBO);
            if (assembly != null && assembly.get_id() != null && !assembly.get_id().isEmpty()) {
                List<AssyData> assemblyList = fetchAssemblyList(site, assembly);
                processAncestry(assembly, site,assemblyList);
            }
            return assembly;
        }
        return null;
    }

    @Override
    public boolean remove(String site, String pcuBO, AssyData.Component component) throws Exception {
        PcuHeader pcuHeaderResponse;
        Bom bomResponse;
        AssyData assyData;
        try{
            pcuHeaderResponse=getPcuHeader(site,pcuBO);
            bomResponse=getBom(pcuHeaderResponse);
            assyData= removeComponent(site,pcuBO,component);
            boolean partialRemove=isPartialRemove(site,pcuBO,component);
            if(assyData == null || assyData.getPcuBO() == null || assyData.getPcuBO().isEmpty()){
                return false;
            }
            assyRepository.save(assyData);
            if(!partialRemove) {
                removeAncestry(site,assyData.getItemBO(), pcuBO, component);
            }
            Bom updatedBom = updateQty(component,bomResponse);
            updateBomHeader(updatedBom,site,pcuBO,createPcuBom(pcuHeaderResponse));


        }catch(AssemblyException e){
            throw e;
        }
        catch (Exception e){
            throw e;
        }



        return true;
    }

    @Override
    public AssyData retrieve(String site, String pcuBO) {
        AssyData assyData= assyRepository.findByActiveAndSiteAndPcuBO(1,site,pcuBO);
        if(assyData==null || assyData.getPcuBO() ==null || assyData.getPcuBO().isEmpty()){
            throw new AssemblyException(4302,pcuBO);
        }
        return assyData;
    }

    private boolean isPartialRemove(String site, String pcuBO, AssyData.Component component) {
        AssyData assyData= assyRepository.findByActiveAndSiteAndPcuBO(1,site,pcuBO);
    boolean partialRemove = true;
        if(assyData!=null &&  assyData.getComponentList()!=null&& !assyData.getComponentList().isEmpty()) {
            for (AssyData.Component components : assyData.getComponentList()) {
                if (components.getComponent().equals(component.getComponent()) && component.getSequence().equals(components.getSequence())) {
                    if (component.getQty() == null || component.getQty().equals("")|| Double.parseDouble(component.getQty())==Double.parseDouble(components.getQty())) {
                        partialRemove = false;
                        break;
                    }
                }
            }
        }
        return partialRemove;
    }

    private Bom updateQty(AssyData.Component component, Bom bom) {
        for (BomComponent bomComponent : bom.getBomComponentList()) {
            if (component.getComponent().equalsIgnoreCase(bomComponent.getComponent())&&component.getSequence().equals(bomComponent.getAssySequence())) {
               if(Double.parseDouble(bomComponent.getAssembledQty())<Double.parseDouble(component.getQty())) {
                   throw new AssemblyException(1708,bomComponent.getAssembledQty());
               }
                String qty = String.format("%.6f", (Double.parseDouble(bomComponent.getAssembledQty()) - Double.parseDouble(component.getQty())));
                if(qty.equals("0.000000")){
                    bomComponent.setAssembledQty("0");
                }else {
                    bomComponent.setAssembledQty(qty);
                }
            }
        }
        return bom;
    }

    private void removeAncestry(String site, String itemBo, String pcuBO, AssyData.Component component) {
        List<AssyData> assyDataList= assyRepository.findByActiveAndSiteAndAncestry_PcuBOAndAncestry_Item(1,site,pcuBO,itemBo);
                assyDataList.stream()
                .forEach(assyData -> {
                    if (assyData.getPcuBO().equals("PcuBO:"+site+","+component.getInventoryBO())) {

                    List<AssyData.Ancestry> filteredAncestryList = assyData.getAncestry().stream()
                            .filter(ancestry ->
                                    !(ancestry.getPcuBO().equals(pcuBO) &&
                                            ancestry.getItem().equals(itemBo)))
                            .collect(Collectors.toList());
                    assyData.setAncestry(filteredAncestryList);
                    assyRepository.save(assyData); // Save the modified AssyData objects
                }
                });
    }

    private AssyData removeComponent(String site, String pcuBO, AssyData.Component component) {
        boolean partialRemove=isPartialRemove(site,pcuBO,component);
        AssyData.Ancestry ancestry = null;
        AssyData assyData= assyRepository.findByActiveAndSiteAndPcuBO(1,site,pcuBO);
        if(assyData!=null &&  assyData.getComponentList()!=null&& !assyData.getComponentList().isEmpty()) {
            for (AssyData.Component components : assyData.getComponentList()) {
                if(components.getComponent().equals(component.getComponent())&& component.getSequence().equals(components.getSequence())) {
                    String invValue = null;
                    for(AssyData.AssemblyData assemblyData: component.getAssemblyDataList()){
                        if ((assemblyData.getDataField().equalsIgnoreCase("inv") || assemblyData.getDataField().equalsIgnoreCase("PCU"))&& assemblyData.getValue()!=null ) {
                            invValue=assemblyData.getValue();
                        }
                        }
                    if(component.getQty()==null || component.getQty().equals("")){
                        component.setQty(components.getQty());

                    }
                    for (AssyData.AssemblyData assemblyData : components.getAssemblyDataList()) {

                        if ((assemblyData.getDataField().equalsIgnoreCase("inv") || assemblyData.getDataField().equalsIgnoreCase("PCU"))&& assemblyData.getValue()!=null &&assemblyData.getValue().equals(invValue)) {

                            if (!partialRemove) {
                                ancestry = AssyData.Ancestry.builder().item("ItemBO:" + site + "," + components.getComponent() + "," + components.getComponentVersion()).build();
                            }
                            if (invValue != null) {
                                Inventory inventoryRequest = Inventory.builder().site(site).inventoryId(invValue).build();
                                Inventory inventory = webClientBuilder.build()
                                        .post()
                                        .uri(retrieveInventoryReturnUrl)
                                        .bodyValue(inventoryRequest)
                                        .retrieve()
                                        .bodyToMono(Inventory.class)
                                        .block();
                                if (inventory == null || inventory.getInventoryId()== null) {
                                    throw new AssemblyException(1700, component.getInventoryBO());
                                }

                                inventory.setRemainingQty(String.valueOf(Double.parseDouble(inventory.getRemainingQty()) + Double.parseDouble(component.getQty())));
                                InventoryMessageModel inventoryUpdate = webClientBuilder.build()
                                        .post()
                                        .uri(updateInventoryUrl)
                                        .bodyValue(inventory)
                                        .retrieve()
                                        .bodyToMono(InventoryMessageModel.class)
                                        .block();
                                if (inventoryUpdate == null || inventoryUpdate.getResponse().getInventoryId().isEmpty()) {
                                    throw new AssemblyException(1702, component.getInventoryBO());
                                }

                            }
                        }
                        


                    }
                    if(partialRemove){
                        components.setQty(String.valueOf(Double.parseDouble(components.getQty()) - Double.parseDouble(component.getQty())));
                        component.setRemovedDate(LocalDateTime.now());
                        component.setRemoved(true);
                        component.setRemovedResourceBO(component.getResourceBO());
                        component.setRemovedOperationBO(component.getOperation());
                        assyData.getComponentList().add(component);
                        break;
                    }else{
                        components.setRemoved(true);
                        components.setRemovedDate(LocalDateTime.now());
                        components.setRemovedBy(component.getRemovedBy());
                        components.setRemovedResourceBO(component.getResourceBO());
                        components.setRemovedOperationBO(component.getOperation());
                        break;
                    }
                }
            }
        }
        assyRepository.save(assyData);
        return assyData;
    }

    private List<AssyData> fetchAssemblyList(String site, AssyData assembly) {
        List<AssyData> assemblyList = new ArrayList<>();

            List<AssyData> childAssemblyList = assyRepository.findByActiveAndSiteAndAncestry_PcuBOAndAncestry_Item(1,site,assembly.getPcuBO(), assembly.getItemBO());
            assemblyList.addAll(childAssemblyList);

        return assemblyList;
    }
    private boolean isValidInput(String site, String itemBO, String pcuBO) {
        return site != null && !site.isEmpty() && itemBO != null && !itemBO.isEmpty() && pcuBO != null && !pcuBO.isEmpty();
    }

//    private void processAncestry(AssyData assembly, String site) throws Exception {
//        for (AssyData.Ancestry ancestry : assembly.getAncestry()) {
//            AssyData childAssembly = assyRepository.findByActiveAndSiteAndPcuBOAndItemBO(1, site, ancestry.getPcuBO(), ancestry.getItem());
//            if (childAssembly != null) {
//                processComponents(site,assembly, childAssembly ,ancestry);
//            }
//        }
//    }

    private void processAncestry(AssyData assembly, String site, List<AssyData> assemblyList) throws Exception {
        assembly.getComponentList().removeIf(AssyData.Component::isRemoved);

        assembly.setComponentList(assembly.getComponentList());
        for(AssyData.Component component: assembly.getComponentList()){
            if( component.getAssembledPcu()!=null && ! component.getAssembledPcu().isEmpty()){
            for(AssyData.AssembledPcu assembledPcu: component.getAssembledPcu()) {
                for (AssyData childAssembly : assemblyList) {
                    if (assembledPcu.getPcuBo().equals(childAssembly.getPcuBO())) {
                    processComponents(site, assembly, childAssembly, null, assemblyList);
                    break;
                }
            }
            }
        }




            }
        }


//    private void processComponents(String site, AssyData assembly, AssyData childAssembly, AssyData.Ancestry ancestry, List<AssyData> assemblyList) throws Exception {
//        for (AssyData.Component component : assembly.getComponentList()) {
//            String itemBo[] = ancestry.getItem().split(",");
//            if (component.getComponent().equals(itemBo[1]) && component.getAssemblyDataList() != null && !component.getAssemblyDataList().isEmpty()) {
//                for (AssyData.AssemblyData assemblyData : component.getAssemblyDataList()) {
//                    if (assemblyData.getValue().equals(ancestry.getPcuBO())) {
//                        if(childAssembly.getAncestry()!=null && !childAssembly.getAncestry().isEmpty()){
//                            processAncestry(childAssembly,site,assemblyList);
//                        }
//                        addChildToComponent(component, childAssembly);
//                    }
//                }
//            }
//        }
//    }
private void processComponents(String site, AssyData assembly, AssyData childAssembly, AssyData.Ancestry ancestry, List<AssyData> assemblyList) throws Exception {

    for (AssyData.Component component : assembly.getComponentList()) {
        for (AssyData assyData : assemblyList) {
            String[] itemBo = assyData.getItemBO().split(",");
            if (component.getComponent().equals(itemBo[1]) && component.getAssembledPcu() != null && !component.getAssembledPcu().isEmpty()) {
                for (AssyData.AssembledPcu assemblyData : component.getAssembledPcu()) {
                    if (assemblyData.getPcuBo().equals(assyData.getPcuBO())) {
                        if (childAssembly.getAncestry() != null && !childAssembly.getAncestry().isEmpty()) {
                            processAncestry(childAssembly, site, assemblyList);
                        }
                        // Assuming addChildToComponent is the correct method to add child assemblies to the parent component
                        addChildToComponent(component, assyData);
                    }
                }
            }
        }
    }
}

//    private void processComponents(String site, AssyData assembly, AssyData childAssembly, AssyData.Ancestry ancestry, List<AssyData> assemblyList) throws Exception {
//        for (AssyData.Component component : assembly.getComponentList()) {
//            for (AssyData assyData : assemblyList) {
//                String[] itemBo = assyData.getItemBO().split(",");
//                if (component.getComponent().equals(itemBo[1]) && component.getAssembledPcu() != null && !component.getAssembledPcu().isEmpty()) {
//                    for (AssyData.AssembledPcu assemblyData : component.getAssembledPcu()) {
//                        if (assemblyData.getPcuBo().equals(assyData.getPcuBO())) {
//                            if (childAssembly.getAncestry() != null && !childAssembly.getAncestry().isEmpty()) {
//                                processAncestry(childAssembly, site, assemblyList);
//                            }
//                            addChildToComponent(component, childAssembly);
//                        }
//                    }
//                }
//            }
//        }
//    }
//    private void processComponents(String site, AssyData assembly, AssyData childAssembly, AssyData.Ancestry ancestry, List<AssyData> assemblyList) throws Exception {
//        for (AssyData.Component component : assembly.getComponentList()) {
//            // Check if the component is assembled with any PCU
//            if (component.getAssembledPcu() != null && !component.getAssembledPcu().isEmpty()) {
//
//                String[] itemBo = assembly.getItemBO().split(",");
//                if (component.getComponent().equals(itemBo[1]) && component.getAssembledPcu() != null && !component.getAssembledPcu().isEmpty()) {
//                for (AssyData.AssembledPcu assemblyData : component.getAssembledPcu()) {
//                    String assemblyPcu = assemblyData.getPcuBo();
//                    // Iterate over each assembly and check if the assembly PCU matches the current component's PCU
//                    for (AssyData assyData : assemblyList) {
//                        if (assemblyPcu.equals(assyData.getPcuBO())) {
//                            // Check if the current assembly is a child assembly
//                            if (childAssembly.getAncestry() != null && !childAssembly.getAncestry().isEmpty()) {
//                                processAncestry(childAssembly, site, assemblyList);
//                            }
//                            // Add the matched assembly as a child to the current component
//                            addChildToComponent(component, assyData);
//                        }
//                    }
//                    }
//                }
//            }
//        }
//    }



    private void addChildToComponent(AssyData.Component component, AssyData childTree) {
        if (component.getChildAssembly() == null) {
            component.setChildAssembly(new ArrayList<>());
        }
        Set<AssyData> uniqueChildAssemblies = new HashSet<>(component.getChildAssembly());
        uniqueChildAssemblies.add(childTree);

        List<AssyData> assyData = new ArrayList<>(uniqueChildAssemblies);

        component.setChildAssembly(assyData);
    }


}