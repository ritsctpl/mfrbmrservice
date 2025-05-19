package com.rits.assemblyservice.service;

import com.rits.Utility.BOConverter;
import com.rits.assemblyservice.dto.*;
import com.rits.assemblyservice.exception.AssemblyException;
import com.rits.assemblyservice.model.Assembly;
import com.rits.assemblyservice.model.AssemblyData;
import com.rits.assemblyservice.model.AssemblyGenealogy;
import com.rits.assemblyservice.model.Component;
import com.rits.assemblyservice.repository.AssemblyGenealogyRepository;
import com.rits.assemblyservice.repository.AssemblyRepository;

import com.rits.customdataformatservice.dto.CustomDataFormatObject;
import com.rits.customdataformatservice.exception.CustomDataFormatException;
import com.rits.customdataformatservice.model.CustomDataObject;
import com.rits.customdataformatservice.model.MainCustomDataObject;
import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import com.rits.startservice.dto.StartRequestDetails;
import com.rits.startservice.model.PcuInWork;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssemblyServiceImpl implements AssemblyService{
    private final AssemblyRepository assemblyRepository;
    private final WebClient.Builder webClientBuilder;
    private final AssemblyGenealogyRepository assemblyGenealogyRepository;
    private final MongoTemplate mongoTemplate;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
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
    @Value("${datafield-service.url}/retrieve")
    private String retrieveDataFieldUrl;
    @Value("${bomheader-service.url}/retrieve")
    private String retrieveBomHeaderUrl;
    @Value("${operation-service.url}/retrieveOperationByCurrentVersion")
    private String retrieveOperation;

    private String mainParent = "";

    @Override
    public boolean addComponent(String site,String parentPcuBO,String userId, String pcuBO, Component component) throws Exception {

        site=site;
        boolean parentAssembly=false;
        String childPcu = null;
        // Retrieve PCUHeader, if it doesn't exist, throw an exception
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();

        if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO()==null) {
            throw new AssemblyException(2701, pcuBO);
        }

        OperationRequest operationRequest = OperationRequest.builder()
                .site(site)
                .operation(component.getOperation())
                .build();

        OperationResponse operationresponse=webClientBuilder.build()
                .post()
                .uri(retrieveOperation)
                .bodyValue(operationRequest)
                .retrieve()
                .bodyToMono(OperationResponse.class)
                .block();

        if (operationresponse == null || operationresponse.getRevision()==null) {
            throw new AssemblyException(1710, operationresponse.getOperation());
        }

        //pcuHeaderRequest.setOperationBO("OperationBO:"+pcuHeaderRequest.getSite()+","+component.getOperation()+",");

        StartRequestDetails pcuInWorkRequest = StartRequestDetails.builder()
                .site(site)
                .pcu(BOConverter.getPcu(pcuBO))
                .operation(operationresponse.getOperation())
                .operationVersion(operationresponse.getRevision())
                .build();

        StartRequestDetails pcuInWork=webClientBuilder.build()
                .post()
                .uri(retrieveInWorkUrl)
                .bodyValue(pcuInWorkRequest)
                .retrieve()
                .bodyToMono(StartRequestDetails.class)
                .block();

        if (pcuInWork == null || pcuInWork.getPcu()==null) {
            throw new AssemblyException(1706, pcuBO);
        }
        IsExist isExist = IsExist.builder().site(site).item(component.getComponent()).build();
        Item item = retrieveItem(isExist);
        if(item==null || item.getItem()==null){
            throw new AssemblyException(4306,isExist.getItem());
        }

        // From header get BomList and retrieve Bom
        String[] bom = pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
        /*BomRequest bomRequest = BomRequest.builder()
                .site(site)
                .bom(bom[1])
                .revision(bom[2])
                .build();*/
        String pcuBom = "PcuBomBO:" + site + "," + bom[1] + "," + bom[2] + "," + pcuBO;
        BomHeaderRequest bomHeaderRequestmain = BomHeaderRequest.builder()
                .site(site)
                .pcuBomBO(pcuBom)
                .build();

        Bom bomResponse = retrieveBomHeader(bomHeaderRequestmain);

        /*Bom bomResponse = webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
*/
        if (bomResponse == null || bomResponse.getBomComponentList()==null|| bomResponse.getBomComponentList().isEmpty()) {
            throw new AssemblyException(200, bom[1], bom[2]);
        }

        double requiredQty=0.0;
        String dataType = null;
        for (BomComponent bomComponent : bomResponse.getBomComponentList()) {
            if (bomComponent.getComponent().equals(component.getComponent())&&bomComponent.getAssySequence().equals(component.getSequence())) {
                requiredQty=Double.parseDouble(bomComponent.getAssyQty());
                if (bomComponent.getAssemblyDataTypeBo() == null || bomComponent.getAssemblyDataTypeBo().isEmpty()) {
                    dataType = item.getAssemblyDataType();
                } else {
                    dataType = bomComponent.getAssemblyDataTypeBo();
                }
                if(dataType!=null&&!dataType.isEmpty()) {
                    checkRequiredFields(site, dataType, component);
                }
                component.setSequence(bomComponent.getAssySequence());
                break;
            }
//            else {
//                component.setNonBom(true);
//            }
        }

        // From dataList, if it has component as dataType, checking bomComponentList and assigning sequence
        // From dataList, if it has inventory and given inventoryBo is not empty, then retrieveInventory and reduce the remainingQty
        for (AssemblyData assemblyData : component.getAssemblyDataList()) {
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
                    if(!parentAssembly) {
                        parentAssembly = true;
                        childPcu = assemblyData.getValue();
                    }
                }
            }if ((assemblyData.getDataField().equalsIgnoreCase("PlannedMaterial"))||(assemblyData.getDataField().equalsIgnoreCase("planned material"))||(assemblyData.getDataField().equalsIgnoreCase("material"))){
                if(assemblyData.getValue()!=null&&!assemblyData.getValue().isEmpty()) {
                    IsExist isExistDataField=IsExist.builder().site(site).item(assemblyData.getValue()).build();
                    Item itemDataField=retrieveItem(isExistDataField);
                    if(itemDataField==null||itemDataField.getItem()==null||itemDataField.getItem().isEmpty()){
                        throw new AssemblyException(3209,assemblyData.getValue());
                    }
                }

            }
        }

        Pcu pcu = Pcu.builder()
                .pcuBo(pcuBO)
                .build();

        List<Pcu> pcus = new ArrayList<>();
        pcus.add(pcu);

        String pcuBomBO = "PcuBomBO:" + site + "," + bom[1] + "," + bom[2] + "," + pcuBO;

        BomHeaderRequest bomHeaderRequest = BomHeaderRequest.builder()
                .site(site)
                .pcuBomBO(pcuBomBO)
                .build();

        Bom bomList = retrieveBomHeader(bomHeaderRequest);

        for (BomComponent bomComponent : bomList.getBomComponentList()) {
            if (component.getComponent().equalsIgnoreCase(bomComponent.getComponent())&&component.getSequence().equals(bomComponent.getAssySequence())) {
                String qty = String.format("%.6f", (Double.parseDouble(bomComponent.getAssembledQty()) + Double.parseDouble(component.getQty())));
                if(Double.parseDouble(qty)>(requiredQty)){
                    throw new AssemblyException(1705);
                }
                bomComponent.setAssembledQty(qty);
            }
        }

        bomHeaderRequest.setBom(bomList);
        bomHeaderRequest.setPcuBos(pcus);

        String tags = null;
        // Generate tags based on AssemblyData using the combination generator
        List<String> updatedTagsList = generateCombinations(site,component.getAssemblyDataList());
        if(updatedTagsList!= null && !updatedTagsList.isEmpty()) {
           tags = String.join(",", updatedTagsList);
        }
        // Create or update Assembly
        Assembly assembly = new Assembly();
        assembly.setPcuBO(pcuBO);
        assembly.setSite(site);
        assembly.setItemBO(pcuHeaderResponse.getItemBO());
        assembly.setPcuBomBO(pcuHeaderResponse.getBomList().get(0).getPcuBomBO());
        assembly.setPcuRouterBO(pcuHeaderResponse.getRouterList().get(0).getPcuRouterBO());
        assembly.setShopOrderBO(pcuHeaderResponse.getShopOrderBO());
        assembly.setParentOrderBO(pcuHeaderResponse.getParentOrderBO());
        assembly.setParentPcuBO(pcuHeaderResponse.getParentPcuBO());

