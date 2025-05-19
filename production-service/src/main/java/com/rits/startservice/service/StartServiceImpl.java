package com.rits.startservice.service;



import com.rits.Utility.BOConverter;
import com.rits.pcucompleteservice.model.PcuComplete;
import com.rits.pcuinqueueservice.exception.PcuInQueueException;
import com.rits.productionlogservice.dto.ProductionLogRequest;
import com.rits.productionlogservice.producer.ProductionLogProducer;
import com.rits.startservice.dto.*;
import com.rits.startservice.exception.StartException;
import com.rits.startservice.model.*;
import com.rits.startservice.repository.StartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StartServiceImpl implements StartService {
    private final StartRepository startRepository;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Value("${resource-service.url}/isExistByHandle")
    private String resourceUrl;

    @Value("${operation-service.url}/isExistByHandle")
    private String operationUrl;

//    @Value("${workcenter-service.url}/isExist")
//    private String workCenterUrl;

    @Value("${pcuinqueue-service.url}/retrieve")
    private String pcuInQueueRetrieveUrl;

    @Value("${pcuinqueue-service.url}/retrieveAllBySite")
    private String retrieveAllpcuInQueueBySiteUrl;

    @Value("${pcucomplete-service.url}/retrieve")
    private String pcuCompleteRetrieveUrl;

    @Value("${pcurouterheader-service.url}/retrieve")
    private String pcuRouterHeaderRetrieveUrl;

    @Value("${pcurouterheader-service.url}/getRoutingSubType")
    private String pcuRouterHeaderGetSubTypeUrl;

    @Value("${pcurouterheader-service.url}/getStepDetailsList")
    private String pcuRouterHeaderGetStepDetailsListUrl;

    @Value("${pcuinqueue-service.url}/delete")
    private String pcuInQueueDeleteUrl;

    @Value("${pcuinqueue-service.url}/create")
    private String pcuInQueueUpdateUrl;

    @Value("${routing-service.url}/retrieve")
    private String retrieveRoutingUrl;

    @Value("${productionlog-service.url}/producer")
    private String productionLogUrl;

    @Value("${operation-service.url}/retrieveOperationByCurrentVersion")
    private String retrieveOperationByCurrentVersionUrl;
    private String version;

    public StartRequestDetails boRemover(PcuInWork pcuInWork){

        StartRequestDetails requestWithoutBO = new StartRequestDetails();

        if(pcuInWork.getPcuBO()!=null && !pcuInWork.getPcuBO().isEmpty()) {
            requestWithoutBO.setPcu(BOConverter.getPcu(pcuInWork.getPcuBO()));
        }

        if(pcuInWork.getItemBO()!=null && !pcuInWork.getItemBO().isEmpty()) {
            requestWithoutBO.setItem(BOConverter.getItem(pcuInWork.getItemBO()));
            requestWithoutBO.setItemVersion(BOConverter.getItemVersion(pcuInWork.getItemBO()));
        }

        if(pcuInWork.getRouterBO()!=null && !pcuInWork.getRouterBO().isEmpty()) {
            requestWithoutBO.setRouter(BOConverter.getRouter(pcuInWork.getRouterBO()));
            requestWithoutBO.setRouterVersion(BOConverter.getRouterVersion(pcuInWork.getRouterBO()));
        }

        if(pcuInWork.getOperationBO()!=null && !pcuInWork.getOperationBO().isEmpty()) {
            requestWithoutBO.setOperation(BOConverter.getOperation(pcuInWork.getOperationBO()));
            requestWithoutBO.setOperationVersion(BOConverter.getOperationVersion(pcuInWork.getOperationBO()));
        }

        if(pcuInWork.getResourceBO()!=null && !pcuInWork.getResourceBO().isEmpty()) {
            requestWithoutBO.setResource(BOConverter.getResource(pcuInWork.getResourceBO()));
        }

        if(pcuInWork.getUserBO()!=null && !pcuInWork.getUserBO().isEmpty()) {
            requestWithoutBO.setUser(BOConverter.getUser(pcuInWork.getUserBO()));
        }

        if(pcuInWork.getShopOrderBO()!=null && !pcuInWork.getShopOrderBO().isEmpty()) {
            requestWithoutBO.setShopOrder(BOConverter.getShopOrder(pcuInWork.getShopOrderBO()));
        }

        if(pcuInWork.getChildRouterBO()!=null && !pcuInWork.getChildRouterBO().isEmpty()) {
            requestWithoutBO.setChildRouter(BOConverter.getChildRouter(pcuInWork.getChildRouterBO()));
            requestWithoutBO.setChildRouterVersion(BOConverter.getChildRouterVersion(pcuInWork.getChildRouterBO()));
        }

        return requestWithoutBO;
    }

    public StartRequest boCreator(StartRequestDetails startRequests) {

        StartRequest startRequest = new StartRequest();

        if(startRequests.getSite() != null && !startRequests.getSite().isEmpty()) {

            String siteVar = startRequests.getSite();

            if(startRequests.getUser() != null && !startRequests.getUser().isEmpty())
                startRequest.setUserBO(BOConverter.retrieveUserBO(siteVar, startRequests.getUser()));

            if(startRequests.getPcu() != null && !startRequests.getPcu().isEmpty())
                startRequest.setPcuBO(BOConverter.retrievePcuBO(siteVar, startRequests.getPcu()));

            if(startRequests.getItem() != null && !startRequests.getItem().isEmpty()) {
                startRequest.setItemBO(BOConverter.retrieveItemBO(siteVar, startRequests.getItem(), startRequests.getItemVersion()));
            }

            if(startRequests.getRouter() != null && !startRequests.getRouter().isEmpty()) {
                startRequest.setRouterBO(BOConverter.retrieveRouterBO(siteVar, startRequests.getRouter(), startRequests.getRouterVersion()));
            }

            if(startRequests.getOperation() != null && !startRequests.getOperation().isEmpty()) {
                startRequest.setOperationBO(BOConverter.retrieveOperationBO(siteVar, startRequests.getOperation(), startRequests.getOperationVersion()));
            }

            if(startRequests.getResource() != null && !startRequests.getResource().isEmpty())
                startRequest.setResourceBO(BOConverter.retriveResourceBO(siteVar, startRequests.getResource()));

            if(startRequests.getShopOrder() != null && !startRequests.getShopOrder().isEmpty())
                startRequest.setShopOrderBO(BOConverter.retrieveShopOrderBO(siteVar, startRequests.getShopOrder()));

            if(startRequests.getChildRouter() != null && !startRequests.getChildRouter().isEmpty())
                startRequest.setChildRouterBO(BOConverter.retrieveChildRouterBO(siteVar, startRequests.getChildRouter(), startRequests.getChildRouterVersion()));

        }

        return startRequest;
    }

    @Override
    public PcuInWorkMessageModel createPcuInWork(StartRequestDetails startRequest) throws Exception {


        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String operation = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());

        if (startRepository.existsByActiveAndSiteAndPcuBOAndOperationBO(1, startRequest.getSite(), pcuBO, operation)) {
            return updatePcuInWork(startRequest);
        }

        StartRequest bos = boCreator(startRequest);

        PcuInWork pcuInWork = PcuInWork.builder()
                .site(startRequest.getSite())
                .handle("PCUInWork:"+startRequest.getSite()+ "," +bos.getPcuBO()+","+ bos.getOperationBO()+","+bos.getShopOrderBO()+","+bos.getResourceBO())
                .pcuBO(bos.getPcuBO())
                .itemBO(bos.getItemBO())
                .resourceBO(bos.getResourceBO())
                .routerBO(bos.getRouterBO())
                .operationBO(bos.getOperationBO())
                .stepID(startRequest.getStepID())
                .userBO(bos.getUserBO())
                .workCenter(startRequest.getWorkCenter())
                .qtyInWork(startRequest.getQtyInWork())
                .qtyToComplete(startRequest.getQtyToComplete())
                .shopOrderBO(bos.getShopOrderBO())
                .childRouterBO(bos.getChildRouterBO())
                .parentStepID(startRequest.getParentStepID())
                .type("inwork")
                .createdDateTime(LocalDateTime.now())
                .active(1)
                .build();

        startRepository.save(pcuInWork);

        startRequest.setSite(pcuInWork.getSite());
        startRequest.setHandle(pcuInWork.getHandle());
        startRequest.setStepID(pcuInWork.getStepID());
        startRequest.setWorkCenter(pcuInWork.getWorkCenter());
        startRequest.setQtyInWork(pcuInWork.getQtyInWork());
        startRequest.setQtyToComplete(pcuInWork.getQtyToComplete());
        startRequest.setParentStepID(pcuInWork.getParentStepID());
        startRequest.setCreatedDateTime(pcuInWork.getCreatedDateTime());
        startRequest.setActive(pcuInWork.getActive());

        bos = null;
        pcuInWork = null;
        return PcuInWorkMessageModel.builder().message_details(new MessageDetails(startRequest.getPcu() + "Created SuccessFully", "S")).response(startRequest).build();

    }



    @Override
    public PcuInWorkMessageModel updatePcuInWork(StartRequestDetails startRequest) throws Exception {

        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String operation = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());

        if (startRepository.existsByActiveAndSiteAndPcuBOAndOperationBO(1, startRequest.getSite(), pcuBO, operation)) {
            PcuInWork existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBOAndOperationBO(1, startRequest.getSite(), pcuBO, operation);

            StartRequest bos = boCreator(startRequest);

            PcuInWork pcuInWork = PcuInWork.builder()
                    .site(existingPcuInWork.getSite())
                    .handle(existingPcuInWork.getHandle())
                    .pcuBO(bos.getPcuBO())
                    .itemBO(bos.getItemBO())
                    .resourceBO(bos.getResourceBO())
                    .routerBO(bos.getRouterBO())
                    .operationBO(bos.getOperationBO())
                    .stepID(existingPcuInWork.getStepID())
                    .userBO(bos.getUserBO())
                    .workCenter(startRequest.getWorkCenter())
                    .qtyInWork(startRequest.getQtyInWork())
                    .qtyToComplete(startRequest.getQtyToComplete())
                    .shopOrderBO(bos.getShopOrderBO())
                    .childRouterBO(bos.getChildRouterBO())
                    .parentStepID(existingPcuInWork.getParentStepID())
                    .type("inwork")
                    .createdDateTime(existingPcuInWork.getCreatedDateTime())
                    .modifiedDateTime(LocalDateTime.now())
                    .active(1)
                    .build();

            startRepository.save(pcuInWork);

            startRequest.setSite(pcuInWork.getSite());
            startRequest.setHandle(pcuInWork.getHandle());
            startRequest.setStepID(pcuInWork.getStepID());
            startRequest.setWorkCenter(pcuInWork.getWorkCenter());
            startRequest.setQtyInWork(pcuInWork.getQtyInWork());
            startRequest.setParentStepID(pcuInWork.getParentStepID());
            startRequest.setCreatedDateTime(pcuInWork.getCreatedDateTime());
            startRequest.setModifiedDateTime(pcuInWork.getModifiedDateTime());
            startRequest.setActive(pcuInWork.getActive());

            existingPcuInWork = null;
            bos = null;
            pcuInWork = null;

            return PcuInWorkMessageModel.builder().message_details(new MessageDetails(startRequest.getPcu() + " updated SuccessFully", "S")).response(startRequest).build();

        } else {
            return createPcuInWork(startRequest);
        }
    }

    @Override
    public List<StartRequestDetails> retrieveByPcuAndSite(StartRequestDetails startRequest) throws Exception {

        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());

        List<PcuInWork> existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBO(1, startRequest.getSite(), pcuBO);
        List<StartRequestDetails> withoutBOList = new ArrayList<>();

        if(existingPcuInWork != null) {
            StartRequestDetails requestWithoutBO = null;

            for(PcuInWork pcuInWork : existingPcuInWork) {
                requestWithoutBO = new StartRequestDetails();

                requestWithoutBO = boRemover(pcuInWork);
                requestWithoutBO.setSite(pcuInWork.getSite());
                requestWithoutBO.setHandle(pcuInWork.getHandle());
                requestWithoutBO.setStepID(pcuInWork.getStepID());
                requestWithoutBO.setQtyInWork(pcuInWork.getQtyInWork());
                requestWithoutBO.setWorkCenter(pcuInWork.getWorkCenter());
                requestWithoutBO.setQtyToComplete(pcuInWork.getQtyToComplete());
                requestWithoutBO.setParentStepID(pcuInWork.getParentStepID());
                requestWithoutBO.setActive(pcuInWork.getActive());
                requestWithoutBO.setCreatedDateTime(pcuInWork.getCreatedDateTime());
                requestWithoutBO.setModifiedDateTime(pcuInWork.getModifiedDateTime());

                withoutBOList.add(requestWithoutBO);

                requestWithoutBO = null;
            }
        }

        existingPcuInWork = null;
        return withoutBOList;
    }

    @Override
    public List<StartRequestDetails> retrieveDeletedPcu(StartRequestDetails startRequest) throws Exception {
        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());

        List<PcuInWork> existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBO(0, startRequest.getSite(), pcuBO);
        List<StartRequestDetails> pcuInWorkWithoutBOList = new ArrayList<>();

        if(existingPcuInWork != null) {
            StartRequestDetails pcuInWorkWithoutBO = null;

            for(PcuInWork pcuInWork : existingPcuInWork) {
                pcuInWorkWithoutBO = new StartRequestDetails();
                pcuInWorkWithoutBO = boRemover(pcuInWork);

                pcuInWorkWithoutBO.setSite(pcuInWork.getSite());
                pcuInWorkWithoutBO.setHandle(pcuInWork.getHandle());
                pcuInWorkWithoutBO.setStepID(pcuInWork.getStepID());
                pcuInWorkWithoutBO.setQtyInWork(pcuInWork.getQtyInWork());
                pcuInWorkWithoutBO.setWorkCenter(pcuInWork.getWorkCenter());
                pcuInWorkWithoutBO.setQtyToComplete(pcuInWork.getQtyToComplete());
                pcuInWorkWithoutBO.setParentStepID(pcuInWork.getParentStepID());
                pcuInWorkWithoutBO.setActive(pcuInWork.getActive());
                pcuInWorkWithoutBO.setCreatedDateTime(pcuInWork.getCreatedDateTime());
                pcuInWorkWithoutBO.setModifiedDateTime(pcuInWork.getModifiedDateTime());

                pcuInWorkWithoutBOList.add(pcuInWorkWithoutBO);

                pcuInWorkWithoutBO = null;
            }
        }

        existingPcuInWork = null;
        return pcuInWorkWithoutBOList;
    }

    @Override
    public Boolean deletePcuFromAllOperations(StartRequestDetails startRequest) {
        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        Boolean isPcuDeletedFromAllOperations = false;

        List<PcuInWork> retrievedList = startRepository.findByActiveAndSiteAndPcuBO(1,startRequest.getSite(),pcuBO);

        if(retrievedList!=null && !retrievedList.isEmpty()) {

            for(PcuInWork pcuInWork : retrievedList){
                pcuInWork.setActive(0);
                startRepository.save(pcuInWork);
                isPcuDeletedFromAllOperations = true;

            }
        }

        retrievedList = null;
        return isPcuDeletedFromAllOperations;
    }

    @Override
    public Boolean unDeletePcuFromAllOperations(StartRequestDetails startRequest) {
        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        Boolean isPcuDeletedFromAllOperations = false;

        List<PcuInWork> retrievedList = startRepository.findByActiveAndSiteAndPcuBO(0,startRequest.getSite(),pcuBO);

        if(retrievedList!=null && !retrievedList.isEmpty()) {

            for(PcuInWork pcuInWork : retrievedList) {
                pcuInWork.setActive(1);
                startRepository.save(pcuInWork);
                isPcuDeletedFromAllOperations = true;

            }
        }

        retrievedList = null;
        return isPcuDeletedFromAllOperations;
    }



    @Override
    public StartRequestDetails retrievePcuInWorkByOperationAndItem(StartRequestDetails startRequest) throws Exception {
        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String operationBO = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());
        String itemBO = BOConverter.retrieveItemBO(startRequest.getSite(), startRequest.getItem(), startRequest.getItemVersion());

        PcuInWork existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndItemBO(1, startRequest.getSite(), pcuBO, operationBO, itemBO);
        if (existingPcuInWork == null) {
            throw new StartException(3603, startRequest.getPcu(), startRequest.getOperation(), startRequest.getItem());
        }
        StartRequestDetails pcu1 = boRemover(existingPcuInWork);
        pcu1.setSite(existingPcuInWork.getSite());
        pcu1.setHandle(existingPcuInWork.getHandle());
        pcu1.setStepID(existingPcuInWork.getStepID());
        pcu1.setQtyInWork(existingPcuInWork.getQtyInWork());
        pcu1.setWorkCenter(existingPcuInWork.getWorkCenter());
        pcu1.setQtyToComplete(existingPcuInWork.getQtyToComplete());
        pcu1.setParentStepID(existingPcuInWork.getParentStepID());
        pcu1.setActive(existingPcuInWork.getActive());
        pcu1.setCreatedDateTime(existingPcuInWork.getCreatedDateTime());
        pcu1.setModifiedDateTime(existingPcuInWork.getModifiedDateTime());

        existingPcuInWork = null;
        return pcu1;
    }



    @Override
    public StartRequestDetails retrievePcuInWorkByOperation(StartRequestDetails startRequest) throws Exception {

        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String operation = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());

        PcuInWork existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBOAndOperationBOContaining(1, startRequest.getSite(), pcuBO, operation);
        if (existingPcuInWork == null) {
            throw new StartException(3605, startRequest.getPcu(), startRequest.getOperation());
        }

        StartRequestDetails requestWithoutBO = boRemover(existingPcuInWork);

        requestWithoutBO.setSite(existingPcuInWork.getSite());
        requestWithoutBO.setHandle(existingPcuInWork.getHandle());
        requestWithoutBO.setQtyInWork(existingPcuInWork.getQtyInWork());
        requestWithoutBO.setStepID(existingPcuInWork.getStepID());
        requestWithoutBO.setWorkCenter(existingPcuInWork.getWorkCenter());
        requestWithoutBO.setQtyToComplete(existingPcuInWork.getQtyToComplete());
        requestWithoutBO.setParentStepID(existingPcuInWork.getParentStepID());
        requestWithoutBO.setActive(existingPcuInWork.getActive());
        requestWithoutBO.setCreatedDateTime(existingPcuInWork.getCreatedDateTime());
        requestWithoutBO.setModifiedDateTime(existingPcuInWork.getModifiedDateTime());

        existingPcuInWork = null;
        return requestWithoutBO;
    }

    @Override
    public StartRequestDetails retrievePcuInWorkByOperationAndResource(StartRequestDetails startRequest) throws Exception {
        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String operationBO = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());
        String resourceBO = BOConverter.retriveResourceBO(startRequest.getSite(), startRequest.getResource());

        PcuInWork existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndResourceBO(1, startRequest.getSite(), pcuBO, operationBO, resourceBO);
        if (existingPcuInWork == null) {
            throw new StartException(3604, startRequest.getPcu(), startRequest.getOperation(), startRequest.getResource());
        }

        StartRequestDetails pcu1 = boRemover(existingPcuInWork);
        pcu1.setSite(existingPcuInWork.getSite());
        pcu1.setHandle(existingPcuInWork.getHandle());
        pcu1.setStepID(existingPcuInWork.getStepID());
        pcu1.setQtyInWork(existingPcuInWork.getQtyInWork());
        pcu1.setWorkCenter(existingPcuInWork.getWorkCenter());
        pcu1.setQtyToComplete(existingPcuInWork.getQtyToComplete());
        pcu1.setParentStepID(existingPcuInWork.getParentStepID());
        pcu1.setActive(existingPcuInWork.getActive());
        pcu1.setCreatedDateTime(existingPcuInWork.getCreatedDateTime());
        pcu1.setModifiedDateTime(existingPcuInWork.getModifiedDateTime());

        existingPcuInWork = null;
        return pcu1;

    }
    @Override
    public MessageModel deletePcuInWork(StartRequestDetails startRequest) throws Exception {
        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String operationBO = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());

        PcuInWork existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBOAndOperationBO(1, startRequest.getSite(), pcuBO, operationBO);

        if (existingPcuInWork.getPcuBO() != null) {
            startRepository.delete(existingPcuInWork);
            existingPcuInWork = null;
            return MessageModel.builder().message_details(new MessageDetails("Deleted " + startRequest.getPcu(),"S")).build();
        }

        throw new StartException(2915, startRequest.getPcu());
    }



    @Override
    public List<PcuInWork> retrieveByOperationAndResource(StartRequest startRequest) throws Exception {
        List<PcuInWork> retrievedRecord;
        if(startRequest.getResourceBO()==null&&startRequest.getResourceBO().isEmpty()) {
            retrievedRecord = startRepository.findByActiveAndSiteAndOperationBOAndResourceBO(1, startRequest.getSite(), startRequest.getOperationBO(), startRequest.getResourceBO());
        }else{
            retrievedRecord=startRepository.findByActiveAndSiteAndOperationBO(1,startRequest.getSite(),startRequest.getOperationBO(), getPagable(startRequest.getRecordLimit()));
        }
        return retrievedRecord;
    }



    @Override
    public PcuList retrieveAll(StartRequestDetails startRequest) throws Exception {

        List<PcuInWork> pcuInWorklist = startRepository.findByActiveAndSite(1,startRequest.getSite());
        List<StartRequestDetails> pcuInWorkWithoutBOList = new ArrayList<>();

        if(pcuInWorklist != null) {
            StartRequestDetails requestWithoutBO = null;

            for(PcuInWork pcuInWork : pcuInWorklist) {
                requestWithoutBO = new StartRequestDetails();
                requestWithoutBO = boRemover(pcuInWork);

//                requestWithoutBO.setOperation(pcu1.getOperation());
//                requestWithoutBO.setItem(pcu1.getItem());
//                requestWithoutBO.setUser(pcu1.getUser());
//                requestWithoutBO.setPcu(pcu1.getPcu());
//                requestWithoutBO.setResource(pcu1.getResource());
//                requestWithoutBO.setChildRouter(pcu1.getChildRouter());
//                requestWithoutBO.setRouter(pcu1.getRouter());
//                requestWithoutBO.setShopOrder(pcu1.getShopOrder());
                requestWithoutBO.setSite(pcuInWork.getSite());
                requestWithoutBO.setHandle(pcuInWork.getHandle());
                requestWithoutBO.setStepID(pcuInWork.getStepID());
                requestWithoutBO.setQtyInWork(pcuInWork.getQtyInWork());
                requestWithoutBO.setWorkCenter(pcuInWork.getWorkCenter());
                requestWithoutBO.setQtyToComplete(pcuInWork.getQtyToComplete());
                requestWithoutBO.setParentStepID(pcuInWork.getParentStepID());
                requestWithoutBO.setActive(pcuInWork.getActive());

                pcuInWorkWithoutBOList.add(requestWithoutBO);
                requestWithoutBO = null;
            }
            pcuInWorklist = null;
        }
        return PcuList.builder().pcuList(pcuInWorkWithoutBOList).build();
    }





    @Override
    public boolean pcuStart(StartRequestDetails startRequest) throws Exception {
        StartRequestDetails retrievePcuInQueueRequest = StartRequestDetails.builder()
                .site(startRequest.getSite())
                .pcu(startRequest.getPcu())
                .operation(startRequest.getOperation()).operationVersion(startRequest.getOperationVersion())
                .build();

        PcuInQueue retrievePcuInQueue = webClientBuilder.build()
                .post()
                .uri(pcuInQueueRetrieveUrl)
                .bodyValue(retrievePcuInQueueRequest)
                .retrieve()
                .bodyToMono(PcuInQueue.class)
                .block();
        if(retrievePcuInQueue==null || retrievePcuInQueue.getPcu()==null || retrievePcuInQueue.getPcu().isEmpty())
            return false;

        if(retrievePcuInQueue.getPcu() == null)
            throw new StartException(2917,startRequest.getPcu());

        if (startRequest.getQuantity().equals(" "))
            throw new StartException(2910);

        if (startRequest.getQuantity().isEmpty())
            startRequest.setQuantity("0");

//        String operationArray[]=startRequest.getOperation().split("/");
//        String operation=operationArray[0];
//        String version= operationArray[1];

//        List<StartRequestWithoutBO> inQueueReq  = new ArrayList<>();
//        inQueueReq.add(startRequest);
//        StartRequest requestList = boCreator(startRequest);
        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String operationBO = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());

        if (Double.parseDouble(startRequest.getQuantity()) > Double.parseDouble(retrievePcuInQueue.getQtyInQueue())) {
            throw new StartException(2900, startRequest.getQuantity());

        } else if ((startRequest.getQuantity().equals("0") && Double.parseDouble(retrievePcuInQueue.getQtyInQueue()) > 0) || startRequest.getQuantity().equals(retrievePcuInQueue.getQtyInQueue())) {

            PcuInQueueReq deletePcuInQueueRequest = PcuInQueueReq.builder().site(startRequest.getSite()).pcu(startRequest.getPcu()).operation(startRequest.getOperation()).operationVersion(startRequest.getOperationVersion()).resource(startRequest.getResource()).shopOrder(startRequest.getShopOrder()).build();
            Boolean deletePcuInQueue = webClientBuilder.build()
                    .post()
                    .uri(pcuInQueueDeleteUrl)
                    .bodyValue(deletePcuInQueueRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if(!deletePcuInQueue) {
                throw new StartException(3606);
            }

            startRequest.setQtyInWork(retrievePcuInQueue.getQtyInQueue());
            startRequest.setQuantity(retrievePcuInQueue.getQtyInQueue());


            PcuInWork existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBOAndOperationBO(1, startRequest.getSite(), pcuBO, operationBO);

            if (existingPcuInWork != null)
                startRequest.setQtyInWork(String.valueOf( Double.parseDouble(existingPcuInWork.getQtyInWork()) +  Double.parseDouble(startRequest.getQuantity())));
            else
                startRequest.setQtyInWork(startRequest.getQuantity());

//            StartRequest startBuilderRes = buildStartRequest(startRequest,requestList);
            createPcuInWork(startRequest);
            return true;

        } else if (Double.parseDouble(startRequest.getQuantity()) < Double.parseDouble(retrievePcuInQueue.getQtyInQueue())) { // check

            double updatedPcuInQueueQuantity = Double.parseDouble(retrievePcuInQueue.getQtyInQueue()) - Double.parseDouble(startRequest.getQuantity());

            PcuInWork existingPcuInWork = startRepository.findByActiveAndSiteAndPcuBOAndOperationBO(1, startRequest.getSite(), pcuBO, operationBO);

            if (existingPcuInWork != null)
                startRequest.setQtyInWork(String.valueOf( Double.parseDouble(existingPcuInWork.getQtyInWork()) +  Double.parseDouble(startRequest.getQuantity())));
            else
                startRequest.setQtyInWork(startRequest.getQuantity());

//            StartRequest startBuilderRes = buildStartRequest(startRequest,requestList);
            PcuInWorkMessageModel create = createPcuInWork(startRequest);

            if (create.getMessage_details().getMsg_type().equals("S")) {
                StartRequestDetails updatePcuInQueueReq = StartRequestDetails.builder()
                        .site(startRequest.getSite())
                        .pcu(startRequest.getPcu())
                        .operation(startRequest.getOperation())
                        .qtyInQueue(String.valueOf(updatedPcuInQueueQuantity))
                        .item(startRequest.getItem())
                        .resource(startRequest.getResource())
                        .router(startRequest.getRouter())
                        .stepID(retrievePcuInQueue.getStepID())
                        .user(startRequest.getUser())
                        .workCenter(startRequest.getWorkCenter())
                        .qtyToComplete(retrievePcuInQueue.getQtyToComplete())
                        .shopOrder(startRequest.getShopOrder())
                        .childRouter(startRequest.getChildRouter())
                        .parentStepID(retrievePcuInQueue.getParentStepID())
                        .build();

                PcuInQueueMessageModel updatePcuInQueue = webClientBuilder.build()
                        .post()
                        .uri(pcuInQueueUpdateUrl)
                        .bodyValue(updatePcuInQueueReq)
                        .retrieve()
                        .bodyToMono(PcuInQueueMessageModel.class)
                        .block();

                if (updatePcuInQueue.getResponse() !=null && updatePcuInQueue.getResponse().getPcu() != null) {
                    return true;
                }
            } else {
                throw new StartException(2913);
            }
        }

        throw new StartException(2904);
    }

    public StartRequest buildStartRequest(StartRequestDetails requestWithoutBO, StartRequest startReq){

        StartRequest startRequest = StartRequest.builder()
                .pcuBO(startReq.getPcuBO())
                .operationBO(startReq.getOperationBO())
                .shopOrderBO(startReq.getShopOrderBO())
                .resourceBO(startReq.getResourceBO())
                .itemBO(startReq.getItemBO())
                .routerBO(startReq.getRouterBO())
                .userBO(startReq.getUserBO())
                .childRouterBO(startReq.getChildRouterBO())
                .stepID(requestWithoutBO.getStepID())
                .site(requestWithoutBO.getSite())
                .workCenter(requestWithoutBO.getWorkCenter())
                .qtyInWork(requestWithoutBO.getQtyInWork())
                .qtyToComplete(requestWithoutBO.getQtyToComplete())
                .parentStepID(requestWithoutBO.getParentStepID())
                .build();

        return startRequest;
    }

    @Override
    public List<RoutingStep> retrieveStepDetails(StartRequestDetails startRequest) {
        String routing=null;
        String routingVersion=null;

        if(startRequest.getChildRouter()==null||startRequest.getChildRouter().isEmpty()||startRequest.getChildRouter()=="") {
//            String[] routingBO = startRequest.getRouter().split("/");
            routing= startRequest.getRouter();
            routingVersion = startRequest.getRouterVersion();
        }else{
//            String[] routingBO = startRequest.getChildRouter().split("/");
            routing= startRequest.getChildRouter();
            routingVersion = startRequest.getChildRouterVersion();

        }
//        String operationBO[]=startRequest.getOperation().split("/");
//        String operation=operationBO[0];

        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String operationBO = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());

        PcuRouterHeaderRequest pcuRouterHeaderGetStepDetailList = PcuRouterHeaderRequest.builder().site(startRequest.getSite()).router(routing).version(routingVersion).operation(operationBO).pcuBo(pcuBO).build();
        List<RoutingStep> retrievePcuRouterHeaderStepDetailList = webClientBuilder.build()
                .post()
                .uri(pcuRouterHeaderGetStepDetailsListUrl)
                .bodyValue(pcuRouterHeaderGetStepDetailList)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RoutingStep>>() {
                })
                .block();
        return retrievePcuRouterHeaderStepDetailList;
    }

    public List<PcuInWork> retrieveByOperation(String site, String operation) throws Exception
    {
        return startRepository.findByActiveAndSiteAndOperationBO(1,site,operation, getPagable(0));
    }


    @Override
    public RoutingStep getRoutingStep(StartRequestDetails startRequest) throws Exception {
        List<RoutingStep> stepList = retrieveStepDetails(startRequest);
        RoutingStep routingStep = null;
        for (RoutingStep step : stepList) {
            if (startRequest.getStepID().equals(step.getStepId())) {
                routingStep = step;
                break;
            }
        }
        return routingStep;
    }



    @Override
    public MessageModel start(StartRequestLists startRequestList) throws Exception {

        List<StartRequestDetails> requestList = startRequestList.getRequestList();
        List<MessageDetails> messageDetailsList = new ArrayList<>();
        MessageDetails messageDetails=new MessageDetails();
        List<String> errorPcus = new ArrayList<>();
        List<String> successPcus = new ArrayList<>();
        String errorMessage = null, successMessage = null;

        if (!requestList.isEmpty()) {
            for (StartRequestDetails startRequest : requestList) {

                startRequest.setOperationVersion(getOperationCurrentVer(startRequest));
//                PcuInQueueRequest pcuInQueueRequest = PcuInQueueRequest.builder().site(startRequest.getSite()).pcuBO(startRequest.getPcuBO()).operationBO(startRequest.getOperationBO()).build();
                StartRequestDetails pcuInQueueRequest = StartRequestDetails.builder().site(startRequest.getSite()).pcu(startRequest.getPcu()).operation(startRequest.getOperation()).operationVersion(startRequest.getOperationVersion()).build();
                PcuInQueue retrievedPcuInQueue = webClientBuilder.build()
                        .post()
                        .uri(pcuInQueueRetrieveUrl)
                        .bodyValue(pcuInQueueRequest)
                        .retrieve()
                        .bodyToMono(PcuInQueue.class)
                        .block();
                if(retrievedPcuInQueue==null || retrievedPcuInQueue.getPcu()==null || retrievedPcuInQueue.getPcu().isEmpty())
                {
                    errorPcus.add(startRequest.getPcu());
                    continue;
                }

                ResourceRequest resourceRequest = new ResourceRequest(startRequest.getSite(), BOConverter.retriveResourceBO(startRequest.getSite(), startRequest.getResource()));
                Boolean isResourceExist = webClientBuilder.build()
                        .post()
                        .uri(resourceUrl)
                        .bodyValue(resourceRequest)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();

//                String operationArray[]=startRequest.getOperation().split("/");
//                String operation=operationArray[0];
//                String version=operationArray[1];
//
//                String operationBO = "OperationBO:" + startRequest.getSite() + "," + operation + "," + version;

                Operation operationRequest = new Operation(BOConverter.retrieveOperationBO(startRequest.getSite(),startRequest.getOperation(), startRequest.getOperationVersion()), startRequest.getSite());
                Boolean isOperationExist = webClientBuilder.build()
                        .post()
                        .uri(operationUrl)
                        .bodyValue(operationRequest)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();



//                RetrieveRequest workCenterRequest = new RetrieveRequest(startRequest.getSite(), startRequest.getWorkCenter());
//                Boolean isWorkCenterExist = webClientBuilder.build()
//                        .post()
//                        .uri(workCenterUrl)
//                        .bodyValue(workCenterRequest)
//                        .retrieve()
//                        .bodyToMono(Boolean.class)
//                        .block();

                String routing=null;
                String routingVersion=null;

                if(startRequest.getChildRouter()==null||startRequest.getChildRouter().isEmpty()) {
//                    String[] routingBO = startRequest.getRouter().split("/");
                    routing= startRequest.getRouter();
                    routingVersion = startRequest.getRouterVersion();

                }else{
//                    String[] routingBO = startRequest.getChildRouter().split("/");
                    routing= startRequest.getChildRouter();
                    routingVersion = startRequest.getChildRouterVersion();

                }

                RoutingRequest routingRequest = RoutingRequest.builder().site(startRequest.getSite()).routing(routing).version(routingVersion).build();
                Routing retrievedRouting = webClientBuilder.build()
                        .post()
                        .uri(retrieveRoutingUrl)
                        .bodyValue(routingRequest)
                        .retrieve()
                        .bodyToMono(Routing.class)
                        .block();

                if (isOperationExist && isResourceExist /*&& isWorkCenterExist*/) {
                    if (retrievedRouting.getStatus().equalsIgnoreCase("releasable")) {

                        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
                        PcuRouterHeaderRequest retrievePcuRouterHeaderRequest = PcuRouterHeaderRequest.builder().site(startRequest.getSite()).pcuBo(pcuBO).build();
                        PcuRouterHeader retrievePcuRouterHeader = webClientBuilder.build()
                                .post()
                                .uri(pcuRouterHeaderRetrieveUrl)
                                .bodyValue(retrievePcuRouterHeaderRequest)
                                .retrieve()
                                .bodyToMono(PcuRouterHeader.class)
                                .block();

                        if (retrievePcuRouterHeader != null) {
                            PcuRouterHeaderRequest subTypePcuRouterHeaderRequest = PcuRouterHeaderRequest.builder().site(startRequest.getSite()).router(routing).version(routingVersion).build();
                            String retrievePcuRouterHeaderSubType = webClientBuilder.build()
                                    .post()
                                    .uri(pcuRouterHeaderGetSubTypeUrl)
                                    .bodyValue(subTypePcuRouterHeaderRequest)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            StartRequest requestList1 = boCreator(startRequest);

                            StartRequest startBuilderRes = buildStartRequest(startRequest,requestList1);

                            List<RoutingStep> retrievePcuRouterHeaderStepDetailList = retrieveStepDetails(startRequest);
                            if (retrievePcuRouterHeaderSubType.equalsIgnoreCase("sequential") || retrievePcuRouterHeaderSubType.equalsIgnoreCase("parallel")) {
                                boolean start = pcuStart(startRequest);
                                if(start)
                                {
                                    successPcus.add(startRequest.getPcu());
                                    Boolean productionLogged = productionLog(startRequest);
                                }else{//this condition not checked
                                    errorPcus.add(startRequest.getPcu());
                                    continue;
                                }
                            } else if (retrievePcuRouterHeaderSubType.equalsIgnoreCase("simultaneous")) {// check
                                if (retrievePcuRouterHeaderStepDetailList == null || retrievePcuRouterHeaderStepDetailList.isEmpty()) {
                                    throw new StartException(2906, retrievePcuRouterHeaderStepDetailList);
                                }
                                for (RoutingStep routingSteps : retrievePcuRouterHeaderStepDetailList) {

                                    if (routingSteps.getOperation().equals(startRequest.getOperation())&&routingSteps.getOperationVersion().equals(startRequest.getOperationVersion())) {
                                        if (routingSteps.getPreviousStepId().equals("00")) {
                                            boolean start = pcuStart(startRequest);
                                            if(start)
                                            {
                                                successPcus.add(startRequest.getPcu());
                                                Boolean productionLogged = productionLog(startRequest);
                                            }else{
                                                errorPcus.add(startRequest.getPcu());
                                                continue;
                                            }
                                        } else {
                                            String[] previousStepIds = routingSteps.getPreviousStepId().split(",");

                                            for (String previousStepId : previousStepIds) {
//                                                StartRequestWithoutBO request= new StartRequestWithoutBO(startRequest);
                                                startRequest.setStepID(previousStepId);
                                                RoutingStep routingStep = getRoutingStep(startRequest);

                                                StartRequestDetails retrievePcuInQueueRequest = StartRequestDetails.builder().site(startRequest.getSite()).pcu(startRequest.getPcu()).operation(startRequest.getOperation()).operationVersion(startRequest.getOperationVersion()).build();
                                                PcuInQueue retrievePcuInQueue = webClientBuilder.build()
                                                        .post()
                                                        .uri(pcuInQueueRetrieveUrl)
                                                        .bodyValue(retrievePcuInQueueRequest)
                                                        .retrieve()
                                                        .bodyToMono(PcuInQueue.class)
                                                        .block();
                                                PcuComplete retrievePcuComplete = webClientBuilder.build()
                                                        .post()
                                                        .uri(pcuCompleteRetrieveUrl)
                                                        .bodyValue(retrievePcuInQueueRequest)
                                                        .retrieve()
                                                        .bodyToMono(PcuComplete.class)
                                                        .block();

                                                String setOperation="OperationBO:"+resourceRequest.getSite()+","+routingStep.getOperation()+","+routingStep.getOperationVersion();
                                                boolean isExist = startRepository.existsByActiveAndSiteAndPcuBOAndOperationBO(1, startRequest.getSite(), pcuBO, setOperation);

                                                if (isExist || retrievePcuInQueue.getPcu() != null) {
                                                    throw new StartException(2912, routingStep.getOperation());
                                                }
                                                if(retrievePcuComplete==null){
                                                    throw new StartException(2918,routingStep.getOperation());
                                                }
                                            }
                                            boolean start = pcuStart(startRequest);
                                            if(start)
                                            {
                                                successPcus.add(startRequest.getPcu());
                                                Boolean productionLogged = productionLog(startRequest);
                                            }else{
                                                errorPcus.add(startRequest.getPcu());
                                                continue;
                                            }
                                        }
                                    }
                                }
                            } else if (retrievePcuRouterHeaderSubType.equalsIgnoreCase("anyorder")) { //check
                                if (retrievePcuRouterHeaderStepDetailList == null || retrievePcuRouterHeaderStepDetailList.isEmpty()) {
                                    throw new StartException(2906, retrievePcuRouterHeaderStepDetailList);
                                }
                                for (RoutingStep routingSteps : retrievePcuRouterHeaderStepDetailList) {
                                    //split it and use
                                    if (routingSteps.getOperation().equals(startRequest.getOperation())&& routingSteps.getOperationVersion().equals(startRequest.getOperationVersion())) {
                                        boolean start = pcuStart(startRequest);
                                        if(start)
                                        {
                                            successPcus.add(startRequest.getPcu());
                                            Boolean productionLogged = productionLog(startRequest);
                                        }else {
                                            errorPcus.add(startRequest.getPcu());
                                            continue;
                                        }
                                    } else {

                                        StartRequestDetails retrievePcuInQueueRequest =  StartRequestDetails.builder().operation(routingSteps.getOperation()).operationVersion(routingSteps.getOperationVersion()).site(startRequest.getSite()).pcu(startRequest.getPcu()).build();
                                        PcuInQueue retrievePcuInQueue = webClientBuilder.build()
                                                .post()
                                                .uri(pcuInQueueRetrieveUrl)
                                                .bodyValue(retrievePcuInQueueRequest)
                                                .retrieve()
                                                .bodyToMono(PcuInQueue.class)
                                                .block();
                                        if (retrievePcuInQueue.getHandle() != null) {
                                            StartRequestDetails deletePcuInQueueRequest = StartRequestDetails.builder().resource(startRequest.getResource()).shopOrder(startRequest.getShopOrder()).operation(routingSteps.getOperation()).operationVersion(routingSteps.getOperationVersion()).site(startRequest.getSite()).pcu(startRequest.getPcu()).build();
                                            Boolean deletePcuInQueue = webClientBuilder.build()
                                                    .post()
                                                    .uri(pcuInQueueDeleteUrl)
                                                    .bodyValue(deletePcuInQueueRequest)
                                                    .retrieve()
                                                    .bodyToMono(Boolean.class)
                                                    .block();
                                            if(!deletePcuInQueue)
                                            {
                                                throw new StartException(3606);
                                            }
                                        }
                                    }
                                }
                            } else {  throw new StartException(2905, startRequest.getPcu()); }
                        } else { throw new StartException(2901, startRequest.getPcu()); }
                    } else { throw new StartException(2914, routing, routingVersion); }
                } else { throw new StartException(2908, startRequest.getWorkCenter()); }
            }
            if (!errorPcus.isEmpty()) {
                List<String> splitErrorPcus = errorPcus.stream()
                        .filter(str -> str != null)  // Filter out null values
                        .flatMap(str -> {
                            String[] parts = str.split(",");
                            return parts.length > 1 ? Arrays.stream(parts).skip(1) : Arrays.stream(parts);
                        })
                        .collect(Collectors.toList());

                errorMessage = "Failed to Start Pcu :" + String.join(",", splitErrorPcus);
            }

            if(!successPcus.isEmpty()) {
                List<String> splitSuccessPcus = successPcus.stream()
                        .filter(str -> str != null)
                        .flatMap(str -> {
                            String[] parts = str.split(",");
                            return parts.length > 1 ? Arrays.stream(parts).skip(1) : Arrays.stream(parts);
                        })
                        .collect(Collectors.toList());
                successMessage = "Start successfully Pcu :" + String.join(",", splitSuccessPcus);
            }
            if(errorMessage!=null && successMessage == null) {
                messageDetails = MessageDetails.builder().msg(errorMessage).msg_type("E").build();
            }
            if(successMessage != null)
            {
                messageDetails = MessageDetails.builder().msg(successMessage).msg_type("S").build();
            }
            return MessageModel.builder().message_details(messageDetails).build();
        }
        throw new StartException(2916, requestList);
    }

    private Pageable getPagable(int maxRecords){
        if (maxRecords > 0) {
            return PageRequest.of(0, maxRecords, Sort.by(Sort.Direction.DESC, "createdDateTime"));
        } else {
            return Pageable.unpaged(); // Retrieve all records
        }
    }

    @Override
    public List<StartRequestDetails> retrieveListByOperationAndResource(StartRequestDetails startRequest) throws Exception {
        List<StartRequestDetails> pcuInWorkWithoutBOList = new ArrayList<>();
        if(startRequest.getOperation() != null) {

            String operation = "";
            List<PcuInWork> pcuInWorkList = startRepository.findByActiveAndSiteAndPcuBO(1, startRequest.getSite(), startRequest.getPcu());
            for(PcuInWork pcuInWork : pcuInWorkList){
                if(startRequest.getOperation().equals(BOConverter.getOperation(pcuInWork.getOperationBO()))){
                    operation = pcuInWork.getOperationBO();
                }
            }

            if(operation.isEmpty()){
                operation = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());
            }

            List<PcuInWork> pcuInWorklist = startRepository.findByActiveAndSiteAndOperationBO(1, startRequest.getSite(), operation, getPagable(startRequest.getRecordLimit()));

            if (pcuInWorklist != null) {
                StartRequestDetails pcuInWorkWithoutBO = null;

                for (PcuInWork pcuInWork : pcuInWorklist) {
                    pcuInWorkWithoutBO = new StartRequestDetails();
                    pcuInWorkWithoutBO = boRemover(pcuInWork);

//                    pcuInWorkWithoutBO.setOperation(pcu1.getOperation());
//                    pcuInWorkWithoutBO.setItem(pcu1.getItem());
//                    pcuInWorkWithoutBO.setUser(pcu1.getUser());
//                    pcuInWorkWithoutBO.setPcu(pcu1.getPcu());
//                    pcuInWorkWithoutBO.setResource(pcu1.getResource());
//                    pcuInWorkWithoutBO.setChildRouter(pcu1.getChildRouter());
//                    pcuInWorkWithoutBO.setRouter(pcu1.getRouter());
//                    pcuInWorkWithoutBO.setShopOrder(pcu1.getShopOrder());
                    pcuInWorkWithoutBO.setSite(pcuInWork.getSite());
                    pcuInWorkWithoutBO.setHandle(pcuInWork.getHandle());
                    pcuInWorkWithoutBO.setStepID(pcuInWork.getStepID());
                    pcuInWorkWithoutBO.setQtyInWork(pcuInWork.getQtyInWork());
                    pcuInWorkWithoutBO.setWorkCenter(pcuInWork.getWorkCenter());
                    pcuInWorkWithoutBO.setQtyToComplete(pcuInWork.getQtyToComplete());
                    pcuInWorkWithoutBO.setParentStepID(pcuInWork.getParentStepID());
                    pcuInWorkWithoutBO.setType(pcuInWork.getType());
                    pcuInWorkWithoutBO.setActive(pcuInWork.getActive());
                    pcuInWorkWithoutBO.setCreatedDateTime(pcuInWork.getCreatedDateTime());

                    pcuInWorkWithoutBOList.add(pcuInWorkWithoutBO);

                    pcuInWorkWithoutBO = null;
                }
                pcuInWorklist = null;
            }
        }

        return pcuInWorkWithoutBOList;

    }

    @Override
    public List<StartRequestDetails>  retrieveListByOperation(StartRequestDetails startRequest) throws Exception {
        String operationBO = BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion());

        List<PcuInWork> pcuInWorklist = startRepository.findByActiveAndSiteAndOperationBO(1,startRequest.getSite(),operationBO, getPagable(startRequest.getRecordLimit()));
        List<StartRequestDetails> pcuInWorkWithoutBOList = new ArrayList<>();

        if(pcuInWorklist != null) {
            StartRequestDetails pcu1 = null;

            for(PcuInWork pcuInWork : pcuInWorklist) {
                pcu1 = new StartRequestDetails();
                pcu1 = boRemover(pcuInWork);

//                requestWithoutBO.setOperation(pcu1.getOperation());
//                requestWithoutBO.setOperationVersion(pcu1.getOperationVersion());
//                requestWithoutBO.setItem(pcu1.getItem());
//                requestWithoutBO.setItemVersion(pcu1.getItemVersion());
//                requestWithoutBO.setUser(pcu1.getUser());
//                requestWithoutBO.setPcu(pcu1.getPcu());
//                requestWithoutBO.setResource(pcu1.getResource());
//                requestWithoutBO.setChildRouter(pcu1.getChildRouter());
//                requestWithoutBO.setChildRouterVersion(pcu1.getChildRouterVersion());
//                requestWithoutBO.setRouter(pcu1.getRouter());
//                requestWithoutBO.setRouterVersion(pcu1.getRouterVersion());
//                requestWithoutBO.setShopOrder(pcu1.getShopOrder());
                pcu1.setSite(pcuInWork.getSite());
                pcu1.setHandle(pcuInWork.getHandle());
                pcu1.setStepID(pcuInWork.getStepID());
                pcu1.setQtyInWork(pcuInWork.getQtyInWork());
                pcu1.setWorkCenter(pcuInWork.getWorkCenter());
                pcu1.setQtyToComplete(pcuInWork.getQtyToComplete());
                pcu1.setParentStepID(pcuInWork.getParentStepID());
                pcu1.setActive(pcuInWork.getActive());

                pcuInWorkWithoutBOList.add(pcu1);

                pcu1 = null;
            }
        }

        pcuInWorklist = null;
        return pcuInWorkWithoutBOList;
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
            throw new StartException(800);
        }
        return extensionResponse;
    }
    public List<StartRequestDetails> getAllPcuByRoute(StartRequestDetails startRequest){
        String pcuBO = BOConverter.retrievePcuBO(startRequest.getSite(), startRequest.getPcu());
        String routerBO = BOConverter.retrieveRouterBO(startRequest.getSite(), startRequest.getRouter(), startRequest.getRouterVersion());

        List<PcuInWork> pcuInWorklist = startRepository.findByActiveAndPcuBOAndRouterBO(startRequest.getActive(),pcuBO,routerBO,startRequest.getSite());
        List<StartRequestDetails> pcuInWorkWithoutBOList = new ArrayList<>();

        if(pcuInWorklist != null) {
            StartRequestDetails pcuInWorkWithoutBO = null;

            for(PcuInWork pcuInWork : pcuInWorklist) {
                pcuInWorkWithoutBO = new StartRequestDetails();
                pcuInWorkWithoutBO = boRemover(pcuInWork);

//                pcuInWorkWithoutBO.setOperation(pcu1.getOperation());
//                pcuInWorkWithoutBO.setItem(pcu1.getItem());
//                pcuInWorkWithoutBO.setUser(pcu1.getUser());
//                pcuInWorkWithoutBO.setPcu(pcu1.getPcu());
//                pcuInWorkWithoutBO.setResource(pcu1.getResource());
//                pcuInWorkWithoutBO.setChildRouter(pcu1.getChildRouter());
//                pcuInWorkWithoutBO.setRouter(pcu1.getRouter());
//                pcuInWorkWithoutBO.setShopOrder(pcu1.getShopOrder());
                pcuInWorkWithoutBO.setSite(pcuInWork.getSite());
                pcuInWorkWithoutBO.setHandle(pcuInWork.getHandle());
                pcuInWorkWithoutBO.setStepID(pcuInWork.getStepID());
                pcuInWorkWithoutBO.setQtyInWork(pcuInWork.getQtyInWork());
                pcuInWorkWithoutBO.setWorkCenter(pcuInWork.getWorkCenter());
                pcuInWorkWithoutBO.setQtyToComplete(pcuInWork.getQtyToComplete());
                pcuInWorkWithoutBO.setParentStepID(pcuInWork.getParentStepID());
                pcuInWorkWithoutBO.setActive(pcuInWork.getActive());

                pcuInWorkWithoutBOList.add(pcuInWorkWithoutBO);
                pcuInWorkWithoutBO = null;
            }
            pcuInWorklist = null;
        }

        return pcuInWorkWithoutBOList;
    }

