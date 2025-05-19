
package com.rits.nonconformanceservice.service;

import com.rits.dispositionlogservice.dto.DispositionLogRequest;
import com.rits.nonconformanceservice.dto.*;
import com.rits.nonconformanceservice.exception.NonConformanceserviceException;
import com.rits.nonconformanceservice.model.NcData;
import com.rits.nonconformanceservice.repository.NonConformanceserviceRepository;
import com.rits.pcuheaderservice.dto.PcuHeaderRequest;
import com.rits.pcuheaderservice.dto.PcuRequest;
import com.rits.pcuheaderservice.model.PcuHeader;
import com.rits.pcuinqueueservice.dto.PcuInQueueRequest;
import com.rits.pcurouterheaderservice.dto.Pcu;
import com.rits.pcurouterheaderservice.dto.PcuROuterHeaderCreateRequest;
import com.rits.pcurouterheaderservice.model.MessageModel;
import com.rits.pcurouterheaderservice.model.PcuRouterHeader;
import com.rits.pcurouterheaderservice.service.PcuRouterHeaderServiceImpl;
import com.rits.pcurouterheaderservice.dto.PcuRouterHeaderRequest;
import com.rits.startservice.dto.StartRequest;
import com.rits.startservice.model.PcuInWork;
import com.rits.startservice.model.PcuInWorkMessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.rits.pcuinqueueservice.model.PcuInQueue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NonConformanceserviceImpl implements NonConformanceservice {
    private final NonConformanceserviceRepository nonConformanceserviceRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${nccode-service.url}/retrieve")
    private String nccodeUrl;
    @Value("${routing-service.url}/isExist")
    private String routingUrl;

    @Value("${pcurouterheader-service.url}/disableRecord")
    private String pcurouterheaderUrl;
    @Value("${pcurouterheader-service.url}/retrieve")
    private String pcurouterheaderRetrieveUrl;
    @Value("${pcurouterheader-service.url}/create")
    private String pcurouterheaderCreateUrl;
    @Value("${pcuinqueue-service.url}/update")
    private String pcuinqueueurl;
    @Value("${pcuinqueue-service.url}/create")
    private String createinqueueurl;
    @Value("${pcuinqueue-service.url}/getAllPcuByRoute")
    private String pcuinqueuegeturl;
    @Value("${pcuinqueue-service.url}/getAllPcuByRoute")
    private String getqpcusUrl;
    @Value("${start-service.url}/update")
    private String updateInworkurl;
    @Value("${start-service.url}/getAllPcuByRoute")
    private String getInworkurl;

    @Value("${routing-service.url}/retrieve")
    private String getRoutingUrl;

    @Value("${nccode-service.url}/getDispositionRouting")
    private String getDispositionRouteUrl;

    @Value("${dispositionlog-service.url}/create")
    private String dispositionlogurl;
    @Value("${pcuheader-service.url}/update")
    private String updtPcuHedUrl;
    private List < PcuInQueue > pcuListRef = new ArrayList < PcuInQueue > ();
    @Autowired
    private PcuRouterHeaderServiceImpl pcuRouterHeaderService;
    private String tooperationBO;

    @Override
    public List < DispositionRoutings > logNc(NcRequest ncRequest) throws Exception {
        NCCode ncCodeResponce = getNcCodeDetails(ncRequest.getNcCodeBo(), ncRequest.getSite());
        List < DispositionRoutings > dispositionRoutings = new ArrayList < DispositionRoutings > ();
        String status = "C";
        if (ncCodeResponce.isClosureRequired()) {
            status = "O";
        }
        String routing = (ncCodeResponce == null || ncCodeResponce.getDispositionRoutingsList() == null || ncCodeResponce.getDispositionRoutingsList().isEmpty()) ?
                null : ncCodeResponce.getDispositionRoutingsList().get(0).getRouting();
        if (ncCodeResponce != null) {
            if (ncCodeResponce.getStatus().equalsIgnoreCase("Enabled")) {
                if (ncRequest.getParentNcCodeBo().isEmpty() || ncRequest.getParentNcCodeBo() == null) {
                    NcData ncdata = NcData.builder()
                            .site(ncRequest.getSite())
                            .handle("NCDataBO:" + ncRequest.getNcCodeBo() + "," + ncRequest.getPcuBO() + "," + ncRequest.getUserBo() + "," + LocalDateTime.now())
                            .changeStamp("0")
                            .ncContextGbo(ncRequest.getPcuBO())
                            .userBo(ncRequest.getUserBo())
                            .dateTime(LocalDateTime.now().toString())
                            .sequence("0")
                            .ncState(status)
                            .ncCodeBo(ncRequest.getNcCodeBo())
                            .ncDataTypeBo(ncCodeResponce.getNcDatatype())
                            .qty(ncRequest.getQty())
                            .defectCount(ncRequest.getDefectCount())
                            .componentBo(ncRequest.getComponentBo())
                            .compContextGbo(ncRequest.getCompContextGbo())
                            .comments(ncRequest.getComments())
                            .routerBo(ncRequest.getRouterBo())
                            .dispositionRouterBo(routing)
                            .stepId(ncRequest.getStepID())
                            .operationBo(ncRequest.getOperationBO())
                            .timesProcessed(0)
                            .resourceBo(ncRequest.getResourceBo())
                            .workCenterBo(ncRequest.getWorkCenterBo())
                            .itemBo(ncRequest.getItemBo())
                            .closureRequired(ncCodeResponce.isClosureRequired())
                            .failureId(ncCodeResponce.getNcCategory())
                            .verifiedState("V")
                            .createdDateTime(LocalDateTime.now().toString())
                            .ncCategory(ncCodeResponce.getNcCategory())
                            .verifiedDateTime(LocalDateTime.now().toString())
                            .reportingCenterBo(ncRequest.getSite())
                            .transferredToDpmo(false)
                            .dataFieldsList(ncRequest.getDataFieldsList())
                            .count(ncRequest.getCount())
                            .build();
                    nonConformanceserviceRepository.save(ncdata);
                    if (ncdata.getNcCategory().equalsIgnoreCase("Failure")) {
                        dispositionRoutings = ncCodeResponce.getDispositionRoutingsList();
                    }

                } else {
                    NcData parentNcData = getparentNcCode(ncRequest.getPcuBO(), ncRequest.getParentNcCodeBo());
                    if (parentNcData != null) {
                        List < SecondaryNCData > secondaryNcList = new ArrayList < SecondaryNCData > ();
                        List < SecondaryNCData > secondaryList = parentNcData.getSecondaryNCDataList();
                        if (secondaryList != null && !secondaryList.isEmpty()) {
                            secondaryNcList = parentNcData.getSecondaryNCDataList();
                            SecondaryNCData secondaryNCData = new SecondaryNCData();
                            if (ncRequest.getChildParentNcCodeBo().equals(null) || ncRequest.getChildParentNcCodeBo().equalsIgnoreCase("") && parentNcData.getNcCodeBo().equalsIgnoreCase(ncRequest.getParentNcCodeBo())) {
                                secondaryNCData=insertNcData(ncRequest,ncCodeResponce,status,routing);
                                secondaryNcList.add(secondaryNCData);
                                parentNcData.setCount(ncRequest.getCount());
                                parentNcData.setSecondaryNCDataList(secondaryNcList);
                                nonConformanceserviceRepository.save(parentNcData);
                                if(ncCodeResponce.isAutoClosePrimaryNC()){
                                    autoClosePrimaryNc(parentNcData,ncRequest);
                                }
                            } else {
                                logSecondaryChildNc(parentNcData, ncRequest, status, ncCodeResponce, routing);

                            }
                        } else {
                            SecondaryNCData secondaryNCData = new SecondaryNCData();
                            secondaryNCData=insertNcData(ncRequest,ncCodeResponce,status,routing);
                            secondaryNcList.add(secondaryNCData);
                            parentNcData.setCount(ncRequest.getCount());
                            parentNcData.setSecondaryNCDataList(secondaryNcList);
                            nonConformanceserviceRepository.save(parentNcData);
                            if(ncCodeResponce.isAutoClosePrimaryNC()){
                                autoClosePrimaryNc(parentNcData,ncRequest);
                            }
                        }
                        if (parentNcData.getNcCategory().equalsIgnoreCase("Failure")) {
                            dispositionRoutings = ncCodeResponce.getDispositionRoutingsList();
                        }
                    } else {
                        throw new NonConformanceserviceException(6002);
                    }
                }
            } else {
                throw new NonConformanceserviceException(6001);
            }
        }
        return dispositionRoutings;
    }

    private boolean logSecondaryChildNc(NcData parentNcdata, NcRequest ncRequest, String status, NCCode ncCodeResponce, String routing) {
        Boolean logged = false;
        List < SecondaryNCData > secondaryNcList = parentNcdata.getSecondaryNCDataList();
        SecondaryNCData secondaryNCData = new SecondaryNCData();
        for (SecondaryNCData secondaryNCDataobj: secondaryNcList) {
            List < SecondaryNCData > childsecondaryNcList = secondaryNCDataobj.getSecondaryNCDataList();
            if (childsecondaryNcList != null && !childsecondaryNcList.isEmpty()) {
                for (SecondaryNCData childsecondaryNCDataobj: childsecondaryNcList) {
                    if (childsecondaryNCDataobj.getNcCodeBo().equalsIgnoreCase(ncRequest.getChildParentNcCodeBo())) {
                        secondaryNCData=insertNcData(ncRequest,ncCodeResponce,status,routing);
                        List<SecondaryNCData> childlist=new ArrayList<SecondaryNCData>();
                        childlist.add(secondaryNCData);
                        childsecondaryNCDataobj.setSecondaryNCDataList(childlist);
                        parentNcdata.setCount(ncRequest.getCount());
                        nonConformanceserviceRepository.save(parentNcdata);
                        if(ncCodeResponce.isAutoClosePrimaryNC()){
                            autoClosePrimaryNc(parentNcdata,ncRequest);
                        }
                        logged=true;
                        return logged;
                    }

                }

            } else {
                secondaryNCData=insertNcData(ncRequest,ncCodeResponce,status,routing);
                List<SecondaryNCData> childlist=new ArrayList<SecondaryNCData>();
                childlist.add(secondaryNCData);
                secondaryNCDataobj.setSecondaryNCDataList(childlist);
                parentNcdata.setCount(ncRequest.getCount());
                nonConformanceserviceRepository.save(parentNcdata);
                if(ncCodeResponce.isAutoClosePrimaryNC()){
                  autoClosePrimaryNc(parentNcdata,ncRequest);
                }
                logged=true;
                return logged;
            }

        }
        return logged;
    }
    private SecondaryNCData insertNcData(NcRequest ncRequest, NCCode ncCodeResponce , String status , String routing){
        SecondaryNCData secondaryNCData = SecondaryNCData.builder()
                .site(ncRequest.getSite())
                .handle("NCDataBO:" + ncRequest.getNcCodeBo() + "," + ncRequest.getUserBo() + "," + LocalDateTime.now())
                .changeStamp("0")
                .ncContextGbo(ncRequest.getPcuBO())
                .userBo(ncRequest.getUserBo())
                .dateTime(ncRequest.getDateTime())
                .sequence("0")
                .ncState(status)
                .ncCodeBo(ncRequest.getNcCodeBo())
                .ncDataTypeBo(ncCodeResponce.getNcDatatype())
                .qty(ncRequest.getQty())
                .defectCount(ncRequest.getDefectCount())
                .componentBo(ncRequest.getComponentBo())
                .compContextGbo(ncRequest.getCompContextGbo())
                .comments(ncRequest.getComments())
                .routerBo(ncRequest.getRouterBo())
                .dispositionRouterBo(routing)
                .stepId(ncRequest.getStepID())
                .operationBo(ncRequest.getOperationBO())
                .timesProcessed(0)
                .resourceBo(ncRequest.getResourceBo())
                .workCenterBo(ncRequest.getWorkCenterBo())
                .itemBo(ncRequest.getItemBo())
                .closureRequired(ncCodeResponce.isClosureRequired())
                .incidentDateTime(LocalDateTime.now().toString())
                .failureId(ncCodeResponce.getNcCategory())
                .verifiedState("V")
                .createdDateTime(LocalDateTime.now().toString())
                .modifiedDateTime(LocalDateTime.now().toString())
                .ncCategory(ncCodeResponce.getNcCategory())
                .verifiedDateTime(LocalDateTime.now().toString())
                .reportingCenterBo(ncRequest.getSite())
                .transferredToDpmo(false)
                .dataFieldsList(ncRequest.getDataFieldsList())
                .build();
        return secondaryNCData;
    }
    private NCCode getNcCodeDetails(String ncCodeBO, String Site) {
        String[] parts = ncCodeBO.split(",");
        NCCode ncCode = new NCCode();
        ncCode.setNcCode(parts[1]);
        ncCode.setSite(Site);
        NCCode ncCodeResponce = null;
        ncCodeResponce = webClientBuilder.build()
                .post()
                .uri(nccodeUrl)
                .bodyValue(ncCode)
                .retrieve()
                .bodyToMono(NCCode.class)
                .block();
        return ncCodeResponce;

    }
    private Boolean autoClosePrimaryNc(NcData parentNcdata, NcRequest ncRequest){
        Boolean closed=false;
        parentNcdata.setNcState("C");
        List<SecondaryNCData> list = parentNcdata.getSecondaryNCDataList();
        for(SecondaryNCData obj:list){
            if(!obj.getNcCodeBo().equalsIgnoreCase(ncRequest.getNcCodeBo())){
                obj.setNcState("C");
            }
        }
        nonConformanceserviceRepository.save(parentNcdata);
        closed=true;
        return closed;
    }


    private NcData findSecondaryOrPrimaryNc(String pcuBo, String NcCodeBO) {
        NcData ncData = new NcData();

        return ncData;
    }
    @Override
    public List < NcData > getNcData(String PCUBo, String OperationBO, String ResourceBO) {
        List < NcData > ncDataList = new ArrayList < NcData > ();
        ncDataList = nonConformanceserviceRepository.findByNcContextGboAndOperationBoAndResourceBo(PCUBo, OperationBO, ResourceBO);
        return ncDataList;
    }
    @Override
    public Boolean closeNC(NcRequest ncRequest) {
        Boolean closed = false;
        NCCode ncCodeResponce = getNcCodeDetails(ncRequest.getNcCodeBo(), ncRequest.getSite());
        if (!ncRequest.getParentNcCodeBo().isEmpty() || ncRequest.getParentNcCodeBo() != null) {
            if (ncCodeResponce.isAutoClosePrimaryNC()) {
                closed = closePrimaryNc(ncRequest);
            } else {
                List < NcData > ncDataList = nonConformanceserviceRepository.findByNcCodeBoAndNcContextGbo(ncRequest.getNcCodeBo(), ncRequest.getPcuBO());
                for (NcData ncdata: ncDataList) {
                    ncdata.setNcState("C");
                    ncdata.setClosedDateTime(ncRequest.getDateTime());
                    ncdata.setClosedUserBo(ncRequest.getUserBo());
                    ncdata.setModifiedDateTime(ncRequest.getDateTime());
                    nonConformanceserviceRepository.save(ncdata);
                    closed = true;
                }

            }
        }
        return closed;
    }

    private Boolean closePrimaryNc(NcRequest ncRequest) {
        Boolean primaryNCclosed = false;
        //NcData ncdata=new NcData();
        NcData ncdata = nonConformanceserviceRepository.findBySecondaryNCDataListNcCodeBoAndNcContextGbo(ncRequest.getNcCodeBo(), ncRequest.getPcuBO());
        ncdata.setNcState("C");
        ncdata.setClosedDateTime(ncRequest.getDateTime());
        ncdata.setClosedUserBo(ncRequest.getUserBo());
        ncdata.setModifiedDateTime(ncRequest.getDateTime());
        List < SecondaryNCData > secondaryNcdatalist = ncdata.getSecondaryNCDataList();
        for (SecondaryNCData secondaryNCData: secondaryNcdatalist) {
            if (secondaryNCData.getNcCodeBo().equalsIgnoreCase(ncRequest.getNcCodeBo())) {
                secondaryNCData.setNcState("C");
                secondaryNCData.setClosedDateTime(ncRequest.getDateTime());
                secondaryNCData.setClosedUserBo(ncRequest.getUserBo());
                secondaryNCData.setModifiedDateTime(ncRequest.getDateTime());
            }
        }
        nonConformanceserviceRepository.save(ncdata);
        return primaryNCclosed;
    }

    public List < NcData > getAllNcByPCU(String PCUBO) {
        List < NcData > ncDataList;
        ncDataList = nonConformanceserviceRepository.findByNcContextGbo(PCUBO);
        return ncDataList;
    }

    public NcData getparentNcCode(String PCUBO, String ParentNcCodeBo) {
        List < NcData > ncDataList = nonConformanceserviceRepository.findByNcContextGboAndNcCodeBo(PCUBO, ParentNcCodeBo);
        if (ncDataList.size() != 0) {
            return ncDataList.get(0);
        }
        return null;
    }

    public Boolean donePCU(DispositionRequest dispositionRequest) throws NonConformanceserviceException {
        Boolean done = false;
        if (checkIsRoutingValid(dispositionRequest.getDispositionRoutingBo())) {
            PcuRouterHeader pcuRouterHeader = disableOldPcuHeader(dispositionRequest.getPcuBO(), dispositionRequest.getSite());
            if (pcuRouterHeader != null) {
                if (updateAllStatePcu(pcuRouterHeader, dispositionRequest)) {
                        if (createPcuheader(dispositionRequest)) {
                            if(updatePcuHeader(dispositionRequest)){
                            if (putInQueue(dispositionRequest)) {
                                if (createDispositionLog(dispositionRequest)) {
                                    done = true;
                                }
                            }
                        }
                    }
                }
            }

        } else {
            throw new NonConformanceserviceException(70001);
        }

        return done;
    }

    private Boolean updatePcuHeader(DispositionRequest dispositionRequest){
        Boolean updated=false;
        PcuHeaderRequest pcuHeaderRequest=new PcuHeaderRequest();
        pcuHeaderRequest.setSite(dispositionRequest.getSite());
        List<com.rits.pcuheaderservice.dto.Pcu> pcuList=new ArrayList<com.rits.pcuheaderservice.dto.Pcu>();
        com.rits.pcuheaderservice.dto.Pcu pcu=new com.rits.pcuheaderservice.dto.Pcu();
        pcu.setPcuBo(dispositionRequest.getPcuBO());
        pcuList.add(pcu);
        pcuHeaderRequest.setPcuBos(pcuList);
        PcuRequest pcuRequest=new PcuRequest();
        pcuRequest.setPcuBO(dispositionRequest.getPcuBO());
        pcuRequest.setSite(dispositionRequest.getSite());
        pcuHeaderRequest.setPcuRequest(pcuRequest);
        pcuHeaderRequest.setRouterBO(dispositionRequest.getDispositionRoutingBo());

        List<PcuHeader> res=webClientBuilder.build()
                .post()
                .uri(updtPcuHedUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToFlux(PcuHeader.class)
                .collectList()
                .block();
        updated=true;
        return updated;
    }
    public Boolean checkIsRoutingValid(String RoutingBO) {
        Boolean valid = true;
        RoutingRequest routingRequest = new RoutingRequest();
        String[] parts = RoutingBO.split(":");
        String[] routval = parts[1].split(",");
        routingRequest.setRouting(routval[1]);
        routingRequest.setVersion(routval[2]);
        routingRequest.setSite(routval[0]);
        RoutingRequest routing = webClientBuilder.build()
                .post()
                .uri(nccodeUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(RoutingRequest.class)
                .block();
        if (routing != null) {
            valid = true;
        }
        return valid;
    }

    private PcuRouterHeader disableOldPcuHeader(String PcuBO, String site) {
        Boolean disabled = false;
        PcuRouterHeader res = getPcuRouterHeader(PcuBO, site);
        PcuRouterHeaderRequest pcuRouterHeader = new PcuRouterHeaderRequest();
        pcuRouterHeader.setPcuBo(res.getPcuBo());
        pcuRouterHeader.setSite(res.getSite());
        pcuRouterHeader.setPcuRouterBo(res.getPcuRouterBo());
        MessageModel message = webClientBuilder.build()
                .post()
                .uri(pcurouterheaderUrl)
                .bodyValue(pcuRouterHeader)
                .retrieve()
                .bodyToMono(MessageModel.class)
                .block();
        disabled = true;
        return res;
    }
    private PcuRouterHeader getPcuRouterHeader(String PcuBO, String site) {
        PcuRouterHeaderRequest pcuRouterHeaderRequest = new PcuRouterHeaderRequest();
        pcuRouterHeaderRequest.setSite(site);
        pcuRouterHeaderRequest.setPcuBo(PcuBO);
        PcuRouterHeader pcuRouterHeader = webClientBuilder.build()
                .post()
                .uri(pcurouterheaderRetrieveUrl)
                .bodyValue(pcuRouterHeaderRequest)
                .retrieve()
                .bodyToMono(PcuRouterHeader.class)
                .block();
        return pcuRouterHeader;
    }
    private Boolean createPcuheader(DispositionRequest dispositionRequest) {
        Boolean created = false;
        //
        PcuROuterHeaderCreateRequest pcuROuterHeaderCreateRequest = new PcuROuterHeaderCreateRequest();
        List < Pcu > pcuList = new ArrayList < Pcu > ();
        Pcu pcu = new Pcu();
        pcu.setPcuBo(dispositionRequest.getPcuBO());
        pcuList.add(pcu);
        pcuROuterHeaderCreateRequest.setPcuBos(pcuList);
        pcuROuterHeaderCreateRequest.setSite(dispositionRequest.getSite());
        pcuROuterHeaderCreateRequest.setPcuRouterBo(dispositionRequest.getDispositionRoutingBo());
        pcuROuterHeaderCreateRequest.setUserBO(dispositionRequest.getUserBo());
        pcuROuterHeaderCreateRequest.setQtyInQueue(dispositionRequest.getQty());
        pcuROuterHeaderCreateRequest.setShopOrderBo(dispositionRequest.getShoporderBO());
        com.rits.pcurouterheaderservice.model.MessageModel messageModel = webClientBuilder.build()
                .post()
                .uri(pcurouterheaderCreateUrl)
                .bodyValue(pcuROuterHeaderCreateRequest)
                .retrieve()
                .bodyToMono(com.rits.pcurouterheaderservice.model.MessageModel.class)
                .block();
        created = true;
        return created;
    }

    private Boolean updateAllStatePcu(PcuRouterHeader pcuRouterHeader, DispositionRequest dispositionRequest) {
        Boolean updated = false;
        MessageModel messageModel = new MessageModel();
        List < PcuInQueue > pcuList = getQueuedPcus(dispositionRequest);
        if (pcuList.size() > 0 && !pcuList.isEmpty()) {
            pcuListRef.addAll(pcuList);
            PcuInQueueRequest pcuInQueueRequest = new PcuInQueueRequest();
            pcuInQueueRequest.setPcuList(pcuList);
            pcuInQueueRequest.setDisable(true);
            com.rits.pcuinqueueservice.model.MessageModel pcuinqueModel = webClientBuilder.build()
                    .post()
                    .uri(pcuinqueueurl)
                    .bodyValue(pcuInQueueRequest)
                    .retrieve()
                    .bodyToMono(com.rits.pcuinqueueservice.model.MessageModel.class)
                    .block();
            if (pcuinqueModel.getResponse() != null) {
                updated = true;
            }
        } else {
            updated = true;
        }
        if (updated) {
            List < PcuInWork > inworkList = getActivePcus(pcuRouterHeader.getPcuBo(), pcuRouterHeader.getPcuRouterBo());
            if (inworkList.size() > 0 && !inworkList.isEmpty()) {
                StartRequest workObj = new StartRequest();
                workObj.setInWorkList(inworkList);
                workObj.setDisable(true);
                PcuInWorkMessageModel pcuInWorkMessageModel = webClientBuilder.build()
                        .post()
                        .uri(updateInworkurl)
                        .bodyValue(workObj)
                        .retrieve()
                        .bodyToMono(PcuInWorkMessageModel.class)
                        .block();
                if (pcuInWorkMessageModel.getResponse() != null) {
                    updated = true;
                }
            } else {
                updated = true;
            }
        }

        return updated;
    }

    private List < PcuInQueue > getQueuedPcus(DispositionRequest dispositionRequest) {

        PcuInQueueRequest pcuInQueueRequest = new PcuInQueueRequest();
        pcuInQueueRequest.setActive(1);
        pcuInQueueRequest.setPcuBO(dispositionRequest.getPcuBO());
        pcuInQueueRequest.setRouterBO(dispositionRequest.getRouterBo());
        pcuInQueueRequest.setItemBO(dispositionRequest.getItemBo());
        pcuInQueueRequest.setSite(dispositionRequest.getSite());
        pcuInQueueRequest.setResourceBO(dispositionRequest.getResourceBo());
        List < PcuInQueue > pcuList = webClientBuilder.build()
                .post()
                .uri(pcuinqueuegeturl)
                .bodyValue(pcuInQueueRequest)
                .retrieve()
                .bodyToFlux(PcuInQueue.class)
                .collectList()
                .block();
        return pcuList;
    }

    private List < PcuInWork > getActivePcus(String PcuBo, String OldRoutingBo) {
        StartRequest startRequest = new StartRequest();
        startRequest.setActive(1);
        startRequest.setPcuBO(PcuBo);
        startRequest.setRouterBO(OldRoutingBo);
        List < PcuInWork > pcuList = webClientBuilder.build()
                .post()
                .uri(getInworkurl)
                .bodyValue(startRequest)
                .retrieve()
                .bodyToFlux(PcuInWork.class)
                .collectList()
                .block();
        return pcuList;
    }

    private Boolean putInQueue(DispositionRequest dispositionRequest) {
        if(dispositionRequest.getToOperationBo()!=null&&!dispositionRequest.getToOperationBo().equalsIgnoreCase("")){
            tooperationBO=dispositionRequest.getToOperationBo();
        }
        else{
            tooperationBO = getFirstOperationBO(dispositionRequest.getDispositionRoutingBo(), dispositionRequest.getSite());
        }
        Boolean success = false;

        PcuInQueueRequest pcuInQueueRequest = new PcuInQueueRequest();
        pcuInQueueRequest.setRouterBO(dispositionRequest.getDispositionRoutingBo());
        pcuInQueueRequest.setSite(dispositionRequest.getSite());
        pcuInQueueRequest.setPcuBO(dispositionRequest.getPcuBO());
        pcuInQueueRequest.setResourceBO(dispositionRequest.getResourceBo());
        pcuInQueueRequest.setShopOrderBO(dispositionRequest.getShoporderBO());
        pcuInQueueRequest.setOperationBO(tooperationBO);
        pcuInQueueRequest.setItemBO(dispositionRequest.getItemBo());
        pcuInQueueRequest.setQtyInQueue(dispositionRequest.getQty());
        pcuInQueueRequest.setQtyToComplete(dispositionRequest.getQty());
        pcuInQueueRequest.setWorkCenter(dispositionRequest.getWorkCenterBo());
        pcuInQueueRequest.setShopOrderBO(dispositionRequest.getShoporderBO());
        pcuInQueueRequest.setUserBO(dispositionRequest.getUserBo());
        pcuInQueueRequest.setStepID(dispositionRequest.getStepID());

        try {
            com.rits.pcuinqueueservice.model.MessageModel messageModel = webClientBuilder.build()
                    .post()
                    .uri(createinqueueurl)
                    .bodyValue(pcuInQueueRequest)
                    .retrieve()
                    .bodyToMono(com.rits.pcuinqueueservice.model.MessageModel.class)
                    .block();
            success = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return success;
    }

    private String getFirstOperationBO(String DispositionRoutingBO, String site) {
        String OperationBO = "";
        RoutingRequest routingRequest = new RoutingRequest();
        String[] parts = DispositionRoutingBO.split(",");
        routingRequest.setRouting(parts[1]);
        routingRequest.setVersion(parts[2]);
        routingRequest.setSite(site);
        Routing routing = webClientBuilder.build()
                .post()
                .uri(getRoutingUrl)
                .bodyValue(routingRequest)
                .retrieve()
                .bodyToMono(Routing.class)
                .block();
        List < RoutingStep > steplist = routing.getRoutingStepList();
        for (RoutingStep stepObj: steplist) {
            if (stepObj.getStepType().equalsIgnoreCase("Operation") && stepObj.isEntryStep()) {
                OperationBO = "OperationBO:" + site + "," + stepObj.getOperation() + "," + stepObj.getOperationVersion();
                return OperationBO;
            }
            if (stepObj.getStepType().equalsIgnoreCase("Routing") && stepObj.isEntryStep()) {
                List < RoutingStep > steplistRouting = stepObj.getRouterDetails().get(0).getRoutingStepList();
                for (RoutingStep routingobj: steplistRouting) {
                    OperationBO = "OperationBO:" + site + "," + routingobj.getOperation() + "," + routingobj.getOperationVersion();
                    return OperationBO;
                }
            }
        }
        return OperationBO;
    }
    public List < String > findOpenNcCode(List < NcData > primaryData) {
        List < String > ncCodeList = new ArrayList < > ();
        for (NcData obj: primaryData) {
            if (obj.getNcState().equalsIgnoreCase("Open")) {
                ncCodeList.add(obj.getNcCodeBo());
                System.out.println(obj.getNcCodeBo());
            }
            List < SecondaryNCData > secondaryList = obj.getSecondaryNCDataList();
            if (secondaryList != null) {
                for (SecondaryNCData secondaryData: secondaryList) {
                    if (secondaryData.getNcState().equalsIgnoreCase("Open")) {
                        ncCodeList.add(secondaryData.getNcCodeBo());
                        System.out.println(secondaryData.getNcCodeBo());
                    }
                }
            }
        }

        return ncCodeList;
    }
    @Override
    public List < DispositionRoutings > getDispositionRouting(List < NcData > ncRequest) {
        List < DispositionRoutings > dispositionRoutings = new ArrayList < DispositionRoutings > ();
        List < String > nclist = findOpenNcCode(ncRequest);
        String site = ncRequest.get(0).getSite();
        NCCodeRequest request = new NCCodeRequest();
        request.setSite(site);
        request.setNcCodeList(nclist);
        dispositionRoutings = webClientBuilder.build()
                .post()
                .uri(getDispositionRouteUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference < List < DispositionRoutings >> () {})
                .block();

        return dispositionRoutings;
    }

    private Boolean createDispositionLog(DispositionRequest dispositionRequest) {
        Boolean logged = false;
        DispositionLogRequest dispositionLogRequest = new DispositionLogRequest();
        dispositionLogRequest.setSite(dispositionRequest.getSite());

        dispositionLogRequest.setPcuBO(dispositionRequest.getPcuBO());
        dispositionLogRequest.setOperationBO(dispositionRequest.getOperationBO());
        dispositionLogRequest.setToOperationBo(tooperationBO);
        dispositionLogRequest.setResourceBo(dispositionRequest.getResourceBo());
        dispositionLogRequest.setWorkCenterBo(dispositionRequest.getWorkCenterBo());
        dispositionLogRequest.setUserBo(dispositionRequest.getUserBo());
        dispositionLogRequest.setRouterBo(dispositionRequest.getRouterBo());
        dispositionLogRequest.setDispositionRoutingBo(dispositionRequest.getDispositionRoutingBo());
        dispositionLogRequest.setQty(dispositionRequest.getQty());
        dispositionLogRequest.setItemBo(dispositionRequest.getItemBo());
        dispositionLogRequest.setActive(dispositionRequest.getActive());
        dispositionLogRequest.setShopOrderBo(dispositionRequest.getShoporderBO());
        com.rits.dispositionlogservice.model.MessageModel messagemodel = webClientBuilder.build()
                .post()
                .uri(dispositionlogurl)
                .bodyValue(dispositionLogRequest)
                .retrieve()
                .bodyToMono(com.rits.dispositionlogservice.model.MessageModel.class)
                .block();
        logged = true;
        return logged;
    }

    @Override
    public List<NcData> retrieveBySiteAndPcu(String site, String pcu) throws Exception
    {
        List<NcData> retrieveList = nonConformanceserviceRepository.findBySiteAndNcContextGbo(site,"PcuBO:"+site+","+pcu);
        return retrieveList;
    }



}