int level=0;
        /////////////////////////////////////////////////////////////////////////////////////////////////
        if(parentPcuBO != null && !parentPcuBO.isEmpty() && !parentPcuBO.equalsIgnoreCase(pcuBO))
        {
            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, parentPcuBO);
            if(existingAssembly!= null && existingAssembly.getPcuBO() != null)
            {
                List<Component> updatedComponentList = insertComponent(pcuBO,existingAssembly.getComponentList(),component);
                existingAssembly.setComponentList(updatedComponentList);
                assemblyRepository.save(existingAssembly);
                return true;
            }
        }
        //return and replace skip it for now.
        ////
        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);

            if (existingAssembly.getTags() != null && !existingAssembly.getTags().isEmpty()) {
                assembly.setTags("," + tags);
            } else {
                assembly.setTags(tags);
            }
            if(parentAssembly) {
                List<Assembly> childAssemblyList = new ArrayList<>();
                Assembly childAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, "PcuBO:" + site + "," + childPcu);
                if (childAssembly != null && childAssembly.getPcuBO() != null && !childAssembly.getPcuBO().isEmpty()) {
                    level+= childAssembly.getLevel();
                    childAssembly.setImmediateParent(pcuBO);
                    childAssembly.setMainParent(pcuBO);

                    Assembly newChildASsembly= Assembly.builder().pcuBO("PcuBO:"+site+","+ childPcu).build();
                    childAssemblyList.add(newChildASsembly);
                    component.setChildAssembly(childAssemblyList);
                }
            }


            component.setLevel(level);
            List<Integer> levels= new ArrayList<>();
            for(Component component1: existingAssembly.getComponentList()){
                        levels.add(component1.getLevel());
            }
            int maxLevel = levels.stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0);
            existingAssembly.setLevel(maxLevel);


            component.setCreatedDateTime(LocalDateTime.now());
            component.setMainParent(pcuBO);
            component.setImmediateParent(pcuBO);
            List<Component> updatedComponentList = updateOrCreateComponent(existingAssembly.getComponentList(), component);

            existingAssembly.setComponentList(updatedComponentList);
            existingAssembly.setActive(1);
            existingAssembly.setMainParent(pcuBO);

            existingAssembly.setImmediateParent(pcuBO);
            AssemblyGenealogy assemblyGenealogy = buildAssemblyGenealogy(existingAssembly);
            assemblyGenealogy.setMainParent(pcuBO);
            assemblyGenealogy.setImmediateParent(pcuBO);
            assemblyGenealogyRepository.save(assemblyGenealogy);
            assemblyRepository.save(assembly);
           // Boolean isDeleted = deleteAssDataRecord(assembly);
            Boolean isUpdated = updateMainPcu(assembly,pcuBO);
            mainParent = "";
            updateBomHeader(bomHeaderRequest);
            return true;
        }else {
            if(parentAssembly) {
                List<Assembly> childAssemblyList = new ArrayList<>();
                Assembly childAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site,"PcuBO:"+site+","+ childPcu);
                if (childAssembly != null && childAssembly.getPcuBO() != null && !childAssembly.getPcuBO().isEmpty()) {
                    level+= childAssembly.getLevel();
                    childAssembly.setImmediateParent(pcuBO);
                    childAssembly.setMainParent(pcuBO);


                    Assembly newChildASsembly= Assembly.builder().pcuBO("PcuBO:"+site+","+ childPcu).build();
                    childAssemblyList.add(newChildASsembly);
                    component.setChildAssembly(childAssemblyList);
                }
            }
            assembly.setLevel(level);
            component.setLevel(level);
            assembly.setTags(tags);
            component.setCreatedDateTime(LocalDateTime.now());
            List<Component> componentList = new ArrayList<>();
            componentList.add(component);
            assembly.setComponentList(componentList);
            assembly.setActive(1);
            assembly.setMainParent(pcuBO);
            assembly.setImmediateParent(pcuBO);
            component.setMainParent(pcuBO);
            component.setImmediateParent(pcuBO);
            AssemblyGenealogy assemblyGenealogy = buildAssemblyGenealogy(assembly);
            assemblyGenealogy.setMainParent(pcuBO);
            assemblyGenealogy.setImmediateParent(pcuBO);
            assemblyGenealogyRepository.save(assemblyGenealogy);
            assemblyRepository.save(assembly);
          //  Boolean isDeleted = deleteAssDataRecord(assembly);
            Boolean isUpdated = updateMainPcu(assembly, pcuBO);
            mainParent = "";
            updateBomHeader(bomHeaderRequest);
            return true;
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////


    public List<Component> updateOrCreateComponent(List<Component> componentList, Component newComponent) {
        // Check if a matching component exists in the component list
        boolean componentExists = false;
        for (Component existingComponent : componentList) {
            if (existingComponent.getSequence().equals(newComponent.getSequence()) &&
                    existingComponent.getComponent().equals(newComponent.getComponent()) &&
                    existingComponent.getOperation().equals(newComponent.getOperation()) &&
                    existingComponent.getAssembledBy().equals(newComponent.getAssembledBy())) {

                // Update existing component's quantity
                String updatedQuantity = String.valueOf(Double.parseDouble(existingComponent.getQty()) + Double.parseDouble(newComponent.getQty()));
                existingComponent.setQty(updatedQuantity);

                // Add assembly data to the existing component's assembly data list
                existingComponent.getAssemblyDataList().addAll(newComponent.getAssemblyDataList());
                existingComponent.getChildAssembly().addAll(newComponent.getChildAssembly());

                componentExists = true;
                break; // No need to continue searching
            }
        }

        // If a matching component doesn't exist, add the new component to the list
        if (!componentExists) {
            componentList.add(newComponent);
        }

        return componentList;
    }

    public Boolean deleteAssDataRecord(Assembly assembly)
    {
        Boolean isDeleted = false;
        for(Component component : assembly.getComponentList()) {
            if (component.getChildAssembly() != null && !component.getChildAssembly().isEmpty() && component.getChildAssembly().get(0) != null) {
                for (Assembly childAssembly : component.getChildAssembly()) {
                    Assembly retrievedAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, childAssembly.getSite(), childAssembly.getPcuBO());
                    if (retrievedAssembly != null && retrievedAssembly.getPcuBO() != null) {
                        retrievedAssembly.setActive(0);
                        assemblyRepository.save(retrievedAssembly);
                        isDeleted = true;
                    }
                }
            }
        }
        return isDeleted;
    }

    public Boolean updateMainPcu(Assembly assembly, String pcuBO)
    {

        Boolean isUpdated = false;
        assembly.setMainParent(pcuBO);
//        if(immediateParent != null && !immediateParent.isEmpty()) {
//            assembly.setImmediateParent(immediateParent);
//        }
        for(Component component : assembly.getComponentList())
        {
            component.setMainParent(pcuBO);
            if(component.getChildAssembly() != null && !component.getChildAssembly().isEmpty() && component.getChildAssembly().get(0) != null)
            {
                for (Assembly childAssembly : component.getChildAssembly()) {
                    updateMainPcu(childAssembly, pcuBO);
                }
            }
        }
        return isUpdated;
    }

    public AssemblyGenealogy buildAssemblyGenealogy(Assembly assembly)
    {
        AssemblyGenealogy assemblyGenealogy = AssemblyGenealogy.builder()
                .site(assembly.getSite())
                .pcuBomBO(assembly.getPcuBomBO())
                .pcuBO(assembly.getPcuBO())
                .shopOrderBO(assembly.getShopOrderBO())
                .mainParent(assembly.getMainParent())
                .immediateParent(assembly.getImmediateParent())
                .itemBO(assembly.getItemBO())
                .pcuRouterBO(assembly.getPcuRouterBO())
                .tags(assembly.getTags())
                .parentOrderBO(assembly.getParentOrderBO())
                .parentPcuBO(assembly.getParentPcuBO())
                .level(assembly.getLevel())
                .componentList(assembly.getComponentList())
                .active(1)
                .build();
        return assemblyGenealogy;
    }
    public List<Component> insertComponent(String pcuBO, List<Component> componentList, Component componentToAdd)
    {
        List<Component> components = componentList;
        if(components != null)
        {
            for(Component component : components)
            {
                if(component.getChildAssembly() != null && component.getChildAssembly().get(0) != null) {
                    for (Assembly childAssembly : component.getChildAssembly()){
                        if (childAssembly.getPcuBO().equalsIgnoreCase(pcuBO)) {
                            componentToAdd.setCreatedDateTime(LocalDateTime.now());
                            childAssembly.getComponentList().add(componentToAdd);
                            break;
                        }
                        if (childAssembly.getComponentList() != null && !childAssembly.getComponentList().isEmpty()) {
                            insertComponent(pcuBO, childAssembly.getComponentList(), componentToAdd);
                        }
                    }
                }
            }
        }
        return components;
    }

    public List<Component> insertNonBomComponent(String pcuBO, List<Component> componentList, Component componentToAdd)
    {
        List<Component> components = componentList;
        if(components != null)
        {
            for(Component component : components)
            {
                if(component.getChildAssembly() != null && component.getChildAssembly().get(0) != null) {
                    for (Assembly childAssembly : component.getChildAssembly()){
                        if (childAssembly.getPcuBO().equalsIgnoreCase(pcuBO)) {
                            componentToAdd.setNonBom(true);
                            componentToAdd.setCreatedDateTime(LocalDateTime.now());
                            childAssembly.getComponentList().add(componentToAdd);
                            break;
                        }
                        if (childAssembly.getComponentList() != null && !childAssembly.getComponentList().isEmpty()) {
                            insertComponent(pcuBO, childAssembly.getComponentList(), componentToAdd);
                        }
                    }
                }
            }
        }
        return components;
    }

    public List<Component> deleteComponent(String pcuBO, List<Component> componentList, Component componentToRemove)
    {
        List<Component> componentsList = componentList;
        if(componentsList != null)
        {
            for(Component component : componentsList)
            {
                if(component.getChildAssembly() != null && component.getChildAssembly().get(0) != null) {
                    for (Assembly childAssembly : component.getChildAssembly()){
                        if (childAssembly.getPcuBO().equalsIgnoreCase(pcuBO)) {
                            List<Component> childAssemblyList = new ArrayList<>();
                            for(Component components : childAssembly.getComponentList())
                            {
                                if(components.getCreatedDateTime().equals(componentToRemove.getCreatedDateTime()) )
                                {
                                    if(components.getQty().equals(componentToRemove.getQty())) {
                                        components.setQty(components.getQty());
                                        components.setRemovedBy(components.getRemovedBy());
                                        components.setRemovedDate(LocalDateTime.now());
                                        components.setRemoved(true);
                                        components.setRemovedOperationBO(components.getOperation());
                                        components.setRemovedResourceBO(components.getResourceBO());
                                        components.setCreatedDateTime(LocalDateTime.now());
                                        components.setPartitionDate(LocalDateTime.now());
                                        childAssemblyList.add(components);
                                        continue;
                                    }else{
                                        components.setQty(String.valueOf(Double.parseDouble(components.getQty()) - Double.parseDouble(componentToRemove.getQty())));

                                        componentToRemove.setQty(componentToRemove.getQty());
                                        componentToRemove.setRemovedBy(componentToRemove.getRemovedBy());
                                        componentToRemove.setRemovedDate(LocalDateTime.now());
                                        componentToRemove.setRemoved(true);
                                        componentToRemove.setRemovedOperationBO(componentToRemove.getOperation());
                                        componentToRemove.setRemovedResourceBO(componentToRemove.getResourceBO());
                                        componentToRemove.setCreatedDateTime(LocalDateTime.now());
                                        componentToRemove.setPartitionDate(LocalDateTime.now());
//                                        childAssembly.getComponentList().add(componentToRemove);
                                        childAssemblyList.add(componentToRemove);
                                        childAssemblyList.add(components);
                                        continue;
                                    }
                                }
                                childAssemblyList.add(components);
                                if (components.getChildAssembly() != null && !components.getChildAssembly().isEmpty()) {
                                    if(components.getChildAssembly().size()>0 && components.getChildAssembly().get(0) != null)
                                    {
                                        for (Assembly subChildAssembly : components.getChildAssembly()) {
                                            deleteComponent(pcuBO, subChildAssembly.getComponentList(), componentToRemove);
                                        }
                                    }
                                }
                            }
                            childAssembly.setComponentList(childAssemblyList);
//                            break;
                        }
                    }
                }
            }
        }
        return componentsList;
    }