//    public PcuInWork pcuInworkBOBuilder(PcuInWorkWithoutBO pcuInWorkWithoutBO){
//
//        PcuInWork pcuInWork = new PcuInWork();
//        if(pcuInWorkWithoutBO.getPcu()!=null && !pcuInWorkWithoutBO.getPcu().isEmpty())
//            pcuInWork.setPcuBO("PcuBO:"+pcuInWorkWithoutBO.getSite()+","+pcuInWorkWithoutBO.getPcu());
//
//        if(pcuInWorkWithoutBO.getItem()!=null && !pcuInWorkWithoutBO.getItem().isEmpty()) {
//            String[] itemArray = pcuInWorkWithoutBO.getItem().split("/");
//            pcuInWork.setItemBO("ItemBO:" + pcuInWorkWithoutBO.getSite() + "," + itemArray[0] + "," + itemArray[1]);
//        }
//
//        if(pcuInWorkWithoutBO.getRouter()!=null && !pcuInWorkWithoutBO.getRouter().isEmpty()) {
//            String[] routerArray = pcuInWorkWithoutBO.getRouter().split("/");
//            pcuInWork.setRouterBO("RoutingBO:" + pcuInWorkWithoutBO.getSite() + "," + routerArray[0] + "," + routerArray[1]);
//        }
//
//        if(pcuInWorkWithoutBO.getOperation()!=null && !pcuInWorkWithoutBO.getOperation().isEmpty()) {
//            String[] opArray = pcuInWorkWithoutBO.getOperation().split("/");
//            pcuInWork.setOperationBO("OperationBO:" + pcuInWorkWithoutBO.getSite() + "," + opArray[0] + "," + opArray[1]);
//        }
//
//        if(pcuInWorkWithoutBO.getResource()!=null && !pcuInWorkWithoutBO.getResource().isEmpty())
//            pcuInWork.setResourceBO("ResourceBO:"+pcuInWorkWithoutBO.getSite()+","+pcuInWorkWithoutBO.getResource());
//
//        if(pcuInWorkWithoutBO.getUser()!=null && !pcuInWorkWithoutBO.getUser().isEmpty())
//            pcuInWork.setUserBO("UserBO:"+pcuInWorkWithoutBO.getSite()+","+pcuInWorkWithoutBO.getUser());
//
//        if(pcuInWorkWithoutBO.getShopOrder()!=null && !pcuInWorkWithoutBO.getShopOrder().isEmpty())
//            pcuInWork.setShopOrderBO("ShopOrderBO:"+pcuInWorkWithoutBO.getSite()+","+pcuInWorkWithoutBO.getShopOrder());
//
//        if(pcuInWorkWithoutBO.getChildRouter()!=null && !pcuInWorkWithoutBO.getChildRouter().isEmpty()) {
//            String[] childRouter = pcuInWorkWithoutBO.getChildRouter().split("/");
//            pcuInWork.setChildRouterBO("ChildRouterBO:" + pcuInWorkWithoutBO.getSite() + "," + childRouter[0] + "," + childRouter[1]);
//        }
//        return pcuInWork;
//    }

    @Override
    public PcuInWorkMessageModel updateAllPcu(StartRequestDetails startRequest)throws Exception{
        List<StartRequestDetails> inworkList= startRequest.getInWorkList();

        for(StartRequestDetails obj : inworkList) {
            StartRequest pcu1 = boCreator(obj);
            PcuInWork pcuInWork = PcuInWork.builder()
                    .site(obj.getSite())
                    .pcuBO(pcu1.getPcuBO())
                    .itemBO(pcu1.getItemBO())
                    .routerBO(pcu1.getRouterBO())
                    .operationBO(pcu1.getOperationBO())
                    .resourceBO(pcu1.getResourceBO())
                    .stepID(obj.getStepID())
                    .userBO(pcu1.getUserBO())
                    .qtyToComplete(obj.getQtyToComplete())
                    .shopOrderBO(pcu1.getShopOrderBO())
                    .qtyInWork(obj.getQtyInWork())
                    .handle(obj.getHandle())
                    .active(0)
                    .build();

            startRepository.save(pcuInWork);

            return PcuInWorkMessageModel.builder().message_details(new MessageDetails(startRequest.getPcu() + " updated SuccessFully", "S")).response(obj).build();

        }
        return null;
    }
    public Boolean productionLog(StartRequestDetails startRequest){
//        MinutesList shiftRecordList = getShiftBreakHours(startRequest.getSite());
//        Minutes minutesRecord = null;
//        for(Minutes shift : shiftRecordList.getMinutesList())
//        {
//            if(shift.getShiftType().equalsIgnoreCase("general"))
//            {
//                minutesRecord = shift;
//                break;
//            }
//        }
        ProductionLogRequest productionLogRequest = ProductionLogRequest.builder()
                .eventType("BATCH_START")
                .userId(startRequest.getUser())
                .pcu(startRequest.getPcu())
                .batchNo(startRequest.getPcu())
                .orderNumber(startRequest.getShopOrder())
                .shopOrderBO(startRequest.getShopOrder())
                .operation_bo(BOConverter.retrieveOperationBO(startRequest.getSite(), startRequest.getOperation(), startRequest.getOperationVersion()))
                .routerBO(BOConverter.retrieveRouterBO(startRequest.getSite(), startRequest.getRouter(), startRequest.getRouterVersion()))
                .workcenterId(startRequest.getWorkCenter())
                .resourceId(startRequest.getResource())
                .itemBO(BOConverter.retrieveItemBO(startRequest.getSite(), startRequest.getItem(), startRequest.getItemVersion()))
                .qty(startRequest.getQuantity() != null ? Integer.parseInt(startRequest.getQuantity()) : 0)
                .site(startRequest.getSite())
//                .shiftName(minutesRecord.getShiftName())
//                .shiftStartTime(minutesRecord.getStartTime().toString())
//                .shiftEndTime(minutesRecord.getEndTime().toString())
//                .totalBreakHours(String.valueOf(minutesRecord.getMinutes()))
                .topic("production-log")
                .status("Active")
                .eventData(startRequest.getPcu()+" Started successfully")
                .build();
        eventPublisher.publishEvent(new ProductionLogProducer(productionLogRequest));
        return true;
    }

