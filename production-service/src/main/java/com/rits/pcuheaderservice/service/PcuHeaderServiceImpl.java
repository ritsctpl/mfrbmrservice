package com.rits.pcuheaderservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.Utility.BOConverter;
import com.rits.pcuheaderservice.dto.*;
import com.rits.pcuheaderservice.exception.PcuHeaderException;
import com.rits.pcuheaderservice.model.*;
import com.rits.pcuheaderservice.model.RouterList;
import com.rits.pcuheaderservice.repository.PcuHeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PcuHeaderServiceImpl implements PcuHeaderService{
    private final PcuHeaderRepository pcuHeaderRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    @Value("${bomheader-service.uri}/create")
    private String bomHeaderUrl;

    @Value("${pcurouterheader-service.uri}/create")
    private String pcuRouterHeaderUrl;

    @Value("${bom-service.url}/retrieve")
    private String bomUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
//     reason for comment: doChangeProduction() in ChangeProductionServiceImpl is using this create() but that method is never used
//    @Override
//    public PcuHeaderMessageModel create(PcuHeaderRequest pcuHeaderRequest) throws Exception
//    {
//        List<PcuHeader> listOutput = new ArrayList<>();
//        List<MessageDetails> message_details = new ArrayList<>();
//        List<BomList> bomLists = new ArrayList<>();
//        List<RouterList> routerLists = new ArrayList<RouterList>();
//        if(!pcuHeaderRequest.getPcuBos().isEmpty())
//        {
//            for(int i=0;i<pcuHeaderRequest.getPcuBos().size();i++)
//            {
//                if(pcuHeaderRepository.existsBySiteAndActiveAndPcuBO(pcuHeaderRequest.getShopOrder().getSite(),1,"PcuBO:"+pcuHeaderRequest.getShopOrder().getSite()+","+pcuHeaderRequest.getPcuBos().get(i).getPcuBo()))
//                {
//                    pcuHeaderRequest.setSite(pcuHeaderRequest.getShopOrder().getSite());
//                    pcuHeaderRequest.setPcuBO(pcuHeaderRequest.getPcuBos().get(i).getPcuBo());
//                    Response deleteResponse = delete(pcuHeaderRequest);
//                }
//                RouterList newRouterList =RouterList.builder().pcuRouterBO("RoutingBO:"+pcuHeaderRequest.getShopOrder().getSite()+","+pcuHeaderRequest.getShopOrder().getPlannedRouting()+","+pcuHeaderRequest.getShopOrder().getRoutingVersion()).status("New").build();
//                routerLists.add(newRouterList);
//                if(pcuHeaderRequest.getShopOrder().getPlannedBom()!=null) {
//                    if (!pcuHeaderRequest.getShopOrder().getPlannedBom().isEmpty()) {
//                        BomList newBomList = BomList.builder().pcuBomBO("PcuBomBO:" + pcuHeaderRequest.getShopOrder().getSite() + "," + pcuHeaderRequest.getShopOrder().getPlannedBom() + "," + pcuHeaderRequest.getShopOrder().getBomVersion()).status("New").build();
//                        bomLists.add(newBomList);
//                    }
//                }
//                PcuHeader createPcuHeader = PcuHeader.builder()
//                        .handle("PcuHeaderBO:"+pcuHeaderRequest.getShopOrder().getSite()+","+pcuHeaderRequest.getPcuBos().get(i).getPcuBo())
//                        .site(pcuHeaderRequest.getShopOrder().getSite())
//                        .pcuBO(pcuHeaderRequest.getPcuBos().get(i).getPcuBo())
//                        .shopOrderBO("ShopOrderBO:"+pcuHeaderRequest.getShopOrder().getSite()+","+pcuHeaderRequest.getShopOrder().getShopOrder())
//                        .itemBO("ItemBO:"+pcuHeaderRequest.getShopOrder().getSite()+","+pcuHeaderRequest.getShopOrder().getPlannedMaterial()+","+pcuHeaderRequest.getShopOrder().getMaterialVersion())
//                        .qtyInQueue(pcuHeaderRequest.getQtyInQueue())
//                        .routerList(routerLists)
//                        .bomList(bomLists)
//                        .parentOrderBO(pcuHeaderRequest.getParentOrderBO())
//                        .parentPcuBO(pcuHeaderRequest.getParentPcuBO())
//                        .active(1)
//                        .createdDateTime(LocalDateTime.now())
//                        .build();
//                pcuHeaderRepository.save(createPcuHeader);
//                listOutput.add(createPcuHeader);
//                PcuROuterHeaderCreateRequest pcuROuterHeaderRequest =PcuROuterHeaderCreateRequest.builder().userBO(pcuHeaderRequest.getUserBO()).site(pcuHeaderRequest.getShopOrder().getSite()).qtyInQueue(String.valueOf(pcuHeaderRequest.getQtyInQueue())).pcuBos(pcuHeaderRequest.getPcuBos()).pcuRouterBo("RoutingBO:"+pcuHeaderRequest.getShopOrder().getSite()+","+pcuHeaderRequest.getShopOrder().getPlannedRouting()+","+pcuHeaderRequest.getShopOrder().getRoutingVersion()).shopOrderBo("ShopOrderBO:"+pcuHeaderRequest.getShopOrder().getSite()+","+pcuHeaderRequest.getShopOrder().getShopOrder()).build();
//                RouterHeaderMessageModel createRouterHeader = createPcuRouterHeader(pcuROuterHeaderRequest);
//                MessageDetails messageDetails = MessageDetails.builder().msg(createRouterHeader.getMessage_details().get(0).getMsg()).msg_type(createRouterHeader.getMessage_details().get(0).getMsg_type()).build();
//                message_details.add(messageDetails);
//                   }
//            if(pcuHeaderRequest.getShopOrder().getPlannedBom()!=null) {
//                if (!pcuHeaderRequest.getShopOrder().getPlannedBom().isEmpty()) {
//                    Bom retrievedBom = retrieveBom(pcuHeaderRequest);
//                    BomHeaderRequest bomHeaderRequest = BomHeaderRequest.builder().pcuBos(pcuHeaderRequest.getPcuBos()).bom(retrievedBom).qtyInQueue(pcuHeaderRequest.getQtyInQueue()).build();
//                    BomHeaderMessageModel createBomHeader = createBomHeader(bomHeaderRequest);
//                }
//            }
//            String createdMessage = getFormattedMessage(12);
//            PcuHeaderMessageModel pcuHeaderMessageModel = PcuHeaderMessageModel.builder().pcuHeaderResponse(listOutput).message(createdMessage).pcuHeaderMessage_details(message_details).message(message_details.get(0).getMsg()).build();
//            return pcuHeaderMessageModel;
//        }
//        throw new PcuHeaderException(2703,pcuHeaderRequest.getPcuBos());
//    }

    @KafkaListener(topics = "pcu-header-create", groupId = "log-group", containerFactory = "kafkaListenerContainerFactory")
    public void pcuHeaderCreation(String res) throws Exception {
        System.out.println("14");
        ObjectMapper objectMapper = new ObjectMapper();
        PcuHeaderRequest message = objectMapper.readValue(res, new TypeReference<PcuHeaderRequest>() {});
        try {
            create(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public PcuHeaderMessageModel create(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        List<PcuHeader> listOutput = new ArrayList<>();
        List<BomList> bomLists = new ArrayList<>();
        List<RouterList> routerLists = new ArrayList<RouterList>();

        if(!pcuHeaderRequest.getPcuNumberList().isEmpty())
        {
            if(pcuHeaderRequest.getShopOrder().getPlannedRouting()!=null && !pcuHeaderRequest.getShopOrder().getPlannedRouting().isEmpty()) {
                RouterList newRouterList = RouterList.builder().pcuRouterBO("RoutingBO:" + pcuHeaderRequest.getShopOrder().getSite() + "," + pcuHeaderRequest.getShopOrder().getPlannedRouting() + "," + pcuHeaderRequest.getShopOrder().getRoutingVersion()).status("New").build();
                routerLists.add(newRouterList);
            }
            if(pcuHeaderRequest.getShopOrder().getPlannedBom()!=null && !pcuHeaderRequest.getShopOrder().getPlannedBom().isEmpty()) {
                BomList newBomList = BomList.builder().pcuBomBO("PcuBomBO:" + pcuHeaderRequest.getShopOrder().getSite() + "," + pcuHeaderRequest.getShopOrder().getPlannedBom() + "," + pcuHeaderRequest.getShopOrder().getBomVersion()).status("New").build();
                bomLists.add(newBomList);
            }
            List<PcuHeader> pcuHeadersToInsert = new ArrayList<>();
            for(Map.Entry<String, Integer> pcus : pcuHeaderRequest.getPcuNumberList().entrySet()) {

                PcuHeader createPcuHeader = PcuHeader.builder()
                        .handle("PcuHeaderBO:" + pcuHeaderRequest.getShopOrder().getSite() + "," + pcus.getKey())
                        .site(pcuHeaderRequest.getShopOrder().getSite())
                        .pcuBO(String.valueOf(pcus.getKey()))
                        .shopOrderBO("ShopOrderBO:" + pcuHeaderRequest.getShopOrder().getSite() + "," + pcuHeaderRequest.getShopOrder().getShopOrder())
                        .itemBO("ItemBO:" + pcuHeaderRequest.getShopOrder().getSite() + "," + pcuHeaderRequest.getShopOrder().getPlannedMaterial() + "," + pcuHeaderRequest.getShopOrder().getMaterialVersion())
                        .qtyInQueue(pcus.getValue())
                        .routerList(routerLists)
                        .bomList(bomLists)
                        .parentOrderBO(pcuHeaderRequest.getParentOrderBO())
                        .parentPcuBO(pcuHeaderRequest.getParentPcuBO())
                        .active(1)
                        .createdDateTime(LocalDateTime.now())
                        .build();
                pcuHeadersToInsert.add(createPcuHeader);
//                pcuHeaderRepository.save(createPcuHeader);
//                listOutput.add(createPcuHeader);
            }
            pcuHeaderRepository.saveAll(pcuHeadersToInsert);
            listOutput.addAll(pcuHeadersToInsert);

            if (pcuHeaderRequest.getShopOrder().getPlannedRouting() != null && !pcuHeaderRequest.getShopOrder().getPlannedRouting().isEmpty()) {
                PcuROuterHeaderCreateRequest pcuROuterHeaderRequest = PcuROuterHeaderCreateRequest.builder().
                        userBO(pcuHeaderRequest.getUserBO()).site(pcuHeaderRequest.getShopOrder().getSite()).
                        pcuNumberList(pcuHeaderRequest.getPcuNumberList()).
                        pcuRouterBo("RoutingBO:" + pcuHeaderRequest.getShopOrder().getSite() + "," + pcuHeaderRequest.getShopOrder().getPlannedRouting() + "," + pcuHeaderRequest.getShopOrder().getRoutingVersion()).shopOrderBo("ShopOrderBO:" + pcuHeaderRequest.getShopOrder().getSite() + "," + pcuHeaderRequest.getShopOrder().getShopOrder()).build();
                RouterHeaderMessageModel createRouterHeader = createPcuRouterHeader(pcuROuterHeaderRequest);
                MessageDetails.builder().msg_type(createRouterHeader.getMessage_details().get(0).getMsg_type()).build();
            }

            if (pcuHeaderRequest.getShopOrder().getPlannedBom() != null && !pcuHeaderRequest.getShopOrder().getPlannedBom().isEmpty()) {
                Bom retrievedBom = retrieveBom(pcuHeaderRequest);
                BomHeaderRequest bomHeaderRequest = BomHeaderRequest.builder().bom(retrievedBom).pcuNumberList(pcuHeaderRequest.getPcuNumberList()).build();
                BomHeaderMessageModel createBomHeader = createBomHeader(bomHeaderRequest);
                MessageDetails.builder().msg_type(createBomHeader.getMessage_details().getMsg_type()).build();
            }

                return PcuHeaderMessageModel.builder().pcuHeaderResponse(listOutput).build();
        }
        throw new PcuHeaderException(2703,pcuHeaderRequest.getPcuNumberList());
    }

    @Override
    public Bom retrieveBom(PcuHeaderRequest pcuHeaderRequest)
    {
        BomRequest bomRequest = BomRequest.builder().site(pcuHeaderRequest.getShopOrder().getSite()).bom(pcuHeaderRequest.getShopOrder().getPlannedBom()).revision(pcuHeaderRequest.getShopOrder().getBomVersion()).build();
        Bom retrieveBom = webClientBuilder.build()
                .post()
                .uri(bomUrl)
                .bodyValue(bomRequest)
                .retrieve()
                .bodyToMono(Bom.class)
                .block();
        return retrieveBom;
    }

    private List<PcuHeader> updateRouter(PcuHeader existingRecord,PcuHeaderRequest pcuHeaderRequest){
        List<PcuHeader> listOutput = new ArrayList<PcuHeader>();
        List<RouterList> routerLists=existingRecord.getRouterList();
        for(RouterList obj:routerLists){
            if(obj.getPcuRouterBO()!=null){
                obj.setPcuRouterBO(pcuHeaderRequest.getRouterBO());
            }
        }
        PcuHeader createPcuHeader = PcuHeader.builder()
                .handle(existingRecord.getHandle())
                .site(existingRecord.getSite())
                .pcuBO(existingRecord.getPcuBO())
                .shopOrderBO(existingRecord.getShopOrderBO())
                .itemBO(existingRecord.getItemBO())
                .qtyInQueue(existingRecord.getQtyInQueue())
                .routerList(routerLists)
                .bomList(existingRecord.getBomList())
                .parentOrderBO(existingRecord.getParentOrderBO())
                .parentPcuBO(existingRecord.getParentPcuBO())
                .active(1)
                .createdDateTime(existingRecord.getCreatedDateTime())
                .modifiedDateTime(LocalDateTime.now())
                .build();
        pcuHeaderRepository.save(createPcuHeader);
        listOutput.add(createPcuHeader);
        return listOutput;
    }
    @Override
    public List<PcuHeader> update(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        List<PcuHeader> listOutput = new ArrayList<PcuHeader>();
        if(!pcuHeaderRequest.getPcuBos().isEmpty())
        {
            for(int i=0;i<pcuHeaderRequest.getPcuBos().size();i++)
            {
                String newPcuBO = pcuHeaderRequest.getPcuBos().get(i).getPcuBo();
                if(pcuHeaderRepository.existsBySiteAndActiveAndPcuBO(pcuHeaderRequest.getPcuRequest().getSite(),1,newPcuBO))
                {
                    PcuHeader existingRecord = pcuHeaderRepository.findBySiteAndActiveAndPcuBO(pcuHeaderRequest.getPcuRequest().getSite(),1,newPcuBO);
                    if(pcuHeaderRequest.getRouterBO()!=null&&!pcuHeaderRequest.getRouterBO().equalsIgnoreCase("")) {
                        listOutput=updateRouter(existingRecord, pcuHeaderRequest);
                        return listOutput;
                    }
                    RouterList newRouterList = new RouterList(pcuHeaderRequest.getPcuRequest().getRouterList().get(0).getPcuRouterBO() , pcuHeaderRequest.getPcuRequest().getRouterList().get(0).getStatus());
                    List<RouterList> routerLists = new ArrayList<RouterList>();
                    routerLists.add(newRouterList);
                    BomList newBomList = new BomList(pcuHeaderRequest.getPcuRequest().getBomList().get(0).getPcuBomBO(),pcuHeaderRequest.getPcuRequest().getBomList().get(0).getStatus());
                    List<BomList> bomLists = new ArrayList<BomList>();
                    bomLists.add(newBomList);
                    PcuHeader createPcuHeader = PcuHeader.builder()
                            .handle(existingRecord.getHandle())
                            .site(existingRecord.getSite())
                            .pcuBO(existingRecord.getPcuBO())
                            .shopOrderBO(existingRecord.getShopOrderBO())
                            .itemBO(existingRecord.getItemBO())
                            .qtyInQueue(pcuHeaderRequest.getPcuBos().size())
                            .routerList(routerLists)
                            .bomList(bomLists)
                            .parentOrderBO(pcuHeaderRequest.getParentOrderBO())
                            .parentPcuBO(pcuHeaderRequest.getPcuBomBO())
                            .active(1)
                            .createdDateTime(existingRecord.getCreatedDateTime())
                            .modifiedDateTime(LocalDateTime.now())
                            .build();
                    pcuHeaderRepository.save(createPcuHeader);
                    listOutput.add(createPcuHeader);
                }
                else {
                    throw new PcuHeaderException(2701,newPcuBO);
                }

            }
            return listOutput;
        }
        throw new PcuHeaderException(2703,pcuHeaderRequest.getPcuBos());
    }

    @Override
    public List<PcuHeader> retrieve(String site) throws Exception {
        List<PcuHeader> retrievedRecord = pcuHeaderRepository.findBySiteAndActive(site, 1);
        for (PcuHeader pcuHeader : retrievedRecord) {
            String[] pcuBO = pcuHeader.getPcuBO().split(",");
            if (pcuBO.length == 2) {
                String pcu = pcuBO[1];
                pcuHeader.setPcuBO(pcu);
            }
            String[] itemBO = pcuHeader.getItemBO().split(",");
            if (itemBO.length == 3) {
                String item = itemBO[1];
                pcuHeader.setItemBO(item);
            }
            String[] shopOrderBO = pcuHeader.getShopOrderBO().split(",");
            if (shopOrderBO.length == 2) {
                String shopOrder = shopOrderBO[1];
                pcuHeader.setShopOrderBO(shopOrder);
            }
        }
        return retrievedRecord;
    }

    @Override
    public PcuHeader retrieveByPcuBO(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        return pcuHeaderRepository.findBySiteAndActiveAndPcuBO(pcuHeaderRequest.getSite(),1,pcuHeaderRequest.getPcuBO());
    }
    @Override
    public List<PcuHeader> retrieveByShopOrder(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        return pcuHeaderRepository.findBySiteAndActiveAndShopOrderBO(pcuHeaderRequest.getSite(),1,pcuHeaderRequest.getShopOrderBO());
    }

    @Override
    public List<PcuHeader> retrieveByPcuBOShopOrder(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        List<PcuHeader> retrievedRecord = new ArrayList<>();
        if(pcuHeaderRequest.getShopOrder()!=null && pcuHeaderRequest.getShopOrder().getShopOrder()!=null && !pcuHeaderRequest.getShopOrder().getShopOrder().isEmpty()) {
            retrievedRecord = pcuHeaderRepository.findBySiteAndActiveAndShopOrderBO(pcuHeaderRequest.getSite(), 1, pcuHeaderRequest.getShopOrderBO());

        }
        return retrievedRecord;
    }

    @Override
    public Boolean isExist(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        if (pcuHeaderRequest.getPcuBO() != null && !pcuHeaderRequest.getPcuBO().isEmpty()) {
            return pcuHeaderRepository.existsBySiteAndActiveAndPcuBO(pcuHeaderRequest.getSite(),1,pcuHeaderRequest.getPcuBO());
        }
        throw new PcuHeaderException(2705,pcuHeaderRequest.getPcuBomBO());
    }

    @Override
    public Boolean CheckRouterReleased(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        PcuHeader existingRecord = pcuHeaderRepository.findBySiteAndPcuBOAndActiveEquals(pcuHeaderRequest.getSite(),pcuHeaderRequest.getPcuBO(),1);
        if(existingRecord.getRouterList().get(0).getStatus().equals("Released"))
        {
            return true;
        }
        PcuROuterHeaderCreateRequest pcuROuterHeaderRequest =PcuROuterHeaderCreateRequest.builder().site(pcuHeaderRequest.getSite()).pcuBos(pcuHeaderRequest.getPcuBos()).pcuRouterBo("RoutingBO:"+pcuHeaderRequest.getSite()+","+pcuHeaderRequest.getShopOrder().getPlannedRouting()+","+pcuHeaderRequest.getShopOrder().getRoutingVersion()).build();
        RouterHeaderMessageModel createRouterHeader = createPcuRouterHeader(pcuROuterHeaderRequest);
        if(createRouterHeader.getMessage_details().get(0).getMsg_type().equals("S"))
        {
            return true;
        }
        return false;
    }


    @Override
    public Boolean CheckBomReleased(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        Bom retrievedBom = retrieveBom(pcuHeaderRequest);
        PcuHeader existingRecord = pcuHeaderRepository.findBySiteAndPcuBOAndActiveEquals(pcuHeaderRequest.getSite(),pcuHeaderRequest.getPcuBO(),1);
        BomHeaderRequest bomHeaderRequest = BomHeaderRequest.builder().pcuBos(pcuHeaderRequest.getPcuBos()).pcuBomBO(existingRecord.getBomList().get(0).getPcuBomBO()).bom(retrievedBom).build();
        if(existingRecord.getBomList().get(0).getStatus().equals("Released"))
        {
            return (Boolean) true;
        }
        BomHeaderMessageModel createBomHeader = createBomHeader(bomHeaderRequest);
        if(createBomHeader.getMessage_details().getMsg_type().equals("S"))
        {
            return true;
        }
        return false;
    }

    @Override
    public PcuHeader updateStatusOfRouterToReleased(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        if(isExist(pcuHeaderRequest)) {
            PcuHeader existingRecord = pcuHeaderRepository.findBySiteAndPcuBOAndActiveEquals(pcuHeaderRequest.getSite(), pcuHeaderRequest.getPcuBO(), 1);
            existingRecord.getRouterList().get(0).setStatus("Released");
            return existingRecord;
        }
        throw new PcuHeaderException(2708,pcuHeaderRequest.getPcuBO());
    }
    @Override
    public PcuHeader updateStatusOfBomToReleased(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        if(isExist(pcuHeaderRequest)) {
            PcuHeader existingRecord = pcuHeaderRepository.findBySiteAndPcuBOAndActiveEquals(pcuHeaderRequest.getSite(), pcuHeaderRequest.getPcuBO(), 1);
            existingRecord.getBomList().get(0).setStatus("Released");
            return existingRecord;
        }
        throw new PcuHeaderException(2708,pcuHeaderRequest.getPcuBO());
    }

    @Override
    public Boolean checkIfBomAndRouterReleased(PcuHeaderRequest pcuHeaderRequest) throws  Exception
    {
        PcuHeader existingRecord = pcuHeaderRepository.findBySiteAndPcuBOAndActiveEquals(pcuHeaderRequest.getSite(),pcuHeaderRequest.getPcuBO(),1);
        if(existingRecord.getBomList().get(0).getStatus().equals("Released") && existingRecord.getRouterList().get(0).getStatus().equals("Released"))
        {
            return true;
        }
        throw new PcuHeaderException(2709,existingRecord.getRouterList().get(0).getStatus(),existingRecord.getBomList().get(0).getStatus());
    }

    @Override
    public Response delete(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        if(pcuHeaderRepository.existsBySiteAndActiveAndPcuBO(pcuHeaderRequest.getSite(),1,pcuHeaderRequest.getPcuBO()))
        {
            PcuHeader existingRecord = pcuHeaderRepository.findBySiteAndActiveAndPcuBO(pcuHeaderRequest.getSite(),1,pcuHeaderRequest.getPcuBO());
            existingRecord.setActive(0);
            pcuHeaderRepository.save(existingRecord);
            return Response.builder().message("Deleted "+ pcuHeaderRequest.getPcuBO()).build();
        }
        throw new PcuHeaderException(2701,pcuHeaderRequest.getPcuBO());
    }

    @Override
    public Response unDelete(PcuHeaderRequest pcuHeaderRequest) throws Exception
    {
        if(pcuHeaderRepository.existsBySiteAndActiveAndPcuBO(pcuHeaderRequest.getSite(),0,pcuHeaderRequest.getPcuBO()))
        {
            PcuHeader existingRecord = pcuHeaderRepository.findBySiteAndActiveAndPcuBO(pcuHeaderRequest.getSite(),0,pcuHeaderRequest.getPcuBO());
            existingRecord.setActive(1);
            pcuHeaderRepository.save(existingRecord);
            return Response.builder().message("Un_Deleted "+ pcuHeaderRequest.getPcuBO()).build();
        }
        throw new PcuHeaderException(2701,pcuHeaderRequest.getPcuBO());
    }


    @Override
    public List<PcuHeader> retrieveByItem(String site,String itemBO) throws Exception {
        List<PcuHeader> pcuHeaders=pcuHeaderRepository.findBySiteAndActiveAndItemBO(site,1,itemBO);
        return pcuHeaders;
    }

    @Override
    public List<PcuHeader> retrieveAll(String site) {
        List<PcuHeader> retrievedRecord = pcuHeaderRepository.findBySiteAndActive(site, 1);
        return retrievedRecord;
    }

    public BomHeaderMessageModel retrievePcuList(String site) {
        List<PcuHeader> pcuHeaders = pcuHeaderRepository.findBySiteAndActive(site, 1);
        List<PcuHeaderResponse> pcuHeaderResponseList = pcuHeaders.stream().map(pcuHeader -> {
            String pcu = BOConverter.getPcu(pcuHeader.getPcuBO());
            String shopOrder = BOConverter.getShopOrder(pcuHeader.getShopOrderBO());
            String item = BOConverter.getItem(pcuHeader.getItemBO());
            String itemVersion = BOConverter.getItemVersion(pcuHeader.getItemBO());

            return new PcuHeaderResponse(pcu, shopOrder, item, itemVersion);
        }).collect(Collectors.toList());


        return BomHeaderMessageModel.builder().pcuHeaderResponseList(pcuHeaderResponseList).build();
    }

    public BomHeaderMessageModel createBomHeader(BomHeaderRequest bomHeaderRequest){
        BomHeaderMessageModel createBomHeader = webClientBuilder.build()
                .post()
                .uri(bomHeaderUrl)
                .bodyValue(bomHeaderRequest)
                .retrieve()
                .bodyToMono(BomHeaderMessageModel.class)
                .block();
        return createBomHeader;
    }
    public RouterHeaderMessageModel createPcuRouterHeader(PcuROuterHeaderCreateRequest pcuROuterHeaderRequest)throws Exception
    {
        RouterHeaderMessageModel createRouterHeader = webClientBuilder.build()
                .post()
                .uri(pcuRouterHeaderUrl)
                .bodyValue(pcuROuterHeaderRequest)
                .retrieve()
                .bodyToMono(RouterHeaderMessageModel.class)
                .block();
        return createRouterHeader;
    }

    public List<String> retrievePcuHeaderList(PcuHeaderRequest pcuHeaderRequest) throws Exception {
        List<String> pcuHeaderList = new ArrayList<>();
        for(Pcu pcuBo : pcuHeaderRequest.getPcuBos()){
            boolean exists = pcuHeaderRepository.existsBySiteAndActiveAndPcuBO(pcuHeaderRequest.getSite(),1, pcuBo.getPcuBo());
            if (!exists) {
                pcuHeaderList.add(pcuBo.getPcuBo());
            }
        }
        return pcuHeaderList;
    }
}