//    public List<Component> deleteComponent(String pcuBO, List<Component> componentList, Component componentToRemove) {
//        if (componentList == null) {
//            return componentList;
//        }
//
//        for (Component component : componentList) {
//            if (component.getChildAssembly() != null && !component.getChildAssembly().isEmpty()) {
//                for (Assembly childAssembly : component.getChildAssembly()) {
//                    if (childAssembly.getPcuBO().equalsIgnoreCase(pcuBO)) {
//                        List<Component> componentsToRemove = new ArrayList<>();
//                        for (Component components : childAssembly.getComponentList()) {
//                            if (components.getCreatedDateTime().equals(componentToRemove.getCreatedDateTime())) {
//                                if (components.getQty().equals(component.getQty())) {
//                                    components.setQty(component.getQty());
//                                } else {
//                                    components.setQty(String.valueOf(Double.parseDouble(components.getQty()) - Double.parseDouble(component.getQty())));
//                                    componentsToRemove.add(componentToRemove);
//                                }
//                                components.setRemovedBy(component.getRemovedBy());
//                                components.setRemovedDate(LocalDateTime.now());
//                                components.setRemoved(true);
//                                components.setRemovedOperationBO(component.getOperation());
//                                components.setRemovedResourceBO(component.getResourceBO());
//                                components.setCreatedDateTime(LocalDateTime.now());
//                                components.setPartitionDate(LocalDateTime.now());
//                            }
//                        }
//                        childAssembly.getComponentList().removeAll(componentsToRemove);
//                        insertComponent(pcuBO, childAssembly.getComponentList(), componentToRemove);
//                    }
//                }
//            }
//        }
//        return componentList;
//    }


    // Define a method to generate combinations
    public List<String> generateCombinations(String site,List<AssemblyData> assemblyDataList) {
        List<String> elements = new ArrayList<>();

        for (AssemblyData assemblyData : assemblyDataList) {
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



    private void checkRequiredFields(String site, String dataType, Component component) {
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
                for (AssemblyData assemblyData : component.getAssemblyDataList()) {
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


    private Item retrieveItem(IsExist isExist) {
        Item item = webClientBuilder.build()
                .post()
                .uri(retrieveItemUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Item.class)
                .block();

        return item;
    }

    private void updateBomHeader(BomHeaderRequest bomHeaderRequest) {

        BomHeaderMessageModel bomHeaderResponse = webClientBuilder.build()
                .post()
                .uri(updateBomHeaderUrl)
                .bodyValue(bomHeaderRequest)
                .retrieve()
                .bodyToMono(BomHeaderMessageModel.class)
                .block();
        if(bomHeaderResponse==null || bomHeaderResponse.getMessage_details().getMsg_type().isEmpty()){
            throw new AssemblyException(4305,bomHeaderRequest.getPcuBomBO());
        }

    }

    @Override
    public boolean removeComponent(String site,String parentPcuBO, String pcuBO, Component component, boolean inventoryReturn, boolean inventoryScrap) throws Exception {

        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();
        if(pcuHeaderResponse==null || pcuHeaderResponse.getPcuBO().isEmpty()){
            throw new AssemblyException(2701,pcuBO);
        }String[] bom= pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
        BomRequest bomRequest=BomRequest.builder().site(site).bom(bom[1]).revision(bom[2]).build();
        Bom bomResponse=webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
        if(bomResponse==null||bomResponse.getBomComponentList().isEmpty()){
            throw new AssemblyException(200,bom[1],bom[2]);
        }
        Assembly assembly=new Assembly();
        assembly.setPcuBO(pcuBO);
        assembly.setSite(site);
        assembly.setItemBO(pcuHeaderResponse.getItemBO());
        assembly.setPcuBomBO(pcuHeaderResponse.getBomList().get(0).getPcuBomBO());
        assembly.setPcuRouterBO(pcuHeaderResponse.getRouterList().get(0).getPcuRouterBO());
        assembly.setShopOrderBO(pcuHeaderResponse.getShopOrderBO());
        assembly.setParentOrderBO(pcuHeaderResponse.getParentOrderBO());
        assembly.setParentPcuBO(pcuHeaderResponse.getParentPcuBO());
        if(parentPcuBO != null && !parentPcuBO.isEmpty() && !parentPcuBO.equalsIgnoreCase(pcuBO))
        {
            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, parentPcuBO);
            if(existingAssembly!= null && existingAssembly.getPcuBO() != null)
            {
                List<Component> updatedComponentList = deleteComponent(pcuBO,existingAssembly.getComponentList(),component);
                existingAssembly.setComponentList(updatedComponentList);
                assemblyRepository.save(existingAssembly);
                return true;
            }
        }
        if(assemblyRepository.existsByActiveAndSiteAndPcuBO(1,site,pcuBO)) {
            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);


            if(existingAssembly.getTags()!=null&& !existingAssembly.getTags().isEmpty()) {
                boolean containsInventory = existingAssembly.getComponentList().stream()
                        .flatMap(comp -> comp.getAssemblyDataList().stream())
                        .map(AssemblyData::getDataField)
                        .anyMatch(dataField -> dataField.contains("inventory"));

//                if (!containsInventory) {
//                    throw new AssemblyException(4303);
//                }


                //if(component and stepId matches checks for qty, if both are same(full),and if has inventory return adding qty back to inventory and update, if partial means,new entry

                for (AssemblyData assemblyData : component.getAssemblyDataList()) {

                    if (assemblyData.getDataField().equalsIgnoreCase("inventory") && !component.getInventoryBO().isEmpty()) {
                        if (inventoryReturn) {
                            Inventory inventoryRequest = Inventory.builder().site(site).inventoryId(component.getInventoryBO()).build();
                            Inventory inventory = webClientBuilder.build()
                                    .post()
                                    .uri(retrieveInventoryUrl)
                                    .bodyValue(inventoryRequest)
                                    .retrieve()
                                    .bodyToMono(Inventory.class)
                                    .block();
                            if (inventory == null || inventory.getInventoryId().isEmpty()) {
                                throw new AssemblyException(1700, component.getInventoryBO());
                            }
                            if ((Integer.parseInt(component.getQty())) > Double.parseDouble(inventory.getRemainingQty())) {
                                throw new AssemblyException(1701);
                            }
                            inventory.setRemainingQty(inventory.getRemainingQty() + Integer.parseInt(component.getQty()));
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
                        } else {
                            return false;
                        }
                    }
                }
            }
            Boolean partialRemoval = false;
            for(Component components : existingAssembly.getComponentList())
            {
                if(components.getCreatedDateTime().equals(component.getCreatedDateTime()) )
                {
                    if(components.getQty().equals(component.getQty())) {
                        components.setQty(component.getQty());
                        components.setRemovedBy(component.getRemovedBy());
                        components.setRemovedDate(LocalDateTime.now());
                        components.setRemoved(true);
                        components.setRemovedOperationBO(component.getOperation());
                        components.setRemovedResourceBO(component.getResourceBO());
                        components.setCreatedDateTime(LocalDateTime.now());
                        components.setPartitionDate(LocalDateTime.now());
                    }else{
                        components.setQty(String.valueOf(Double.parseDouble(components.getQty()) - Double.parseDouble(component.getQty())));

                        partialRemoval = true;
                        component.setQty(component.getQty());
                        component.setRemovedBy(component.getRemovedBy());
                        component.setRemovedDate(LocalDateTime.now());
                        component.setRemoved(true);
                        component.setRemovedOperationBO(component.getOperation());
                        component.setRemovedResourceBO(component.getResourceBO());
                        component.setCreatedDateTime(LocalDateTime.now());
                        component.setPartitionDate(LocalDateTime.now());
                    }
                }
            }
            Pcu pcu=Pcu.builder()
                    .pcuBo(pcuBO)
                    .build();
            List<Pcu> pcus=new ArrayList<>();
            pcus.add(pcu);
            String pcuBomBO= "PcuBomBO:"+site+","+bom[1]+","+bom[2]+","+pcuBO;

            BomHeaderRequest bomHeaderRequest=BomHeaderRequest.builder()
                    .site(site)
                    .pcuBomBO(pcuBomBO)
                    .bom(bomResponse)
                    .pcuBos(pcus)
                    .build();
            Bom bomList=retrieveBomHeader(bomHeaderRequest);

            for(BomComponent bomComponent:bomList.getBomComponentList())  {
                String dataType=null;
                if(bomComponent.getAssemblyDataTypeBo()==null|| bomComponent.getAssemblyDataTypeBo().isEmpty())
                {
                    IsExist isExist=IsExist.builder().site(site).item(component.getComponent()).build();
                    Item item= retrieveItem(isExist);
                    if(item==null || item.getItem()==null){
                        throw new AssemblyException(4306,isExist.getItem());
                    }
                    dataType=item.getAssemblyDataType();
                }
                else{
                    dataType=bomComponent.getAssemblyDataTypeBo();
                }
                checkRequiredFields(site,dataType,component);
                if(component.getComponent().equalsIgnoreCase(bomComponent.getComponent())){
                    String qty= String.format("%.6f", (Double.parseDouble(bomComponent.getAssembledQty())-Double.parseDouble(component.getQty())));
                    bomComponent.setAssembledQty(qty);
                }
            }

            bomHeaderRequest.setBom(bomList);

            updateBomHeader(bomHeaderRequest);
            if(partialRemoval)
            {
                existingAssembly.getComponentList().add(component);
            }
            assembly.setComponentList(existingAssembly.getComponentList());
            assembly.setActive(1);
            assemblyRepository.save(assembly);
            return true;
        }
        else {
            throw new AssemblyException(4302,pcuBO);
        }
    }

    //    @Override
//    public boolean groupAddComponent(String site, String pcuBO, List<Component> components) throws Exception {
//        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
//                .site(site)
//                .pcuBO(pcuBO)
//                .build();
//
//        PcuHeader pcuHeaderResponse = webClientBuilder.build()
//                .post()
//                .uri(readPcuUrl)
//                .bodyValue(pcuHeaderRequest)
//                .retrieve()
//                .bodyToMono(PcuHeader.class)
//                .block();
//
//        if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO().isEmpty()) {
//            throw new AssemblyException(2701, pcuBO);
//        }
//
//        String[] bom = pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
//        BomRequest bomRequest = BomRequest.builder()
//                .site(site)
//                .bom(bom[1])
//                .revision(bom[2])
//                .build();
//
//        Bom bomResponse = webClientBuilder.build()
//                .post()
//                .uri(retrieveBomUrl)
//                .bodyValue(bomRequest)
//                .retrieve()
//                .bodyToMono(Bom.class)
//                .block();
//
//        if (bomResponse == null || bomResponse.getBomComponentList().isEmpty()) {
//            throw new AssemblyException(200, bom[1], bom[2]);
//        }
//
//        for (Component component : components) {
//            for (AssemblyData assemblyData : component.getAssemblyDataList()) {
//                if (assemblyData.getDataField().equalsIgnoreCase("component")) {
//                    for (BomComponent bomComponent : bomResponse.getBomComponentList()) {
//                        if (bomComponent.getComponent().equals(component.getComponent())) {
//                            component.setSequence(bomComponent.getAssySequence());
//                        }
//                    }
//                }
//            }
//        }
//
//        Assembly assembly = new Assembly();
//        assembly.setPcuBO(pcuBO);
//        assembly.setSite(site);
//        assembly.setItemBO(pcuHeaderResponse.getItemBO());
//        assembly.setPcuBomBO(pcuHeaderResponse.getBomList().get(0).getPcuBomBO());
//        assembly.setPcuRouterBO(pcuHeaderResponse.getRouterList().get(0).getPcuRouterBO());
//        assembly.setShopOrderBO(pcuHeaderResponse.getShopOrderBO());
//
//        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
//            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
//
//            if (existingAssembly.getTags() != null && !existingAssembly.getTags().isEmpty()) {
//                String[] tags = existingAssembly.getTags().split("_");
//                List<String> existingTags = new ArrayList<>(Arrays.asList(tags));
//
//                for (Component component : components) {
//                    String dataField = component.getAssemblyDataList().get(0).getDataField();
//                    if (!existingTags.contains(dataField)) {
//                        existingTags.add(dataField);
//                    }
//                }
//
//                String concatenatedTags = String.join("_", existingTags);
//                assembly.setTags(concatenatedTags);
//            } else {
//                List<String> dataFields = components.stream()
//                        .map(c -> c.getAssemblyDataList().get(0).getDataField())
//                        .collect(Collectors.toList());
//                String concatenatedTags = String.join("_", dataFields);
//                assembly.setTags(concatenatedTags);
//            }
//
//            existingAssembly.getComponentList().addAll(components);
//            assembly.setComponentList(existingAssembly.getComponentList());
//            assembly.setActive(1);
//            assemblyRepository.save(assembly);
//            return true;
//        } else {
//            assembly.setComponentList(components);
//            assembly.setActive(1);
//            assemblyRepository.save(assembly);
//            return true;
//        }
//    }
    @Override
    public boolean groupAddComponent(String site, String pcuBO, List<Component> components) throws Exception {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();

        if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO().isEmpty()) {
            throw new AssemblyException(2701, pcuBO);
        }

        String[] bom = pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
        BomRequest bomRequest = BomRequest.builder()
                .site(site)
                .bom(bom[1])
                .revision(bom[2])
                .build();

        Bom bomResponse = webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();

        if (bomResponse == null || bomResponse.getBomComponentList().isEmpty()) {
            throw new AssemblyException(200, bom[1], bom[2]);
        }
        for (Component component : components) {
            String dataType=null;
            for (BomComponent bomComponent : bomResponse.getBomComponentList()) {
                if(bomComponent.getAssemblyDataTypeBo()==null|| bomComponent.getAssemblyDataTypeBo().isEmpty())
                {
                    IsExist isExist=IsExist.builder().site(site).item(component.getComponent()).build();
                    Item item= retrieveItem(isExist);
                    if(item==null || item.getItem()==null){
                        throw new AssemblyException(4306,isExist.getItem());
                    }
                    dataType=item.getAssemblyDataType();
                }
                else{
                    dataType=bomComponent.getAssemblyDataTypeBo();
                }
                checkRequiredFields(site,dataType,component);
                if (bomComponent.getComponent().equals(component.getComponent())) {
                    component.setSequence(bomComponent.getAssySequence());
                }
                else{
                    component.setNonBom(true);
                }
            }

            for (AssemblyData assemblyData : component.getAssemblyDataList()) {

                if(assemblyData.getDataField().equalsIgnoreCase("inventory") && !component.getInventoryBO().isEmpty()){
                    Inventory inventoryRequest=Inventory.builder().site(site).inventoryId(component.getInventoryBO()).build();
                    Inventory inventory=webClientBuilder.build()
                            .post()
                            .uri(retrieveInventoryUrl)
                            .bodyValue(inventoryRequest)
                            .retrieve()
                            .bodyToMono(Inventory.class)
                            .block();
                    if(inventory==null||inventory.getInventoryId().isEmpty()){
                        throw new AssemblyException(1700,component.getInventoryBO());
                    }
                    if((Double.parseDouble(component.getQty())) > Double.parseDouble(inventory.getRemainingQty())){
                        throw new AssemblyException(1701) ;
                    }
                    inventory.setRemainingQty(String.valueOf(Double.parseDouble(inventoryRequest.getRemainingQty())-Double.parseDouble((component.getQty()))));
                    InventoryMessageModel inventoryUpdate=webClientBuilder.build()
                            .post()
                            .uri(updateInventoryUrl)
                            .bodyValue(inventory)
                            .retrieve()
                            .bodyToMono(InventoryMessageModel.class)
                            .block();
                    if(inventoryUpdate==null||inventoryUpdate.getResponse().getInventoryId().isEmpty()){
                        throw new AssemblyException(1702,component.getInventoryBO());
                    }
                }
            }

            Pcu pcu=Pcu.builder()
                    .pcuBo(pcuBO)
                    .build();
            List<Pcu> pcus=new ArrayList<>();
            pcus.add(pcu);
            String pcuBomBO= "PcuBomBO:"+site+","+bom[1]+","+bom[2]+","+pcuBO;

            BomHeaderRequest bomHeaderRequest=BomHeaderRequest.builder()
                    .site(site)
                    .pcuBomBO(pcuBomBO)
                    .bom(bomResponse)
                    .pcuBos(pcus)
                    .build();
            Bom bomList=retrieveBomHeader(bomHeaderRequest);

            for(BomComponent bomComponent:bomList.getBomComponentList())  {
                if(component.getComponent().equalsIgnoreCase(bomComponent.getComponent())){
                    String qty= String.format("%.6f", (Double.parseDouble(bomComponent.getAssembledQty())+Double.parseDouble(component.getQty())));
                    bomComponent.setAssembledQty(qty);
                }
            }

            bomHeaderRequest.setBom(bomList);

            updateBomHeader(bomHeaderRequest);


        }
        Assembly assembly = new Assembly();
        assembly.setPcuBO(pcuBO);
        assembly.setSite(site);
        assembly.setItemBO(pcuHeaderResponse.getItemBO());
        assembly.setPcuBomBO(pcuHeaderResponse.getBomList().get(0).getPcuBomBO());
        assembly.setPcuRouterBO(pcuHeaderResponse.getRouterList().get(0).getPcuRouterBO());
        assembly.setShopOrderBO(pcuHeaderResponse.getShopOrderBO());
        assembly.setParentOrderBO(pcuHeaderResponse.getParentOrderBO());
        assembly.setParentPcuBO(pcuHeaderResponse.getParentPcuBO());
        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);

            if (existingAssembly.getTags() != null && !existingAssembly.getTags().isEmpty()) {
                Set<String> existingTags = new HashSet<>(Arrays.asList(existingAssembly.getTags().split("_")));
                List<String> updatedTags = new ArrayList<>(existingTags);

                for (Component component : components) {
                    for (AssemblyData assemblyData : component.getAssemblyDataList()) {
                        String dataField = assemblyData.getDataField();
                        if (!existingTags.contains(dataField)) {
                            updatedTags.add(dataField);
                        }
                    }
                    component.setCreatedDateTime(LocalDateTime.now());
                }

                assembly.setTags(String.join("_", updatedTags));
            } else {
                List<String> dataFields = new ArrayList<>();
                for (Component component : components) {
                    for (AssemblyData assemblyData : component.getAssemblyDataList()) {
                        dataFields.add(assemblyData.getDataField());
                    }
                    component.setCreatedDateTime(LocalDateTime.now());
                }
                assembly.setTags(String.join("_", dataFields));
            }

            existingAssembly.getComponentList().addAll(components);
            assembly.setComponentList(existingAssembly.getComponentList());
            assembly.setActive(1);
            assemblyRepository.save(assembly);
            return true;
        } else {
            List<String> dataFields = new ArrayList<>();
            for (Component component : components) {
                for (AssemblyData assemblyData : component.getAssemblyDataList()) {
                    dataFields.add(assemblyData.getDataField());
                }
                component.setCreatedDateTime(LocalDateTime.now());
            }
            assembly.setTags(String.join("_", dataFields));
            assembly.setComponentList(components);
            assembly.setActive(1);
            assemblyRepository.save(assembly);
            return true;
        }
    }



    @Override
    public boolean groupRemoveComponent(String site, String pcuBO, List<Component> components, boolean inventoryReturn, boolean inventoryScrap) throws Exception {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();

        if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO().isEmpty()) {
            throw new AssemblyException(2701, pcuBO);
        }

        String[] bom = pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
        BomRequest bomRequest = BomRequest.builder()
                .site(site)
                .bom(bom[1])
                .revision(bom[2])
                .build();

        Bom bomResponse = webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();

        if (bomResponse == null || bomResponse.getBomComponentList().isEmpty()) {
            throw new AssemblyException(200, bom[1], bom[2]);
        }

        Assembly assembly = new Assembly();
        assembly.setPcuBO(pcuBO);
        assembly.setSite(site);
        assembly.setItemBO(pcuHeaderResponse.getItemBO());
        assembly.setPcuBomBO(pcuHeaderResponse.getBomList().get(0).getPcuBomBO());
        assembly.setPcuRouterBO(pcuHeaderResponse.getRouterList().get(0).getPcuRouterBO());
        assembly.setShopOrderBO(pcuHeaderResponse.getShopOrderBO());
        assembly.setParentOrderBO(pcuHeaderResponse.getParentOrderBO());
        assembly.setParentPcuBO(pcuHeaderResponse.getParentPcuBO());

        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);

            if (existingAssembly.getTags() != null && !existingAssembly.getTags().isEmpty()) {
                Set<String> existingTags = new HashSet<>(Arrays.asList(existingAssembly.getTags().split("_")));
                List<String> updatedTags = new ArrayList<>(existingTags);

                List<Component> componentsToRemove = new ArrayList<>();

                for (Component component : components) {

                    component.setQty(component.getQty());
                    component.setRemovedBy(component.getRemovedBy());
                    component.setRemovedDate(LocalDateTime.now());
                    component.setRemoved(true);
                    component.setRemovedOperationBO(component.getOperation());
                    component.setRemovedResourceBO(component.getResourceBO());
                    component.setCreatedDateTime(LocalDateTime.now());
                    component.setPartitionDate(LocalDateTime.now());

                    Pcu pcu=Pcu.builder()
                            .pcuBo(pcuBO)
                            .build();
                    List<Pcu> pcus=new ArrayList<>();
                    pcus.add(pcu);
                    String pcuBomBO= "PcuBomBO:"+site+","+bom[1]+","+bom[2]+","+pcuBO;

                    BomHeaderRequest bomHeaderRequest=BomHeaderRequest.builder()
                            .site(site)
                            .pcuBomBO(pcuBomBO)
                            .bom(bomResponse)
                            .pcuBos(pcus)
                            .build();
                    Bom bomList=retrieveBomHeader(bomHeaderRequest);

                    for(BomComponent bomComponent:bomList.getBomComponentList())  {
                        if(component.getComponent().equalsIgnoreCase(bomComponent.getComponent())){
                            String qty= String.format("%.6f", (Double.parseDouble(bomComponent.getAssembledQty())-Double.parseDouble(component.getQty())));
                            bomComponent.setAssembledQty(qty);
                        }
                    }

                    bomHeaderRequest.setBom(bomList);

                    updateBomHeader(bomHeaderRequest);



                }

                existingAssembly.getComponentList().addAll(componentsToRemove);

                for (Component componentToRemove : components) {
                    if (componentToRemove.getAssemblyDataList().stream().anyMatch(assemblyData -> assemblyData.getDataField().equalsIgnoreCase("inventory"))) {
                        if (inventoryReturn) {
                            if (componentToRemove.getInventoryBO() != null && !componentToRemove.getInventoryBO().isEmpty()) {
                                Inventory inventoryRequest = Inventory.builder().site(site).inventoryId(componentToRemove.getInventoryBO()).build();
                                Inventory inventory = webClientBuilder.build()
                                        .post()
                                        .uri(retrieveInventoryUrl)
                                        .bodyValue(inventoryRequest)
                                        .retrieve()
                                        .bodyToMono(Inventory.class)
                                        .block();
                                if (inventory == null || inventory.getInventoryId() == null || inventory.getInventoryId().isEmpty()) {
                                    throw new AssemblyException(1700, componentToRemove.getInventoryBO());
                                }
                                if ((Double.parseDouble(componentToRemove.getQty())) >Double.parseDouble( inventory.getRemainingQty())) {
                                    throw new AssemblyException(1701);
                                }
                                inventory.setRemainingQty(inventoryRequest.getRemainingQty() + Integer.parseInt(componentToRemove.getQty()));
                                InventoryMessageModel inventoryUpdate = webClientBuilder.build()
                                        .post()
                                        .uri(updateInventoryUrl)
                                        .bodyValue(inventory)
                                        .retrieve()
                                        .bodyToMono(InventoryMessageModel.class)
                                        .block();
                                if (inventoryUpdate == null || inventoryUpdate.getResponse().getInventoryId() == null || inventoryUpdate.getResponse().getInventoryId().isEmpty()) {
                                    throw new AssemblyException(1702, componentToRemove.getInventoryBO());
                                }
                            }
                        } else {
                            // Handle inventory not returned
                            return false;

                        }
                    }
                }

                assembly.setTags(String.join("_", updatedTags));
            } else {
                assembly.setTags(existingAssembly.getTags());
            }

            assembly.setComponentList(existingAssembly.getComponentList());
            assembly.setActive(1);
            assemblyRepository.save(assembly);
            return true;
        } else {
            throw new AssemblyException(4302, pcuBO);
        }
    }



    @Override
    public List<Component> findAssembledComponent(String site, String pcuBO) throws Exception {
        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
            List<Component> components = assembly.getComponentList().stream()
                    .filter(component -> !component.isRemoved())
                    .collect(Collectors.toList());
            return components;
        } else {
            throw new AssemblyException(4302, pcuBO);
        }
    }

    @Override
    public List<Component> findAssembledByComponent(String site, String pcuBO, String component, boolean removedComponentNeeded) throws Exception {
        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
            List<Component> components = assembly.getComponentList().stream()
                    .filter(comp -> comp.getComponent().equals(component) && comp.isRemoved() == removedComponentNeeded)
                    .collect(Collectors.toList());
            return components;
        } else {
            throw new AssemblyException(4302, pcuBO);
        }
    }


    @Override
    public List<Component> findAssembledComponentHistory(String site, String pcuBO) throws Exception {
        if(assemblyRepository.existsByActiveAndSiteAndPcuBO(1,site,pcuBO)) {
            Assembly assembly=assemblyRepository.findByActiveAndSiteAndPcuBO(1,site,pcuBO);
            return assembly.getComponentList();
        }
        else {
            throw new AssemblyException(4302, pcuBO);
        }
    }

    @Override
    public List<Component> findAssembledComponentByOperation(String site, String pcuBO, String operation, boolean removedComponentNeeded) throws Exception {
        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
            List<Component> components = assembly.getComponentList().stream()
                    .filter(component -> component.getOperation().equals(operation) && component.isRemoved() == removedComponentNeeded)
                    .collect(Collectors.toList());
            return components;
        } else {
            throw new AssemblyException(4302, pcuBO);
        }
    }


    @Override
    public List<Component> findAssembledComponentByCustomField(String site, String pcuBO, String dataAttribute, String dataField) throws Exception {
        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
            List<Component> components = assembly.getComponentList().stream()
                    .filter(component ->
                            component.getAssemblyDataList().stream()
                                    .anyMatch(assemblyData ->
                                            assemblyData.getValue().equals(dataAttribute) &&
                                                    assemblyData.getDataField().equals(dataField))
                    )
                    .collect(Collectors.toList());
            return components;
        } else {
            throw new AssemblyException(4302, pcuBO);
        }
    }


    @Override
    public Component findAssembledComponentById(String site, String pcuBO, String id) throws Exception {
        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
            return assembly.getComponentList().stream()
                    .filter(component -> component.getUniqueID().equals(id) && !component.isRemoved())
                    .findFirst()
                    .orElseThrow(() -> new AssemblyException(4304,id));
        } else {
            throw new AssemblyException(4302, pcuBO);
        }
    }

    @Override
    public boolean addNonBomComponent(String site, String pcuBO, Component component) throws Exception {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();
        if(pcuHeaderResponse==null || pcuHeaderResponse.getPcuBO().isEmpty()){
            throw new AssemblyException(2701,pcuBO);
        }
        String[] bom= pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
        BomRequest bomRequest=BomRequest.builder().site(site).bom(bom[1]).revision(bom[2]).build();
        Bom bomResponse=webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
        if(bomResponse==null||bomResponse.getBomComponentList().isEmpty()){
            throw new AssemblyException(200,bom[1],bom[2]);
        }
        for(AssemblyData  assemblyData: component.getAssemblyDataList()){
            if(assemblyData.getDataField().equalsIgnoreCase("inventory") && !component.getInventoryBO().isEmpty()){
                Inventory inventoryRequest=Inventory.builder().site(site).inventoryId(component.getInventoryBO()).build();
                Inventory inventory=webClientBuilder.build()
                        .post()
                        .uri(retrieveInventoryUrl)
                        .bodyValue(inventoryRequest)
                        .retrieve()
                        .bodyToMono(Inventory.class)
                        .block();
                if(inventory==null||inventory.getInventoryId().isEmpty()){
                    throw new AssemblyException(1700,component.getInventoryBO());
                }
                if((Double.parseDouble(component.getQty())) > Double.parseDouble(inventory.getRemainingQty())){
                    throw new AssemblyException(1701) ;
                }
                inventory.setRemainingQty(String.valueOf(Double.parseDouble(inventoryRequest.getRemainingQty())-Double.parseDouble(component.getQty())));
                InventoryMessageModel inventoryUpdate=webClientBuilder.build()
                        .post()
                        .uri(updateInventoryUrl)
                        .bodyValue(inventory)
                        .retrieve()
                        .bodyToMono(InventoryMessageModel.class)
                        .block();
                if(inventoryUpdate==null||inventoryUpdate.getResponse().getInventoryId().isEmpty()){
                    throw new AssemblyException(1702,component.getInventoryBO());
                }
            }
        }
//ask about inventory.
        Assembly assembly=new Assembly();
        assembly.setPcuBO(pcuBO);
        assembly.setSite(site);
        assembly.setItemBO(pcuHeaderResponse.getItemBO());
        assembly.setPcuBomBO(pcuHeaderResponse.getBomList().get(0).getPcuBomBO());
        assembly.setPcuRouterBO(pcuHeaderResponse.getRouterList().get(0).getPcuRouterBO());
        assembly.setShopOrderBO(pcuHeaderResponse.getShopOrderBO());
        assembly.setParentOrderBO(pcuHeaderResponse.getParentOrderBO());
        assembly.setParentPcuBO(pcuHeaderResponse.getParentPcuBO());
        component.setNonBom(true);
        if(assemblyRepository.existsByActiveAndSiteAndPcuBO(1,site,pcuBO)){
            Assembly existingAssembly=assemblyRepository.findByActiveAndSiteAndPcuBO(1,site, pcuBO);
            if (existingAssembly.getTags() != null && !existingAssembly.getTags().isEmpty()) {
                Set<String> existingTags = new HashSet<>(Arrays.asList(existingAssembly.getTags().split("_")));
                List<String> updatedTags = new ArrayList<>(existingTags);

                for (AssemblyData assemblyData : component.getAssemblyDataList()) {
                    String dataField = assemblyData.getDataField();
                    if (!existingTags.contains(dataField)) {
                        updatedTags.add(dataField);
                    }
                }

                assembly.setTags(String.join("_", updatedTags));
            }  else {
                List<String> dataFields = component.getAssemblyDataList().stream()
                        .map(AssemblyData::getDataField)
                        .collect(Collectors.toList());
                assembly.setTags(String.join("_", dataFields));
            }
            component.setModifiedDateTime(LocalDateTime.now());

            existingAssembly.getComponentList().add(component);
            assembly.setComponentList(existingAssembly.getComponentList());
            assembly.setActive(1);
            assemblyRepository.save(assembly);
            return true;
        }else{
            List<Component> componentList=new ArrayList<>();
            List<String> dataFields = component.getAssemblyDataList().stream()
                    .map(AssemblyData::getDataField)
                    .collect(Collectors.toList());
            assembly.setTags(String.join("_", dataFields));
            component.setCreatedDateTime(LocalDateTime.now());
            componentList.add(component);
            assembly.setComponentList(componentList);
            assembly.setActive(1);
            assemblyRepository.save(assembly);
            return true;
        }
    }
    @Override
    public String callExtension(Extension extension) throws Exception {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new AssemblyException(800);
        }
        return extensionResponse;
    }
    public Bom retrieveBomHeader(BomHeaderRequest bomHeaderRequest){

        Bom bom = webClientBuilder.build()
                .post()
                .uri(retrieveBomHeaderUrl)
                .bodyValue(bomHeaderRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
        if (bom == null&& bom.getBom()==null) {
            throw new AssemblyException(800);
        }
        return bom;
    }
    public DataType  retrieveDataType(String site,String bom , String revision, String component) {
        BomRequest bomRequest = BomRequest.builder().site(site).bom(bom).revision(revision).build();
        Bom bomResponse = webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
        if (bomResponse == null || bomResponse.getBomComponentList().isEmpty()) {
            throw new AssemblyException(200, bom, revision);
        }
        String dataType = null;

        for (BomComponent bomComponent : bomResponse.getBomComponentList())
            if (bomComponent.getComponent().equalsIgnoreCase(component)) {
                if(bomComponent.getAssemblyDataTypeBo()==null|| bomComponent.getAssemblyDataTypeBo().isEmpty())
                {
                    IsExist isExist = IsExist.builder().site(site).item(component).build();
                    Item item = retrieveItem(isExist);
                    if(item==null || item.getItem()==null){
                        throw new AssemblyException(4306,isExist.getItem());
                    }
                    dataType = item.getAssemblyDataType();
                } else {
                    dataType = bomComponent.getAssemblyDataTypeBo();
                }
            }

        IsExist dataTypeExist = IsExist.builder().site(site).dataType(dataType).category("Assembly").build();
        DataType dataTypeResponse = webClientBuilder.build()
                .post()
                .uri(retrieveDataTypeUrl)
                .bodyValue(dataTypeExist)
                .retrieve()
                .bodyToMono(DataType.class)
                .block();

        if (dataTypeResponse == null && dataTypeResponse.getDataFieldList().isEmpty()) {
            throw new AssemblyException(4307, dataTypeExist.getDataType());
        }

        for (DataField dataField : dataTypeResponse.getDataFieldList()) {
            IsExist dataTypeExist1 = IsExist.builder().site(site).dataField(dataField.getDataField()).build();
            DataFieldResponse dataFieldResponse = webClientBuilder.build()
                    .post()
                    .uri(retrieveDataFieldUrl)
                    .bodyValue(dataTypeExist1)
                    .retrieve()
                    .bodyToMono(DataFieldResponse.class)
                    .block();

            if (dataFieldResponse == null) {
                throw new AssemblyException(4308, dataTypeExist1.getDataField());
            }

            dataField.setBrowseIcon(dataFieldResponse.isBrowseIcon());

            List<DetailsList> details = dataField.getDetails();
            if (dataFieldResponse.getListDetails() != null) {
                if (details == null) {
                    details = new ArrayList<>();
                }
                details.clear();

                for (ListDetails listDetail : dataFieldResponse.getListDetails()) {
                    DetailsList detail = new DetailsList();
                    detail.setSequence(listDetail.getSequence());
                    detail.setType(listDetail.getType());
                    detail.setFieldValue(listDetail.getFieldValue());
                    detail.setLabelValue(listDetail.getLabelValue());
                    details.add(detail);
                }
                dataField.setDetails(details);
            } else {
                dataField.setDetails(new ArrayList<>());
            }
        }
        return dataTypeResponse;
    }

    @Override
    public ComponentResponse findComponent(String site, String findComponent) throws Exception {
        IsExist isExist =IsExist.builder().site(site).item(findComponent).build();

        Item item=retrieveItem(isExist);
        if(item==null|| item.getItem()==null) {
            isExist.setInventoryId(findComponent);
            Inventory inventory = retrieveInventory(isExist);
            if (inventory.getInventoryId() == null) {
                //isDataFOrmatPresentUrl
                isExist.setDatastring(findComponent);
                List<MainCustomDataObject> mainCustomDataObjects = isDataFormatPresent(isExist);
                List<CustomDataObject> customDataObjects = mainCustomDataObjects.get(0).getCustomdataList();
                List<String> trackableDataField = new ArrayList<>();
                for (CustomDataObject customDataObject : customDataObjects) {
                    isExist.setDataField(customDataObject.getDataField());
                    if (customDataObject.getDataField().equalsIgnoreCase("Component") || customDataObject.getDataField().equalsIgnoreCase("Material")) {
                        return ComponentResponse.builder().component(customDataObject.getValue()).build();
                    } else if (customDataObject.getDataField().equalsIgnoreCase("InventoryId") ||customDataObject.getDataField().equalsIgnoreCase("INV")) {

                        isExist.setInventoryId(customDataObject.getValue());
                        Inventory inventoryDataField = retrieveInventory(isExist);
                        if (inventoryDataField != null) {
                            return ComponentResponse.builder().component(inventoryDataField.getItem()).inventoryId(inventoryDataField.getInventoryId()).qty(Integer.parseInt(inventoryDataField.getRemainingQty())).build();
                        }
                    }
                    else{
                        throw new AssemblyException(1704);
                    }
                }

            } else {
                if (Double.parseDouble(inventory.getRemainingQty()) > 0.0) {
                    return ComponentResponse.builder().component(inventory.getItem()).inventoryId(inventory.getInventoryId()).qty(Integer.parseInt(inventory.getRemainingQty())).build();
                } else {
                    throw new AssemblyException(1703, inventory.getInventoryId());
                }
            }
        }
        else{
            return  ComponentResponse.builder().component(item.getItem()).build();
        }
        throw new AssemblyException(4305,findComponent);
    }

    @Override
    public Assembly getAssembly(String site, String pcuBO) throws Exception {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();

        if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO().isEmpty()) {
            throw new AssemblyException(2701, pcuBO);
        }

        // From header get BomList and retrieve Bom
        String[] bom = pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
        BomRequest bomRequest = BomRequest.builder()
                .site(site)
                .bom(bom[1])
                .revision(bom[2])
                .build();

        Bom bomResponse = webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();

        if (bomResponse == null || bomResponse.getBomComponentList().isEmpty()) {
            throw new AssemblyException(200, bom[1], bom[2]);
        }
        Assembly assembly=assemblyRepository.findByActiveAndSiteAndPcuBO(1,site,pcuBO);
        if(assembly==null|| assembly.getPcuBO()==null){
            throw new AssemblyException(4302,pcuBO);
        }else{
            List<Component> components = assembly.getComponentList();


            Map<String, Component> componentMap = new HashMap<>();

            for (Component component : components) {
                String key = component.getComponent() + component.getOperation();

                if (componentMap.containsKey(key)) {

                    Component existingComponent = componentMap.get(key);
                    existingComponent.setQty(String.valueOf(Integer.parseInt(existingComponent.getQty()) + Integer.parseInt(component.getQty())));
                } else {

                    componentMap.put(key, component);
                }
            }

            List<Assembly> childAssemblyList = assemblyRepository.findByActiveAndSiteAndParentPcuBO(1, site, pcuBO);
            List<Component> combinedComponents = new ArrayList<>(componentMap.values());
            assembly.setComponentList(combinedComponents);

            for (Component component : combinedComponents) {
                for (BomComponent bomComponent : bomResponse.getBomComponentList()) {
                    if (bomComponent.getComponent().equals(component.getComponent()) && bomComponent.getComponentType().equals("Manufactured")) {
                        for (AssemblyData assemblyData : component.getAssemblyDataList()) {
                            if (assemblyData.getDataField().equals("inventoryId")) {
                                String inventoryIdValue = assemblyData.getValue();

                                // Check if inventoryIdValue matches the pcuBO of any childAssembly
                                Assembly matchingChildAssembly = childAssemblyList.stream()
                                        .filter(childAssembly -> inventoryIdValue.equals(childAssembly.getPcuBO()))
                                        .findFirst()
                                        .orElse(null);

                                if (matchingChildAssembly != null) {
                                    component.getChildAssembly().add(matchingChildAssembly);
                                }
                            }
                        }
                    }
                }
            }

        }
        return assembly;
    }

    @Override
    public Assembly retrieveAssembly(String site, String pcuBO) throws Exception {
        if(assemblyRepository.existsByActiveAndSiteAndPcuBO(1,site,pcuBO)) {
            Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
            return assembly;
        }else {
            throw new AssemblyException(4302, pcuBO);
        }
    }

    @Override
    public Assembly getSerializedAssembly(String site, String pcuBO) throws Exception {
        Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);

        if (assembly == null || assembly.getPcuBO() == null) {
            throw new AssemblyException(4302, pcuBO);
        } else {
            List<Component> components = assembly.getComponentList();

            Map<String, Component> componentMap = new HashMap<>();

            for (Component component : components) {
                String key = component.getComponent() + component.getOperation();

                if (componentMap.containsKey(key)) {
                    Component existingComponent = componentMap.get(key);
                    int accumulatedQty = Integer.parseInt(existingComponent.getQty()) + Integer.parseInt(component.getQty());
                    existingComponent.setQty(String.valueOf(accumulatedQty));
                } else {
                    componentMap.put(key, component);
                }
            }

            List<Component> uniqueComponents = new ArrayList<>(componentMap.values());
            assembly.setComponentList(uniqueComponents);

            return assembly;
        }
    }

    @Override
    public Assembly getSerializedAssemblyByPcuAndItem(String site, String pcuBO, String itemBO) throws Exception {
        List<Assembly> assembly = assemblyRepository.findByActiveAndSiteAndParentPcuBOAndItemBO(1, site, pcuBO,itemBO);

        if (assembly == null || assembly.get(0).getPcuBO() == null) {
            throw new AssemblyException(4302, pcuBO);
        } else {
            List<Component> components = assembly.get(0).getComponentList();

            Map<String, Component> componentMap = new HashMap<>();

            for (Component component : components) {
                String key = component.getComponent() + component.getOperation();

                if (componentMap.containsKey(key)) {
                    Component existingComponent = componentMap.get(key);
                    int accumulatedQty = Integer.parseInt(existingComponent.getQty()) + Integer.parseInt(component.getQty());
                    existingComponent.setQty(String.valueOf(accumulatedQty));
                } else {
                    componentMap.put(key, component);
                }
            }

            List<Component> uniqueComponents = new ArrayList<>(componentMap.values());
            assembly.get(0).setComponentList(uniqueComponents);

            return assembly.get(0);
        }
    }

    @Override
    public List<Assembly> getChildAssembly(String site, String pcuBO) throws Exception {
        List<Assembly> childAssemblyList = assemblyRepository.findByActiveAndSiteAndParentPcuBO(1, site, pcuBO);

        return childAssemblyList;
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

    private List<MainCustomDataObject> isDataFormatPresent(IsExist isExist) {
        try{
            CustomDataFormatObject isDataFormatPresent = webClientBuilder.build()
                    .post()
                    .uri(retrieveIsDataFormatPresentUrl)
                    .bodyValue(isExist)
                    .retrieve()
                    .bodyToMono(CustomDataFormatObject.class)
                    .block();
            if (isDataFormatPresent == null || isDataFormatPresent.getMainCustomDataObjectList()==null||isDataFormatPresent.getMainCustomDataObjectList().isEmpty()) {
                throw new AssemblyException(1700, isExist.getDatastring());
            }

            return isDataFormatPresent.getMainCustomDataObjectList();
        }
        catch(CustomDataFormatException customDataFormatException){
            throw customDataFormatException;
        }catch (Exception e) {
            throw e;
        }
    }

    public Inventory retrieveInventory(IsExist isExist) {
        Inventory inventory = webClientBuilder.build()
                .post()
                .uri(retrieveInventoryUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Inventory.class)
                .block();

        return inventory;
    }

    @Override
    public boolean addNonBomComponents(String site,String parentPcuBO, String pcuBO, Component component) throws Exception {
        site=site;
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();

        if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO()==null) {
            throw new AssemblyException(2701, pcuBO);
        }
        pcuHeaderRequest.setOperationBO("OperationBO:"+pcuHeaderRequest.getSite()+","+component.getOperation()+",");
//        PcuInWork pcuInWork=webClientBuilder.build()
//                .post()
//                .uri(retrieveInWorkUrl)
//                .bodyValue(pcuHeaderRequest)
//                .retrieve()
//                .bodyToMono(PcuInWork.class)
//                .block();
//
//        if (pcuInWork == null || pcuInWork.getPcuBO()==null) {
//            throw new AssemblyException(1706, pcuBO);
//        }
        IsExist isExist = IsExist.builder().site(site).item(component.getComponent()).build();
        Item item = retrieveItem(isExist);
        if(item==null || item.getItem()==null){
            throw new AssemblyException(4306,isExist.getItem());
        }

        String[] bom = pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
        String pcuBom = "PcuBomBO:" + site + "," + bom[1] + "," + bom[2] + "," + pcuBO;
        BomHeaderRequest bomHeaderRequestmain = BomHeaderRequest.builder()
                .site(site)
                .pcuBomBO(pcuBom)
                .build();

        Bom bomResponse = retrieveBomHeader(bomHeaderRequestmain);

        if (bomResponse == null || bomResponse.getBomComponentList()==null|| bomResponse.getBomComponentList().isEmpty()) {
            throw new AssemblyException(200, bom[1], bom[2]);
        }

        double requiredQty=0.0;
        String dataType = null;
        for (BomComponent bomComponent : bomResponse.getBomComponentList()) {
            if (bomComponent.getComponent().equals(component.getComponent())&&bomComponent.getAssySequence().equals(component.getSequence())) {
                requiredQty=Double.parseDouble(bomComponent.getAssyQty());
                if (bomComponent.getAssemblyDataTypeBo() == null || bomComponent.getAssemblyDataTypeBo().isEmpty()) {
                    dataType = item.getAssemblyDataType();
                } else {
                    dataType = bomComponent.getAssemblyDataTypeBo();
                }
                if(dataType!=null&&!dataType.isEmpty()) {
                    checkRequiredFields(site, dataType, component);
                }
                component.setSequence(bomComponent.getAssySequence());
                break;
            }
        }

        for (AssemblyData assemblyData : component.getAssemblyDataList()) {
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
                }
            }if ((assemblyData.getDataField().equalsIgnoreCase("PlannedMaterial"))||(assemblyData.getDataField().equalsIgnoreCase("planned material"))||(assemblyData.getDataField().equalsIgnoreCase("material"))){
                if(assemblyData.getValue()!=null&&!assemblyData.getValue().isEmpty()) {
                    IsExist isExistDataField=IsExist.builder().site(site).item(assemblyData.getValue()).build();
                    Item itemDataField=retrieveItem(isExistDataField);
                    if(itemDataField==null||itemDataField.getItem()==null||itemDataField.getItem().isEmpty()){
                        throw new AssemblyException(3209,assemblyData.getValue());
                    }
                }

            }
        }

        Pcu pcu = Pcu.builder()
                .pcuBo(pcuBO)
                .build();

        List<Pcu> pcus = new ArrayList<>();
        pcus.add(pcu);

        String pcuBomBO = "PcuBomBO:" + site + "," + bom[1] + "," + bom[2] + "," + pcuBO;

        BomHeaderRequest bomHeaderRequest = BomHeaderRequest.builder()
                .site(site)
                .pcuBomBO(pcuBomBO)
                .build();

        Bom bomList = retrieveBomHeader(bomHeaderRequest);

        for (BomComponent bomComponent : bomList.getBomComponentList()) {
            if (component.getComponent().equalsIgnoreCase(bomComponent.getComponent())&&component.getSequence().equals(bomComponent.getAssySequence())) {
                String qty = String.format("%.6f", (Double.parseDouble(bomComponent.getAssembledQty()) + Double.parseDouble(component.getQty())));
                if(Double.parseDouble(qty)>(requiredQty)){
                    throw new AssemblyException(1705);
                }
                bomComponent.setAssembledQty(qty);
            }
        }

        bomHeaderRequest.setBom(bomList);
        bomHeaderRequest.setPcuBos(pcus);
        updateBomHeader(bomHeaderRequest);
        List<String> updatedTagsList = generateCombinations(site,component.getAssemblyDataList());
        String tags = String.join(",", updatedTagsList);
        Assembly assembly = new Assembly();
        assembly.setPcuBO(pcuBO);
        assembly.setSite(site);
        assembly.setItemBO(pcuHeaderResponse.getItemBO());
        assembly.setPcuBomBO(pcuHeaderResponse.getBomList().get(0).getPcuBomBO());
        assembly.setPcuRouterBO(pcuHeaderResponse.getRouterList().get(0).getPcuRouterBO());
        assembly.setShopOrderBO(pcuHeaderResponse.getShopOrderBO());
        assembly.setParentOrderBO(pcuHeaderResponse.getParentOrderBO());
        assembly.setParentPcuBO(pcuHeaderResponse.getParentPcuBO());
        if(parentPcuBO != null && !parentPcuBO.isEmpty() && !parentPcuBO.equalsIgnoreCase(pcuBO))
        {
            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, parentPcuBO);
            if(existingAssembly!= null && existingAssembly.getPcuBO() != null)
            {
                List<Component> updatedComponentList = insertNonBomComponent(pcuBO,existingAssembly.getComponentList(),component);
                existingAssembly.setComponentList(updatedComponentList);
                assemblyRepository.save(existingAssembly);
                return true;
            }
        }
        if (assemblyRepository.existsByActiveAndSiteAndPcuBO(1, site, pcuBO)) {
            Assembly existingAssembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);

            if (existingAssembly.getTags() != null && !existingAssembly.getTags().isEmpty()) {
                assembly.setTags("," + tags);
            } else {
                assembly.setTags(tags);
            }
            if(item.getProcurementType()!=null&& !item.getProcurementType().isEmpty() && item.getProcurementType().equalsIgnoreCase("Manufactured")){
                List<Assembly> childAssemblyList=new ArrayList<>();
                List<Assembly> childAssembly  = assemblyRepository.findByActiveAndSiteAndParentPcuBOAndItemBO(1,site,pcuBO, "ItemBO:"+site+","+item.getItem()+","+item.getRevision());
                for(Assembly assembly1 : childAssembly) {
                    assembly1.setImmediateParent(pcuBO);
                    assembly1.setMainParent(pcuBO);
                    childAssemblyList.add(assembly1);
                }
                component.setChildAssembly(childAssemblyList);

            }
            component.setCreatedDateTime(LocalDateTime.now());
            component.setNonBom(true);
            existingAssembly.getComponentList().add(component);
            assembly.setComponentList(existingAssembly.getComponentList());
            assembly.setActive(1);
            assemblyRepository.save(assembly);
            return true;
        }else {
            if(item.getProcurementType()!=null&& !item.getProcurementType().isEmpty() && item.getProcurementType().equalsIgnoreCase("Manufactured")){
                List<Assembly> childAssemblyList=new ArrayList<>();
                List<Assembly> childAssembly  = assemblyRepository.findByActiveAndSiteAndParentPcuBOAndItemBO(1,site,pcuBO, "ItemBO:"+site+","+item.getItem()+","+item.getRevision());
                for(Assembly assembly1 : childAssembly) {
                    assembly1.setImmediateParent(pcuBO);
                    assembly1.setMainParent(pcuBO);
                    childAssemblyList.add(assembly1);
                }
                component.setChildAssembly(childAssemblyList);
            }
            assembly.setTags(tags);
            component.setCreatedDateTime(LocalDateTime.now());
            component.setNonBom(true);
            List<Component> componentList = new ArrayList<>();
            componentList.add(component);
            assembly.setComponentList(componentList);
            assembly.setActive(1);
            assemblyRepository.save(assembly);
            return true;
        }
    }

    @Override
    public Assembly retrieveSerializedComponentListForReturnAndReplace(String site, String pcuBO)
    {
        Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
        if(assembly != null && assembly.getPcuBO() != null)
        {
            if(assembly.getComponentList() != null)
            {
                List<Component> components = serializedComponentList(site,assembly.getComponentList());
                assembly.setComponentList(components);
            }
        }
        return assembly;
    }

    public List<Component> serializedComponentList(String site,List<Component> componentList)
    {
        List<Component> components = new ArrayList<>();
        if(componentList != null)
        {
            for(Component component : componentList)
            {
                if(component.isRemoved())
                {
                    continue;
                }

                IsExist isExist = IsExist.builder().site(site).item(component.getComponent()).build();
                Item item = retrieveItem(isExist);
                if(item!= null && item.getItem() != null && item.getAssemblyDataType().equalsIgnoreCase("None"))
                {
                    continue;
                }

                if(component.isNonBom())
                {
                    component.setComponent("NonBomComponent:"+component.getComponent());
                }else{
                    component.setComponent("Component:"+component.getComponent());
                }
                components.add(component);
                if(component.getChildAssembly()!=null && component.getChildAssembly().get(0)!=null)
                {
                    for(Assembly childAssembly : component.getChildAssembly()) {
                        if (childAssembly.getComponentList() != null) {
                            List<Component> subComponentList = serializedComponentList(site, childAssembly.getComponentList());
                            childAssembly.setComponentList(subComponentList);
                        }
                    }
                }
            }
        }
        return components;
    }

    @Override
    public Assembly retrieveNonSerializedComponentListForReturnAndReplace(String site, String pcuBO)
    {
        Assembly assembly = assemblyRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
        if(assembly != null && assembly.getPcuBO() != null)
        {
            if(assembly.getComponentList() != null)
            {
                List<Component> components = nonSerilizedComponentList(site,assembly.getComponentList());
                assembly.setComponentList(components);
            }
        }
        return assembly;
    }

    public List<Component> nonSerilizedComponentList(String site,List<Component> componentList)
    {
        List<Component> components = new ArrayList<>();
        if(componentList != null)
        {
            for(Component component : componentList)
            {
                if(component.isRemoved())
                {
                    continue;
                }

                IsExist isExist = IsExist.builder().site(site).item(component.getComponent()).build();
                Item item = retrieveItem(isExist);
                if(item!= null && item.getItem() != null && !item.getAssemblyDataType().equalsIgnoreCase("None"))
                {
                    continue;
                }

                if(component.isNonBom())
                {
                    component.setComponent("NonBomComponent:"+component.getComponent());
                }else{
                    component.setComponent("Component:"+component.getComponent());
                }
                components.add(component);
                if(component.getChildAssembly()!=null && component.getChildAssembly().get(0)!=null)
                {
//                    if(component.getChildAssembly().get(0).getComponentList() != null) {
//                        serializedComponentList(site,component.getChildAssembly().get(0).getComponentList());
//                    }
                    for(Assembly childAssembly : component.getChildAssembly()) {
                        if (childAssembly.getComponentList() != null) {
                            List<Component> subComponentList = nonSerilizedComponentList(site, childAssembly.getComponentList());
                            childAssembly.setComponentList(subComponentList);
                        }
                    }
                }
            }
        }
        return components;
    }

    @Override
    public boolean isAllQuantityAssembled(PcuCompleteReq completeReq){
        String site = completeReq.getSite();
        String pcuBO= BOConverter.retrievePcuBO(completeReq.getSite(), completeReq.getPcu());
        double requiredQty=0.0;
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();

        if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO()==null) {
            throw new AssemblyException(2701, pcuBO);
        }
        // From header get BomList and retrieve Bom
        if(pcuHeaderResponse.getBomList()!=null && !pcuHeaderResponse.getBomList().isEmpty() && pcuHeaderResponse.getBomList().get(0).getPcuBomBO()!=null && !pcuHeaderResponse.getBomList().get(0).getPcuBomBO().isEmpty()) {
            String[] bom = pcuHeaderResponse.getBomList().get(0).getPcuBomBO().split(",");
            String pcuBom = "PcuBomBO:" + site + "," + bom[1] + "," + bom[2] + "," + pcuBO;
            BomHeaderRequest bomHeaderRequestmain = BomHeaderRequest.builder()
                    .site(site)
                    .pcuBomBO(pcuBom)
                    .build();

            Bom bomHeaderResponse = retrieveBomHeader(bomHeaderRequestmain);
            if (bomHeaderResponse == null || bomHeaderResponse.getBomComponentList() == null || bomHeaderResponse.getBomComponentList().isEmpty()) {
                throw new AssemblyException(200, bom[1], bom[2]);
            }
//            String[] operationBo = pcuCompleteReqWithBO.getOperationBO().split(",");
            String operation = BOConverter.getOperation(completeReq.getOperation());

            for (BomComponent bomComponent : bomHeaderResponse.getBomComponentList()) {
                if (bomComponent.getAssyOperation() != null && !bomComponent.getAssyOperation().isEmpty() && bomComponent.getAssyOperation().equalsIgnoreCase(operation)) {
                    requiredQty = Double.parseDouble(bomComponent.getAssyQty());
                    if (!(requiredQty == Double.parseDouble(bomComponent.getAssembledQty()))) {
                        return false;
                    }
                }
            }
        }else{
            return true;
        }
        return true;
    }
    @Override
    public Assembly retrieveAssemblyAggr(String site, String pcuBO) {
//        GraphLookupOperation graphLookupOperation = GraphLookupOperation.builder()
//                .from("Assembly") // Specify the collection to perform the lookup
//                .startWith("$pcuBO") // Start with the field "pcuBO"
//                .connectFrom("childAssembly.pcuBO") // Specify the field to connect from
//                .connectTo("_id") // Specify the field to connect to
//                .maxDepth(3) // Specify the maximum recursion depth
//                .as("childAssemblies"); // Alias for the results
//
//        Aggregation aggregation = Aggregation.newAggregation(
//                Aggregation.match(Criteria.where("_id").is( pcuBO)
//                        .and("site").is(site)),
//                graphLookupOperation
//        );
//
//        AggregationResults<Assembly> results = mongoTemplate.aggregate(aggregation, "Assembly", Assembly.class);
//        List<Assembly> assemblies = results.getMappedResults();
//
//        // Check if any assemblies were retrieved
//        if (!assemblies.isEmpty()) {
//            // Return the first assembly
//            return assemblies.get(0);
//        } else {
//            // Handle case where no assembly was found
//            return null;
//        }
        return assemblyRecord(site,pcuBO);

    }
    /* Added for PCU add or Update Component.  - Senthil POC. addOrUpdateComponent
     * */
    @Override
    public boolean addOrUpdateComponent(String site, String parentPcuBO, String userId, String pcuBO, Component component,String parentItem) throws Exception {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder()
                .site(site)
                .pcuBO(pcuBO)
                .build();

        PcuHeader pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(PcuHeader.class)
                .block();

        /*if (pcuHeaderResponse == null || pcuHeaderResponse.getPcuBO()==null) {
            throw new AssemblyException(2701, pcuBO);
        }
*/
        Assembly parentAssembly = assemblyRepository.findById(pcuBO)
                .orElse(null);

        if (parentAssembly == null) {
            // Step 2: Create new assembly and add component
            parentAssembly = new Assembly();
            parentAssembly.setPcuBO(pcuBO);// Setup new assembly with necessary details
         //   parentAssembly.setItemBO(pcuHeaderResponse.getItemBO());
            parentAssembly.setItemBO(parentItem); // Need to set the other
            parentAssembly.setComponentList(new ArrayList<>()); // Initialize component list
            parentAssembly.getComponentList().add(component); // Add the new component
            assemblyRepository.save(parentAssembly);
        } else {
            // Step 3 & 4: Check if component exists and update or add accordingly
            Optional<Component> existingComponentOpt = parentAssembly.getComponentList().stream()
                    .filter(c -> c.getSequence().equals(component.getSequence()) && // Need to add the step id also
                            c.getComponent().equals(component.getComponent()) &&
                            c.getAssembledBy().equals(userId) &&
                            c.getOperation().equals(component.getOperation()) &&
                            c.getStepId().equals(component.getStepId())&&
                            c.getResourceBO().equals(component.getResourceBO()))
                    .findFirst();

            if (existingComponentOpt.isPresent()) {
                // Step 5: Update existing component details
                Component existingComponent = existingComponentOpt.get();
                updateComponentDetails(existingComponent, component, userId);
            } else {
                // Insert new component into the assembly
                parentAssembly.getComponentList().add(component);
            }

            assemblyRepository.save(parentAssembly);
        }

        // Step 6: Handle ancestry updates for sub-assemblies
        if (component.getAssembledPcus() != null && !component.getAssembledPcus().isEmpty()) {
            for (AssembledPcu assembledPcu : component.getAssembledPcus()) {
                updateAncestryAndDescendants(assembledPcu.getPcuBo(), parentAssembly.getPcuBO(), parentItem);
            }
        }

        return true;
    }
    /* Added for PCU add or Update Component.  - Senthil POC. updateComponentDetails
     * */
    private void updateComponentDetails(Component existingComponent, Component newDetails, String userId) {
        // Example: Updating assembled quantity. You might need logic to handle different quantities correctly.
        existingComponent.setAssembledQty(newDetails.getAssembledQty());

        // Update assembler information
        existingComponent.setAssembledBy(userId);

        // Update assemblyDataList by adding new entries from newDetails
        if (newDetails.getAssemblyDataList() != null) {
            if (existingComponent.getAssemblyDataList() == null) {
                existingComponent.setAssemblyDataList(new ArrayList<>());
            }
            existingComponent.getAssemblyDataList().addAll(newDetails.getAssemblyDataList());
        }

        // Update assembledPcus by adding new entries from newDetails
        if (newDetails.getAssembledPcus() != null) {
            if (existingComponent.getAssembledPcus() == null) {
                existingComponent.setAssembledPcus(new ArrayList<>());
            }
            existingComponent.getAssembledPcus().addAll(newDetails.getAssembledPcus());
        }

        // Note: Assuming the parent Assembly document is saved after this update,
        // which persists these changes to the database.
    }
    /* Added for PCU add or Update Component.  - Senthil POC. updateAncestryAndDescendants
     * */
    private void updateAncestryAndDescendants(String assembledPcuBO, String newParentPcuBO, String itemName) {
        // Define newAncestry outside the if block to ensure it is accessible throughout the method
        Ancestry newAncestry = new Ancestry();
        newAncestry.setItem(itemName); // Set the item name
        newAncestry.setPcuBO(newParentPcuBO); // Set the new parent PCU BO

        // Fetch and update the directly assembled PCU
        Assembly assembledPcuAssembly = assemblyRepository.findById(assembledPcuBO).orElse(null);
        if (assembledPcuAssembly != null) {

            if (assembledPcuAssembly.getAncestry() == null) {
                assembledPcuAssembly.setAncestry(new ArrayList<>());
            }

            assembledPcuAssembly.getAncestry().add(newAncestry); // Add new parent info to ancestry
            assemblyRepository.save(assembledPcuAssembly);
        }

        // Efficiently find only descendants that have the assembledPcuBO in their ancestry
        List<Assembly> descendantAssemblies = assemblyRepository.findByAncestryPcuBO(assembledPcuBO);
        for (Assembly descendantAssembly : descendantAssemblies) {
            // Ensure we're not adding the ancestry to the same assembly we've just updated
            if (!descendantAssembly.getPcuBO().equals(assembledPcuBO)) {
                // Initialize ancestry if null
                if (descendantAssembly.getAncestry() == null) {
                    descendantAssembly.setAncestry(new ArrayList<>());
                }
                descendantAssembly.getAncestry().add(newAncestry); // Add the new ancestry info to descendants
                assemblyRepository.save(descendantAssembly);
            }
        }
    }
    /* Added for PCU add or Update Component.  - Senthil POC. buildAssemblyHierarchy
     * */
    public Assembly buildAssemblyHierarchy(String rootPcuBO) {
        Optional<Assembly> rootAssemblyOpt = assemblyRepository.findByPcuBO(rootPcuBO);
        if (!rootAssemblyOpt.isPresent()) {
            throw new RuntimeException("Root assembly not found.");
        }

        Assembly rootAssembly = rootAssemblyOpt.get();
        List<Assembly> allRelatedAssemblies = assemblyRepository.findByAncestryPcuBO(rootPcuBO);

        Map<String, Assembly> allAssembliesMap = new HashMap<>();
        allAssembliesMap.put(rootAssembly.getPcuBO(), rootAssembly);
        allRelatedAssemblies.forEach(assembly -> allAssembliesMap.put(assembly.getPcuBO(), assembly));

        populateChildAssemblies(rootAssembly, allAssembliesMap);

        return rootAssembly; // This now includes nested child assemblies
    }
    /* Added for PCU add or Update Component.  - Senthil POC. populateChildAssemblies
     * */
    private void populateChildAssemblies(Assembly parentAssembly, Map<String, Assembly> allAssembliesMap) {
        if (parentAssembly.getComponentList() == null) {
            return;
        }

        for (Component component : parentAssembly.getComponentList()) {
            if (component.getAssembledPcus() != null) {
                for (AssembledPcu assembledPcu : component.getAssembledPcus()) {
                    Assembly childAssembly = allAssembliesMap.get(assembledPcu.getPcuBo());
                    if (childAssembly != null) {
                        if (component.getChildAssembly() == null) {
                            component.setChildAssembly(new ArrayList<>());
                        }
                        component.getChildAssembly().add(childAssembly);
                        populateChildAssemblies(childAssembly, allAssembliesMap); // Recursive call
                    }
                }
            }
        }
    }

    private Assembly assemblyRecord(String site, String pcuBO) {
        Assembly assembly= assemblyRepository.findByActiveAndSiteAndPcuBO(1,site,pcuBO);
        if(assembly!=null&& assembly.getComponentList()!=null &&!assembly.getComponentList().isEmpty()){
            for(Component component: assembly.getComponentList()){
                if(component.getChildAssembly()!=null&& !component.getChildAssembly().isEmpty() ){
                    List<Assembly> neewChildAssemblyList= new ArrayList<>();
                    for(Assembly childAssembly: component.getChildAssembly()){
                        Assembly response= assemblyRecord(site, childAssembly.getPcuBO());
                        if(response!=null&& response.getPcuBO()!=null && !response.getPcuBO().isEmpty()){
                            neewChildAssemblyList.add(response);
                        }
                    }
                    component.setChildAssembly(neewChildAssemblyList);
                }
            }
        }
        return assembly;

    }