//    public MinutesList getShiftBreakHours(String site)
//    {
//        ShiftRequest retrieveShiftRequest = ShiftRequest.builder().site(site).build();
//        MinutesList retrievedRecord = webClientBuilder.build()
//                .post()
//                .uri(getShiftBreakHoursUrl)
//                .bodyValue(retrieveShiftRequest)
//                .retrieve()
//                .bodyToMono(MinutesList.class)
//                .block();
//        return retrievedRecord;
//    }

    @Override
    public List<StartRequestDetails> getAllInQueuePcuBySite(String site)
    {
        List<PcuInWork> getPcus = startRepository.findByActiveAndSite(1,site);

        PcuInQueueRequest buildPcuInQueueReq =  PcuInQueueRequest.builder().site(site).build();
        List<PcuInQueue> retrievePcuInQueues = webClientBuilder.build()
                .post()
                .uri(retrieveAllpcuInQueueBySiteUrl)
                .bodyValue(buildPcuInQueueReq)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PcuInQueue>>() {
                })
                .block();

        List<StartRequestDetails> pcuInWorkWithoutBOList = new ArrayList<>();

        if(getPcus != null)
        {
            for(PcuInQueue pcuInQueue : retrievePcuInQueues){

                StartRequestDetails pcuInWork = StartRequestDetails.builder()
                        .pcu(pcuInQueue.getPcu())
                        .operation(pcuInQueue.getOperation()).operationVersion(pcuInQueue.getOperationVersion())
                        .shopOrder(pcuInQueue.getShopOrder())
                        .site(pcuInQueue.getSite())
                        .active(pcuInQueue.getActive()).build();
                pcuInWorkWithoutBOList.add(pcuInWork);
            }
            for(PcuInWork pcuInWork1 : getPcus){
                String pcu = BOConverter.getPcu(pcuInWork1.getPcuBO());
                String operation = BOConverter.getOperation(pcuInWork1.getOperationBO());
                String operationVersion = BOConverter.getOperationVersion(pcuInWork1.getOperationBO());
                String shopOrder = BOConverter.getShopOrder(pcuInWork1.getShopOrderBO());

                StartRequestDetails pcuInWork = StartRequestDetails.builder()
                        .pcu(pcu)
                        .operation(operation).operationVersion(operationVersion)
                        .shopOrder(shopOrder)
                        .site(pcuInWork1.getSite())
                        .active(pcuInWork1.getActive())
                        .build();
                pcuInWorkWithoutBOList.add(pcuInWork);
            }
        }
        List<StartRequestDetails> retrievedRecords = pcuInWorkWithoutBOList.stream().distinct().collect(Collectors.toList());
        pcuInWorkWithoutBOList = null;
        retrievePcuInQueues = null;
        buildPcuInQueueReq = null;
        getPcus = null;

        return retrievedRecords;
    }

    public String getOperationCurrentVer(StartRequestDetails startRequest) throws Exception{
        Operation oper = Operation.builder().site(startRequest.getSite()).operation(startRequest.getOperation()).build();

        try {
            Operation operVersion = webClientBuilder.build()
                    .post()
                    .uri(retrieveOperationByCurrentVersionUrl)
                    .bodyValue(oper)
                    .retrieve()
                    .bodyToMono(Operation.class)
                    .block();

            if(operVersion == null)
                throw new PcuInQueueException(1710);

            return operVersion.getRevision();

        } catch (Exception e){
            throw e;
        }
    }

}