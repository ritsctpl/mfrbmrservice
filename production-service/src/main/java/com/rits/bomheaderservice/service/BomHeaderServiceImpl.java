package com.rits.bomheaderservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.assemblyservice.dto.AuditLogRequest;
import com.rits.bomheaderservice.dto.BomComponentList;
import com.rits.bomheaderservice.dto.BomHeaderRequest;
import com.rits.bomheaderservice.dto.Extension;
import com.rits.bomheaderservice.dto.Pcu;
import com.rits.bomheaderservice.exception.BomHeaderException;
import com.rits.bomheaderservice.model.*;
import com.rits.bomheaderservice.repository.BomHeaderRepository;
import com.rits.shoporderrelease.dto.Item;
import com.rits.shoporderrelease.dto.ItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BomHeaderServiceImpl implements BomHeaderService{

    private final BomHeaderRepository bomHeaderRepository;

    private final WebClient.Builder webClientBuilder;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;
    @Value("${item-service.url}/retrieve")
    private String itemUrl;


//
//    @Value("${validation.type.uri}/bomheader")
//    private String validationUrl;

    @Override
    public BomHeaderMessageModel create(BomHeaderRequest bomHeaderRequest) throws Exception
    {
        List<Bom> bomList = new ArrayList<Bom>();
        List<Integer> pcuValues = new ArrayList<>();
        List<Pcu> pcuKeys = new ArrayList<>();
        if(!bomHeaderRequest.getPcuNumberList().isEmpty()){
            for(Map.Entry<String, Integer> pcus : bomHeaderRequest.getPcuNumberList().entrySet()) {
                Pcu pcuKey = Pcu.builder().pcuBo(pcus.getKey()).build();
                pcuKeys.add(pcuKey);
                pcuValues.add(pcus.getValue());
            }
            bomHeaderRequest.setPcuBos(pcuKeys);
        }

        if(!bomHeaderRequest.getBom().getBomComponentList().isEmpty()){
            for(int i=0;i<bomHeaderRequest.getBom().getBomComponentList().size();i++)
            {
                if(!bomHeaderRequest.getPcuNumberList().isEmpty() && i<=pcuValues.size()-1){
                    bomHeaderRequest.setQtyInQueue(pcuValues.get(i));
                }
                BomComponent bomValue = bomHeaderRequest.getBom().getBomComponentList().get(i);
                ItemRequest itemRequest = ItemRequest.builder().site(bomHeaderRequest.getBom().getSite()).item(bomValue.getComponent()).revision(bomValue.getComponentVersion()).build();
                Item existingItem = webClientBuilder
                        .build()
                        .post()
                        .uri(itemUrl)
                        .body(BodyInserters.fromValue(itemRequest))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Item>() {
                        })
                        .block();
                Double lot = null;
                if(existingItem!=null && existingItem.getItem()!=null && !existingItem.getItem().isEmpty())
                {
//                    lot = Double.parseDouble(existingItem.getLotSize());  //commented it for the lot size data type change
                    lot = existingItem.getLotSize();
                }
                Double assignedQty = Double.parseDouble(bomValue.getAssyQty());
//                Integer  qtyInQueue = bomHeaderRequest.getQtyInQueue();   //commented it for the lot size data type change
                Double  qtyInQueue = bomHeaderRequest.getQtyInQueue();
                if(assignedQty!=null && assignedQty>0  && qtyInQueue!=null && qtyInQueue>0 && lot>0 && lot!=null) {
                    Double newAssyQty =(assignedQty*qtyInQueue.doubleValue())/lot;
                    DecimalFormat df = new DecimalFormat("#.######");
                    df.setRoundingMode(RoundingMode.HALF_UP);
                    Double roundedValue = Double.parseDouble(df.format(newAssyQty));
                    bomValue.setAssyQty( String.format("%.6f", roundedValue));
                }
                bomValue.setAssembledQty("0");
            }
        }
        bomList.add(bomHeaderRequest.getBom());
        List<BomHeader> response = new ArrayList<BomHeader>();

        for(int i=0;i<bomHeaderRequest.getPcuBos().size();i++)
        {
            BomHeader createBomHeader = BomHeader.builder()
                    .handle("BomBO:"+bomHeaderRequest.getBom().getSite()+","+bomHeaderRequest.getPcuBos().get(i).getPcuBo())
                    .site(bomHeaderRequest.getBom().getSite())
                    .pcuBomBO("PcuBomBO:"+bomHeaderRequest.getBom().getSite()+","+bomHeaderRequest.getBom().getBom()+","+bomHeaderRequest.getBom().getRevision()+","+bomHeaderRequest.getPcuBos().get(i).getPcuBo())
                    .bomList(bomList)
                    .pcuBO(bomHeaderRequest.getPcuBos().get(i).getPcuBo())
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();
                     response.add(createBomHeader);
//                     bomHeaderRepository.save(createBomHeader);
            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(bomHeaderRequest.getSite())
                    .change_stamp("Create")
                    .action_code("BOMHEADER-CREATE")
                    .action_detail("BomHeader Created "+"PcuBomBO:"+bomHeaderRequest.getBom().getSite()+","+bomHeaderRequest.getBom().getBom()+","+bomHeaderRequest.getBom().getRevision()+",PcuBO:"+bomHeaderRequest.getBom().getSite()+","+bomHeaderRequest.getPcuBos().get(i).getPcuBo())
                    .action_detail_handle("ActionDetailBO:"+bomHeaderRequest.getSite()+","+"BOMHEADER-CREATE"+","+bomHeaderRequest.getUserId()+":"+"com.rits.bomheaderservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(bomHeaderRequest.getUserId())
                    .txnId("BOMHEADER-CREATE"+String.valueOf(LocalDateTime.now())+bomHeaderRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();
        }
        bomHeaderRepository.saveAll(response);
        return BomHeaderMessageModel.builder().message_details(new MessageDetails(" Created SuccessFully","S")).build();
    }

    @Override
    public BomHeaderMessageModel update(BomHeaderRequest bomHeaderRequest) throws Exception {
        if(bomHeaderRepository.existsBySiteAndActiveAndPcuBomBO(bomHeaderRequest.getSite(),1,bomHeaderRequest.getPcuBomBO())) {


            BomHeader existingBomHeader=bomHeaderRepository.findBySiteAndActiveAndPcuBomBO(bomHeaderRequest.getSite(), 1, bomHeaderRequest.getPcuBomBO());
            List<Bom> bomList = new ArrayList<Bom>();
            bomList.add(bomHeaderRequest.getBom());

            for (int i = 0; i < bomHeaderRequest.getPcuBos().size(); i++) {
                existingBomHeader.setBomList(bomList);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(bomHeaderRequest.getSite())
                        .change_stamp("Update")
                        .action_code("BOMHEADER-UPDATE")
                        .action_detail("BomHeader Updated "+"PcuBomBO:"+bomHeaderRequest.getBom().getSite()+","+bomHeaderRequest.getBom().getBom()+","+bomHeaderRequest.getBom().getRevision()+",PcuBO:"+bomHeaderRequest.getBom().getSite()+","+bomHeaderRequest.getPcuBos().get(i).getPcuBo())
                        .action_detail_handle("ActionDetailBO:"+bomHeaderRequest.getSite()+","+"BOMHEADER-UPDATE"+","+bomHeaderRequest.getUserId()+":"+"com.rits.bomheaderservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(bomHeaderRequest.getUserId())
                        .txnId("BOMHEADER-UPDATE"+String.valueOf(LocalDateTime.now())+bomHeaderRequest.getUserId())
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .build();

                webClientBuilder.build()
                        .post()
                        .uri(auditlogUrl)
                        .bodyValue(activityLog)
                        .retrieve()
                        .bodyToMono(AuditLogRequest.class)
                        .block();
                bomHeaderRepository.save(existingBomHeader);
            }
            return BomHeaderMessageModel.builder().message_details(new MessageDetails(" updated SuccessFully", "S")).build();
        }
        else {
            throw new BomHeaderException(2801,bomHeaderRequest.getPcuBomBO());
        }
    }

    @Override
        public Bom retrieve(BomHeaderRequest bomHeaderRequest) throws Exception
        {
            BomHeader existingRecord = bomHeaderRepository.findBySiteAndActiveAndPcuBomBO(bomHeaderRequest.getSite(),1,bomHeaderRequest.getPcuBomBO());
            return existingRecord.getBomList().get(0);
        }

        @Override
        public List<BomHeader> updateStatusOfBomToReleased(BomHeaderRequest bomHeaderRequest) throws Exception
        {
            List<BomHeader> response = new ArrayList<BomHeader>();
            for(int i=0;i<bomHeaderRequest.getPcuBos().size();i++)
            {
                String newPcuBO = "PcuBomBO:"+bomHeaderRequest.getBom().getSite()+","+bomHeaderRequest.getBom().getBom()+","+bomHeaderRequest.getPcuBos().get(i).getPcuBo();
                BomHeader existingRecord = bomHeaderRepository.findBySiteAndActiveAndPcuBomBO(bomHeaderRequest.getBom().getSite(),1,newPcuBO);
                existingRecord.getBomList().get(0).setStatus("Released");
                bomHeaderRepository.save(existingRecord);
                response.add(existingRecord);
            }
            return response;
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
            throw new BomHeaderException(800);
        }
        return extensionResponse;
    }
    @Override
    public BomHeaderMessageModel validation(JsonNode payload)
   {
//        BomHeaderMessageModel validationResponse = webClientBuilder.build()
//                .post()
//                .uri(validationUrl)
//                .bodyValue(payload)
//                .retrieve()
//                .bodyToMono(BomHeaderMessageModel.class)
//                .block();
        return null;
    }
    public BomComponentList getComponentListByOperation(String site,String bom,String revision,String pcuBO , String operation) throws Exception {
        String pcuBomBO="PcuBomBO:"+site+","+bom+","+revision+","+pcuBO;
        if (bomHeaderRepository.existsBySiteAndActiveAndPcuBomBO(site,1, pcuBomBO)) {
            BomHeader bomHeader = bomHeaderRepository.findBySiteAndActiveAndPcuBomBO(site,1, pcuBomBO);
            if(!bomHeader.getBomList().isEmpty()) {
                Bom bomType = bomHeader.getBomList().get(0);
                List<BomComponent> bomComponentLists = new ArrayList<>();
                if(!bomType.getBomComponentList().isEmpty()) {
                    for (BomComponent bomComponent : bomType.getBomComponentList()) {
                        if (operation.equalsIgnoreCase(bomComponent.getAssyOperation())) {
                            bomComponentLists.add(bomComponent);
                        }
                    }
                }
                return BomComponentList.builder().bomComponentList(bomComponentLists).build();
            }
        } else {
            throw new BomHeaderException(2801,pcuBomBO);
        }
        return null;
    }
    @Override
    public Boolean deleteBomHeaderByPcu(String site, String pcuBO)
    {
        Boolean isDeleted = false;
        BomHeader retrievedBomHeader = bomHeaderRepository.findByActiveAndSiteAndPcuBO(1,site,pcuBO);
        if(retrievedBomHeader != null)
        {
            retrievedBomHeader.setActive(0);
            bomHeaderRepository.save(retrievedBomHeader);
            isDeleted = true;
        }
        return isDeleted;
    }

    @Override
    public Boolean unDeleteBomHeaderByPcu(String site, String pcuBO)
    {
        Boolean isDeleted = false;
        BomHeader retrievedBomHeader = bomHeaderRepository.findByActiveAndSiteAndPcuBO(0,site,pcuBO);
        if(retrievedBomHeader != null)
        {
            retrievedBomHeader.setActive(1);
            bomHeaderRepository.save(retrievedBomHeader);
            isDeleted = true;
        }
        return isDeleted;
    }

}