//    private MongoCollection<org.bson.Document> assemblyCollection;
//    private Set<String> visitedIds;
//
//    public void RecursiveQuery(MongoDatabase database) {
//        this.assemblyCollection = database.getCollection("Assembly");
//        this.visitedIds = new HashSet<>();
//    }
//
//    public Assembly processDocument(String id) {
//        if (!visitedIds.contains(id)) {
//            visitedIds.add(id);
//            Document assemblyDocument = assemblyCollection.find(new Document("_id", id)).first();
//            if (assemblyDocument != null) {
//                Assembly assembly = convertDocumentToAssembly(assemblyDocument);
//                List<Document> childAssemblyList = assemblyDocument.getList("childAssembly", Document.class);
//                if (childAssemblyList != null) {
//                    List<Assembly> childAssemblies = new ArrayList<>();
//                    for (Document child : childAssemblyList) {
//                        String childPcuBO = child.getString("pcuBO");
//                        if (childPcuBO != null) {
//                            Assembly childAssembly = processDocument(childPcuBO);
//                            if (childAssembly != null) {
//                                childAssemblies.add(childAssembly);
//                            }
//                        }
//                    }
//                    assembly.setChildAssembly(childAssemblies);
//                }
//                return assembly;
//            }
//        }
//        return null; // Or handle the case where no assembly was found
//    }
//    private Assembly convertDocumentToAssembly(Document document) {
//        MappingMongoConverter converter = new MappingMongoConverter((DbRefResolver) new MongoMappingContext(), null);
//        return converter.read(Assembly.class, (Bson) document);
//    }



}
