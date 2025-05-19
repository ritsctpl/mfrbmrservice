package com.rits.dhrservice.service;

import com.rits.dhrservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DhrServiceImpl implements DhrService{

    private final WebClient.Builder webClientBuilder;

    @Value("${dccollect-service.url}/retrieveByPcuForDataCollection")
    private String retrieveDataCollectionUrl;

    @Value("${toollog-service.url}/retrieveBySiteAndPcu")
    private String retrieveToolLogUrl;

    @Value("${nonconformance-service.url}/retrieveByPcuForLogNc")
    private String retrieveLoggedNcUrl;

    @Value("${toolnumber-service.url}/retrieve")
    private String retrieveToolNumberUrl;

    @Value("${item-service.url}/retrieve")
    private String retrieveItemUrl;

    @Value("${assembly-service.url}/getSerializedAssembly")
    private String retrieveAssemblyUrl;

    @Value("${assembly-service.url}/getSerializedAssemblyByPcuItem")
    private String retrieveAssemblyBYItemAndPcuUrl;

    @Value("${assembly-service.url}/retrieveAssembly")
    private String retrieveNonSerializedAssemblyBYItemAndPcuUrl;
    @Value("${bom-service.url}/retrieve")
    private String retrieveBomUrl;

    @Value("${productionlog-service.url}/retrieveByPcuForWorkInstruction")
    private String retrieveWorkInstructionUrl;




    @Override
    public List<ParametricMeasures> retrieveFromDataCollection(String site, String pcu) throws Exception
    {
        DcCollectRequest retrieveDcCollectRequest = DcCollectRequest.builder().site(site).pcu(pcu).build();
        List<ParametricMeasures> retrievedDataCollection = webClientBuilder.build()
                .post()
                .uri(retrieveDataCollectionUrl)
                .bodyValue(retrieveDcCollectRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ParametricMeasures>>() {
                })
                .block();
        return retrievedDataCollection;
    }

    @Override
    public List<ToolLog> retrieveFromToolLog(String site, String pcu)throws Exception
    {
        ToolLogRequest retrieveRequest = ToolLogRequest.builder().site(site).pcuBO(pcu).build();
        List<ToolLog> retrievedToolLog = webClientBuilder.build()
                .post()
                .uri(retrieveToolLogUrl)
                .bodyValue(retrieveRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ToolLog>>() {
                })
                .block();
        return retrievedToolLog;
    }

    @Override
    public List<NcData> retrieveFromLoggedNc(String site, String pcu)throws Exception
    {
        NcRequest retrieveRequest = NcRequest.builder().site(site).pcuBO(pcu).build();
        List<NcData> retrievedLoggedNc = webClientBuilder.build()
                .post()
                .uri(retrieveLoggedNcUrl)
                .bodyValue(retrieveRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<NcData>>() {
                })
                .block();
        return retrievedLoggedNc;
    }

    @Override
    public List<Component> retrieveFromAssembly(String site, String pcu) throws Exception
    {
        Assembly retrievedAssembly = retrieveAssemblyByPcu(site,"PcuBO:"+site+","+pcu);
        List<Component> componentList = new ArrayList<>();
        if(retrievedAssembly != null && retrievedAssembly.getComponentList()!=null && !retrievedAssembly.getComponentList().isEmpty())
        {
           componentList = getFinalComponentList(site,"PcuBO:"+site+","+pcu,retrievedAssembly.getComponentList());
           if(componentList.isEmpty() || componentList == null)
           {
               componentList = retrievedAssembly.getComponentList();
           }
        }
        return componentList;
    }

    public List<Component> getFinalComponentList(String site, String pcuBO, List<Component> componentList)
    {
        for(Component component : componentList)
        {
            List<BomComponent> bomComponentList =  getSubComponentList(site,component);
            if(bomComponentList != null)
            {
                for(BomComponent bomComponent : bomComponentList)
                {
                    Assembly retrieve = retrieveAssemblyByPcuAndItem(site,pcuBO,"ItemBO:"+site+","+bomComponent.getComponent()+","+bomComponent.getComponentVersion());
                    if(retrieve != null && retrieve.getComponentList()!=null && !retrieve.getComponentList().isEmpty())
                    {
                        component.setChildComponent(retrieve.getComponentList());
                        getFinalComponentList(site,retrieve.getParentPcuBO(),retrieve.getComponentList());
                    }
                }
            }
        }
        return componentList;
    }

    public List<BomComponent> getSubComponentList(String site,Component component)
    {
        List<BomComponent> bomComponentList = null;
           String item =  component.getComponent();
           if(item != null && !item.isEmpty()) {
               String bomName = getBomFromItem(site, item);
               bomComponentList = retrieveBomComponentList(site,bomName);
           }
           return bomComponentList;
    }

    public String getBomFromItem(String site , String item)
    {
        String bom = "";
        Item retrieveditem = retrieveItem(site,item);
        if(retrieveditem!=null && retrieveditem.getItem() !=null)
        {
            if(retrieveditem.getProcurementType().equalsIgnoreCase("manufactured"))
            {
                if(retrieveditem.getBom() != null && retrieveditem.getBomVersion() != null && !retrieveditem.getBom().isEmpty() && !retrieveditem.getBomVersion().isEmpty())
                {
                    bom = retrieveditem.getBom();
                }
            }
        }
        return bom;
    }

    public List<BomComponent> retrieveBomComponentList(String site, String bom)
    {
        List<BomComponent> bomComponentList = new ArrayList<>();
        BomRequest bomRequest = BomRequest.builder().site(site).bom(bom).build();
        Bom retrieveBom = webClientBuilder.build()
                .post()
                .uri(retrieveBomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
        if(retrieveBom != null & retrieveBom.getBom()!=null && retrieveBom.getBomComponentList() != null)
        {
            bomComponentList.addAll(retrieveBom.getBomComponentList());
        }
        return bomComponentList;
    }

    public Assembly retrieveAssemblyByPcu(String site, String pcuBO)
    {
        AssemblyRequest assemblyRequest = AssemblyRequest.builder().site(site).pcuBO(pcuBO).build();
        Assembly retrieveAssembly = webClientBuilder.build()
                .post()
                .uri(retrieveAssemblyUrl)
                .bodyValue(assemblyRequest)
                .retrieve()
                .bodyToMono(Assembly.class)
                .block();
        return retrieveAssembly;
    }

    public Assembly retrieveAssemblyByPcuAndItem(String site, String pcuBO, String itemBO)
    {
        AssemblyRequest assemblyRequest = AssemblyRequest.builder().site(site).itemBO(itemBO).pcuBO(pcuBO).build();
        Assembly retrieveAssembly = webClientBuilder.build()
                .post()
                .uri(retrieveAssemblyBYItemAndPcuUrl)
                .bodyValue(assemblyRequest)
                .retrieve()
                .bodyToMono(Assembly.class)
                .block();
        return retrieveAssembly;
    }

    public Item retrieveItem(String site, String item)
    {
        ItemRequest itemRequest = ItemRequest.builder().site(site).item(item).build();
        Item retrievedItem = webClientBuilder.build()
                .post()
                .uri(retrieveItemUrl)
                .bodyValue(itemRequest)
                .retrieve()
                .bodyToMono(Item.class)
                .block();
        return retrievedItem;
    }

    @Override
    public List<ProductionLogMongo> retrieveForWorkInstruction(String site, String pcu)
    {
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder().site(site).pcuBO("PcuBO:"+site+","+pcu).build();
        List<ProductionLogMongo> retrievedWorkInstruction = webClientBuilder.build()
                .post()
                .uri(retrieveWorkInstructionUrl)
                .bodyValue(productionLogRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductionLogMongo>>() {
                })
                .block();
        return retrievedWorkInstruction;
    }

    @Override
    public Assembly retrieveAssemblyByPcy(String site, String pcu)
    {
        AssemblyRequest assemblyRequest = AssemblyRequest.builder().site(site).pcuBO("PcuBO:"+site+","+pcu).build();
        Assembly retrieveAssembly = webClientBuilder.build()
                .post()
                .uri(retrieveNonSerializedAssemblyBYItemAndPcuUrl)
                .bodyValue(assemblyRequest)
                .retrieve()
                .bodyToMono(Assembly.class)
                .block();
        return retrieveAssembly;
    }

}
