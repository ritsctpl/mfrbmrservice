package com.rits.pcuinqueueservice.service;

import com.rits.Utility.BOConverter;
import com.rits.pcuinqueueservice.dto.Extension;
import com.rits.pcuinqueueservice.dto.PcuInQueueRequest;
import com.rits.pcuinqueueservice.dto.PcuInQueueReq;
import com.rits.pcuinqueueservice.exception.PcuInQueueException;
import com.rits.pcuinqueueservice.model.MessageDetails;
import com.rits.pcuinqueueservice.model.MessageModel;
import com.rits.pcuinqueueservice.model.PcuInQueue;
import com.rits.pcuinqueueservice.model.PcuInQueueDetails;
import com.rits.pcuinqueueservice.repository.PcuInQueueRepository;
import com.rits.worklistservice.dto.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import org.springframework.data.domain.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PcuInQueueServiceImpl implements  PcuInQueueService{
    private final PcuInQueueRepository pcuInQueueRepository;
    private final WebClient.Builder webClientBuilder;
    private final MongoTemplate mongoTemplate;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Value("${operation-service.url}/retrieveOperationByCurrentVersion")
    private String retrieveOperationByCurrentVersionUrl;

    @Override
    public PcuInQueueRequest convertToPcuInQueueRequest(PcuInQueueReq pcuInQueueReq) {

        PcuInQueueRequest request = new PcuInQueueRequest();

        if (pcuInQueueReq.getHandle() != null && !pcuInQueueReq.getHandle().isEmpty()) {
            request.setHandle(pcuInQueueReq.getHandle());
        }

        if (pcuInQueueReq.getSite() != null && !pcuInQueueReq.getSite().isEmpty()) {
            request.setSite(pcuInQueueReq.getSite());
        }

        if (pcuInQueueReq.getDateTime() != null) {
            request.setDateTime(pcuInQueueReq.getDateTime());
        }

        if (pcuInQueueReq.getPcu() != null && !pcuInQueueReq.getPcu().isEmpty()) {
            request.setPcuBO(BOConverter.retrievePcuBO(pcuInQueueReq.getSite(), pcuInQueueReq.getPcu()));
        }

        if (pcuInQueueReq.getItem() != null && !pcuInQueueReq.getItem().isEmpty()) {
            request.setItemBO(BOConverter.retrieveItemBO(pcuInQueueReq.getSite(), pcuInQueueReq.getItem(), pcuInQueueReq.getItemVersion()));
        }

        if (pcuInQueueReq.getRouter() != null && !pcuInQueueReq.getRouter().isEmpty()) {
            request.setRouterBO(BOConverter.retrieveRouterBO(pcuInQueueReq.getSite(), pcuInQueueReq.getRouter(), pcuInQueueReq.getRouterVersion()));
        }

        if (pcuInQueueReq.getResource() != null && !pcuInQueueReq.getResource().isEmpty()) {
            request.setResourceBO(BOConverter.retriveResourceBO(pcuInQueueReq.getSite(), pcuInQueueReq.getResource()));
        }

        if (pcuInQueueReq.getOperation() != null && !pcuInQueueReq.getOperation().isEmpty()) {
            request.setOperationBO(BOConverter.retrieveOperationBO(pcuInQueueReq.getSite(), pcuInQueueReq.getOperation(), pcuInQueueReq.getOperationVersion()));
        }

        if (pcuInQueueReq.getStepID() != null && !pcuInQueueReq.getStepID().isEmpty()) {
            request.setStepID(pcuInQueueReq.getStepID());
        }

        if (pcuInQueueReq.getUser() != null && !pcuInQueueReq.getUser().isEmpty()) {
            request.setUserBO(BOConverter.retrieveUserBO(pcuInQueueReq.getSite(), pcuInQueueReq.getUser()));
        }

        if (pcuInQueueReq.getQtyToComplete() != null) {
            request.setQtyToComplete(pcuInQueueReq.getQtyToComplete());
        }

        if (pcuInQueueReq.getQtyInQueue() != null) {
            request.setQtyInQueue(pcuInQueueReq.getQtyInQueue());
        }

        if (pcuInQueueReq.getShopOrder() != null && !pcuInQueueReq.getShopOrder().isEmpty()) {
            request.setShopOrderBO(BOConverter.retrieveShopOrderBO(pcuInQueueReq.getSite(), pcuInQueueReq.getShopOrder()));
        }

        if (pcuInQueueReq.getChildRouter() != null && !pcuInQueueReq.getChildRouter().isEmpty()) {
            request.setChildRouterBO(BOConverter.retrieveChildRouterBO(pcuInQueueReq.getSite(), pcuInQueueReq.getChildRouter(), pcuInQueueReq.getChildRouterVersion()));
        }

        if (pcuInQueueReq.getParentStepID() != null && !pcuInQueueReq.getParentStepID().isEmpty()) {
            request.setParentStepID(pcuInQueueReq.getParentStepID());
        }

        if (pcuInQueueReq.getWorkCenter() != null && !pcuInQueueReq.getWorkCenter().isEmpty()) {
            request.setWorkCenter(pcuInQueueReq.getWorkCenter());
        }

        if (pcuInQueueReq.getDisable() != null) {
            request.setDisable(pcuInQueueReq.getDisable());
        }
        request.setActive(pcuInQueueReq.getActive());

        return request;
    }

    @Override
    public PcuInQueueDetails convertToPcuInQueueNoBO(PcuInQueue pcuInQueue) {
            if (pcuInQueue == null) {
//                throw new IllegalArgumentException("PcuInQueue cannot be null");
                throw new PcuInQueueException(2916);
            }

            PcuInQueueDetails pcuInQueueNoBO = new PcuInQueueDetails();

            if (pcuInQueue.getSite() != null && !pcuInQueue.getSite().isEmpty()) {
                pcuInQueueNoBO.setSite(pcuInQueue.getSite());
            }

            if (pcuInQueue.getHandle() != null && !pcuInQueue.getHandle().isEmpty()) {
                pcuInQueueNoBO.setHandle(pcuInQueue.getHandle());
            }

            pcuInQueueNoBO.setDateTime(pcuInQueue.getDateTime());

            if (pcuInQueue.getPcuBO() != null && !pcuInQueue.getPcuBO().isEmpty()) {
                pcuInQueueNoBO.setPcu(BOConverter.getPcu(pcuInQueue.getPcuBO()));
            }

            if (pcuInQueue.getItemBO() != null && !pcuInQueue.getItemBO().isEmpty()) {
                pcuInQueueNoBO.setItem(BOConverter.getItem(pcuInQueue.getItemBO()));
                pcuInQueueNoBO.setItemVersion(BOConverter.getItemVersion(pcuInQueue.getItemBO()));
            }

            if (pcuInQueue.getRouterBO() != null && !pcuInQueue.getRouterBO().isEmpty()) {
                pcuInQueueNoBO.setRouter(BOConverter.getRouter(pcuInQueue.getRouterBO()));
                pcuInQueueNoBO.setRouterVersion(BOConverter.getRouterVersion(pcuInQueue.getRouterBO()));
            }

            if (pcuInQueue.getOperationBO() != null && !pcuInQueue.getOperationBO().isEmpty()) {
                pcuInQueueNoBO.setOperation(BOConverter.getOperation(pcuInQueue.getOperationBO()));
                pcuInQueueNoBO.setOperationVersion(BOConverter.getOperationVersion(pcuInQueue.getOperationBO()));
            }

            if (pcuInQueue.getResourceBO() != null && !pcuInQueue.getResourceBO().isEmpty()) {
                pcuInQueueNoBO.setResource(BOConverter.getResource(pcuInQueue.getResourceBO()));
            }

            if (pcuInQueue.getStepID() != null && !pcuInQueue.getStepID().isEmpty()) {
                pcuInQueueNoBO.setStepID(pcuInQueue.getStepID());
            }

            if (pcuInQueue.getUserBO() != null && !pcuInQueue.getUserBO().isEmpty()) {
                pcuInQueueNoBO.setUser(BOConverter.getUser(pcuInQueue.getUserBO()));
            }

            if (pcuInQueue.getQtyToComplete() != null && !pcuInQueue.getQtyToComplete().isEmpty()) {
                pcuInQueueNoBO.setQtyToComplete(pcuInQueue.getQtyToComplete());
            }

            if (pcuInQueue.getQtyInQueue() != null && !pcuInQueue.getQtyInQueue().isEmpty()) {
                pcuInQueueNoBO.setQtyInQueue(pcuInQueue.getQtyInQueue());
            }

            if (pcuInQueue.getShopOrderBO() != null && !pcuInQueue.getShopOrderBO().isEmpty()) {
                pcuInQueueNoBO.setShopOrder(BOConverter.getShopOrder(pcuInQueue.getShopOrderBO()));
            }

            if (pcuInQueue.getChildRouterBO() != null && !pcuInQueue.getChildRouterBO().isEmpty()) {
                pcuInQueueNoBO.setChildRouter(BOConverter.getChildRouter(pcuInQueue.getChildRouterBO()));
                pcuInQueueNoBO.setChildRouterVersion(BOConverter.getChildRouterVersion(pcuInQueue.getChildRouterBO()));
            }

            if (pcuInQueue.getParentStepID() != null && !pcuInQueue.getParentStepID().isEmpty()) {
                pcuInQueueNoBO.setParentStepID(pcuInQueue.getParentStepID());
            }

            if (pcuInQueue.getWorkCenter() != null && !pcuInQueue.getWorkCenter().isEmpty()) {
                pcuInQueueNoBO.setWorkCenter(pcuInQueue.getWorkCenter());
            }

            pcuInQueueNoBO.setActive(pcuInQueue.getActive());
            pcuInQueueNoBO.setType(pcuInQueue.getType());

            return pcuInQueueNoBO;
    }

    @Override
    public MessageModel createPcuInQueue(PcuInQueueRequest pcuInQueueRequest) throws  Exception {
        if (pcuInQueueRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBO(1, pcuInQueueRequest.getSite(), pcuInQueueRequest.getPcuBO(), pcuInQueueRequest.getOperationBO(), pcuInQueueRequest.getShopOrderBO())) {
            return updatePcuInQueue(pcuInQueueRequest);
        } else {
            PcuInQueue pcuInQueue = PcuInQueue.builder()
                    .site(pcuInQueueRequest.getSite())
                    .handle(pcuInQueueRequest.getHandle())
                    .dateTime(LocalDateTime.now())
                    .pcuBO(pcuInQueueRequest.getPcuBO())
                    .itemBO(pcuInQueueRequest.getItemBO())
                    .resourceBO(pcuInQueueRequest.getResourceBO())
                    .routerBO(pcuInQueueRequest.getRouterBO())
                    .operationBO(pcuInQueueRequest.getOperationBO())
                    .stepID(pcuInQueueRequest.getStepID())
                    .userBO(pcuInQueueRequest.getUserBO())
                    .workCenter(pcuInQueueRequest.getWorkCenter())
                    .qtyToComplete(pcuInQueueRequest.getQtyToComplete())
                    .qtyInQueue(pcuInQueueRequest.getQtyInQueue())
                    .shopOrderBO(pcuInQueueRequest.getShopOrderBO())
                    .childRouterBO(pcuInQueueRequest.getChildRouterBO())
                    .parentStepID(pcuInQueueRequest.getParentStepID())
                    .type("inqueue")
                    .active(1)
                    .build();

            PcuInQueue savedPcuInQueue = pcuInQueueRepository.save(pcuInQueue);

            PcuInQueueDetails pcuInQueueNoBO = convertToPcuInQueueNoBO(savedPcuInQueue);

            return MessageModel.builder()
                    .message_details(new MessageDetails(pcuInQueueNoBO.getPcu() + " created Successfully", "S"))
                    .response(pcuInQueueNoBO)
                    .build();
        }
    }

    @Override
    public MessageModel updateAllPcu(PcuInQueueRequest pcuInQueueRequest)throws Exception{
       List<PcuInQueue> pculist= pcuInQueueRequest.getPcuList();
        PcuInQueue pcuInQueue=new PcuInQueue();
        for (PcuInQueue obj : pculist) {
             pcuInQueue = PcuInQueue.builder()
                    .site(obj.getSite())
                    .pcuBO(obj.getPcuBO())
                    .itemBO(obj.getItemBO())
                    .routerBO(obj.getRouterBO())
                    .operationBO(obj.getOperationBO())
                    .resourceBO(obj.getResourceBO())
                    .stepID(obj.getStepID())
                    .userBO(obj.getUserBO())
                    .qtyToComplete(obj.getQtyToComplete())
                    .shopOrderBO(obj.getShopOrderBO())
                    .qtyInQueue(obj.getQtyInQueue())
                    .handle(obj.getHandle())
                    .active(0)
                    .build();

            PcuInQueue savedPcuInQueue = pcuInQueueRepository.save(pcuInQueue);

            PcuInQueueDetails pcuInQueueNoBO = convertToPcuInQueueNoBO(savedPcuInQueue);

            return MessageModel.builder()
                    .message_details(new MessageDetails(pcuInQueueNoBO.getPcu() + " updated Successfully", "S"))
                    .response(pcuInQueueNoBO)
                    .build();
        }
           return null;
    }

    @Override
    public MessageModel updatePcuInQueue(PcuInQueueRequest pcuInQueueRequest) throws Exception {

        if(pcuInQueueRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBO(1,pcuInQueueRequest.getSite(),pcuInQueueRequest.getPcuBO(),pcuInQueueRequest.getOperationBO(),pcuInQueueRequest.getShopOrderBO())) {
            PcuInQueue existingPcuInQueue = pcuInQueueRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBO(1, pcuInQueueRequest.getSite(), pcuInQueueRequest.getPcuBO(), pcuInQueueRequest.getOperationBO(), pcuInQueueRequest.getShopOrderBO());
            PcuInQueue pcuInQueue = PcuInQueue.builder()
                    .site(existingPcuInQueue.getSite())
                    .handle(existingPcuInQueue.getHandle())
                    .dateTime(pcuInQueueRequest.getDateTime())
                    .pcuBO(existingPcuInQueue.getPcuBO())
                    .itemBO(pcuInQueueRequest.getItemBO())
                    .resourceBO(existingPcuInQueue.getResourceBO())
                    .routerBO(pcuInQueueRequest.getRouterBO())
                    .operationBO(existingPcuInQueue.getOperationBO())
                    .stepID(pcuInQueueRequest.getStepID())
                    .userBO(pcuInQueueRequest.getUserBO())
                    .workCenter(pcuInQueueRequest.getWorkCenter())
                    .qtyToComplete(pcuInQueueRequest.getQtyToComplete())
                    .qtyInQueue(pcuInQueueRequest.getQtyInQueue())
                    .shopOrderBO(existingPcuInQueue.getShopOrderBO())
                    .childRouterBO(pcuInQueueRequest.getChildRouterBO())
                    .parentStepID(pcuInQueueRequest.getParentStepID())
                    .type("inqueue")
                    .active(1)
                    .build();

            PcuInQueue savedPcuInQueue = pcuInQueueRepository.save(pcuInQueue);

            PcuInQueueDetails pcuInQueueNoBO = convertToPcuInQueueNoBO(savedPcuInQueue);

            return MessageModel.builder()
                    .message_details(new MessageDetails(pcuInQueueNoBO.getPcu() + " updated Successfully", "S"))
                    .response(pcuInQueueNoBO)
                    .build();
        }
           else {
            return createPcuInQueue(new PcuInQueueRequest());
        }
    }

    @Override
    public Boolean deletePcuInQueue(PcuInQueueReq pcuInQueueReq) throws Exception {
        if (pcuInQueueReq == null) {
            throw new PcuInQueueException(2916);
        }
        if (pcuInQueueReq.getSite() == null || pcuInQueueReq.getSite().isEmpty()) {
            throw new PcuInQueueException(9902);
        }
        if (pcuInQueueReq.getPcu() == null || pcuInQueueReq.getPcu().isEmpty()) {
            throw new PcuInQueueException(3503);
        }
        PcuInQueueRequest pcuInQueueRequest = convertToPcuInQueueRequest(pcuInQueueReq);
        if(pcuInQueueRepository.existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBO(1, pcuInQueueRequest.getSite(), pcuInQueueRequest.getPcuBO(), pcuInQueueRequest.getOperationBO(), pcuInQueueRequest.getShopOrderBO())){
            PcuInQueue pcuInQueue=pcuInQueueRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBO(1, pcuInQueueRequest.getSite(), pcuInQueueRequest.getPcuBO(), pcuInQueueRequest.getOperationBO(), pcuInQueueRequest.getShopOrderBO());
            pcuInQueueRepository.delete(pcuInQueue);
            return true;
        }
        else{
            throw new PcuInQueueException(3604,pcuInQueueRequest.getPcuBO(),pcuInQueueRequest.getOperationBO(),pcuInQueueRequest.getResourceBO());
        }
    }

    @Override
    public List<PcuInQueueDetails> convertToPcuInQueueNoBOAsList(List<PcuInQueue> responseList) {
        return responseList.stream()
                .map(this::convertToPcuInQueueNoBO)
                .collect(Collectors.toList());
    }

    @Override
    public PcuInQueue retrievePcuInQueueAndItem(String site, String pcuBo, String item,String operation) throws Exception {
        PcuInQueue existingPcuInQueue=pcuInQueueRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndItemBO(1,site,pcuBo,operation,item);
        if(existingPcuInQueue==null)
        {
            throw new PcuInQueueException(3603,pcuBo,operation,item);
        }
        return existingPcuInQueue;
    }

    @Override
    public PcuInQueue retrievePcuInQueueAndOperation(String site, String pcuBo, String operation) throws Exception {
        PcuInQueue existingPcuInQueue = null;

        if(StringUtils.isNotEmpty(operation) && BOConverter.getOperationVersion(operation).equals(null))
            existingPcuInQueue=pcuInQueueRepository.findByActiveAndSiteAndPcuBOAndOperationBOContaining(1,site,pcuBo,BOConverter.getOperation(operation));
        else
            existingPcuInQueue=pcuInQueueRepository.findByActiveAndSiteAndPcuBOAndOperationBO(1,site,pcuBo,operation);

        if(existingPcuInQueue==null)
        {
            throw new PcuInQueueException(3602,pcuBo,operation);
        }
        return existingPcuInQueue;
    }

    @Override
    public PcuInQueue retrievePcuInQueueAndResource(String site, String pcuBo, String resource,String operation) throws Exception {
        PcuInQueue existingPcuInQueue=pcuInQueueRepository.findByActiveAndSiteAndPcuBOAndOperationBOAndResourceBO(1,site,pcuBo,operation,resource);
        if(existingPcuInQueue==null)
        {
            throw new PcuInQueueException(3604,pcuBo,operation,resource);
        }
        return existingPcuInQueue;
    }

    private Pageable getPagable(int maxRecords){
        if (maxRecords > 0) {
            return PageRequest.of(0, maxRecords, Sort.by(Sort.Direction.DESC, "dateTime"));
        } else {
            return Pageable.unpaged(); // Retrieve all records
        }
    }

    @Override
    public List<PcuInQueue> retrieveListOfPcuBO(int maxRecords, String site, String operation, String resource) throws Exception {
        return pcuInQueueRepository.findByActiveAndSiteAndOperationBOAndResourceBO(1, site, operation, resource, getPagable(maxRecords));
    }

    @Override
    public List<PcuInQueue> retrieveListOfPcuBOByOperation(int maxRecords, String site, String operation) throws Exception {
        return pcuInQueueRepository.findByActiveAndSiteAndOperationBO(1,site,operation, getPagable(maxRecords));
    }

    @Override
    public List<PcuInQueue> retrieveListOfPcuBOByOperationAndShopOrderBO(String site, String operation, String shopOrderBO) throws Exception {
        return pcuInQueueRepository.findByActiveAndSiteAndOperationBOAndShopOrderBO(1,site,operation,shopOrderBO);
    }

    @Override
    public List<PcuInQueue> retrieveListOfPcuBOByPcu(int maxRecords, String site, String operation, String resource, String pcuBO) throws Exception {
        return pcuInQueueRepository.findByActiveAndSiteAndOperationBOAndResourceBOAndPcuBO(1,site,operation,resource,pcuBO, getPagable(maxRecords));
    }

//    @Override
//    public List<PcuInQueue> retrieveListOfPcuBOByPcuAndOperation(String site, String operation, String pcuBO) throws Exception {
//        List<PcuInQueue> pcuInQueues = pcuInQueueRepository.findByActiveAndSiteAndPcuBO(1, site, pcuBO);
//        List<PcuInQueue> pcuInQueueList = new ArrayList<>();
//        for(PcuInQueue pcuInQueue : pcuInQueues){
//            if(operation.equals(BOConverter.getOperation(pcuInQueue.getOperationBO()))){
//                pcuInQueueList.add(pcuInQueue);
//            }
//        }
//        return pcuInQueueList;
//    }

    private String escapeRegex(String input) {
        return input.replaceAll("([\\\\.*+?^${}()|\\[\\]])", "\\\\$1");
    }

    public  List<PcuInQueue> retrievePcuInQueue(String site, String operation, String resource, String pcuBO) {
        Query query = new Query(Criteria
                .where("active").is(1)
                .and("site").is(site));
               if(operation!=null){
                   query.addCriteria(Criteria.where("operationBO").is(operation));
               }

        if (resource != null) {
            query.addCriteria(Criteria.where("resourceBO").is(resource));
        }

        if (pcuBO != null) {
            query.addCriteria(Criteria.where("pcuBO").is(pcuBO));
        }

        List<PcuInQueue> pcuInQueues= mongoTemplate.find(query, PcuInQueue.class);
        HashMap<String,String> shopOrderAndPriority=new HashMap<>();
        for(PcuInQueue pcuInQueue:pcuInQueues){
        }
        return  pcuInQueues;
    }

    @Override
    public List<PcuInQueue> retrieveByPcuAndSite(String pcu, String site) throws Exception
    {
        List<PcuInQueue> existingPcuInQueue = pcuInQueueRepository.findByActiveAndSiteAndPcuBO(1, site, pcu);
        return existingPcuInQueue;
    }

    @Override
    public List<PcuInQueue> retrieveByPcuAndSiteForUnscrap(String pcu, String site) throws Exception
    {
        List<PcuInQueue> existingPcuInQueue = pcuInQueueRepository.findByActiveAndSiteAndPcuBO(0, site, pcu);
        return existingPcuInQueue;
    }

    @Override
    public Boolean deletePcuInallOperation(String pcu, String site) throws Exception
    {
        Boolean isPCuDeletedFromAllOperations = false;
        List<PcuInQueue> existingPcuInQueue = pcuInQueueRepository.findByActiveAndSiteAndPcuBO(1, site, pcu);
        if(existingPcuInQueue!=null && !existingPcuInQueue.isEmpty())
        {
            for(PcuInQueue pcuInQueue : existingPcuInQueue)
            {
                isPCuDeletedFromAllOperations = false;
                pcuInQueue.setActive(0);
                pcuInQueueRepository.save(pcuInQueue);
                isPCuDeletedFromAllOperations = true;
            }
        }
        return isPCuDeletedFromAllOperations;
    }
    @Override
    public Boolean unDeletePcuInallOperation(String pcu, String site) throws Exception
    {
        Boolean isPCuDeletedFromAllOperations = false;
        List<PcuInQueue> existingPcuInQueue = pcuInQueueRepository.findByActiveAndSiteAndPcuBO(0, site, pcu);
        if(existingPcuInQueue!=null && !existingPcuInQueue.isEmpty())
        {
            for(PcuInQueue pcuInQueue : existingPcuInQueue)
            {
                isPCuDeletedFromAllOperations = false;
                pcuInQueue.setActive(1);
                pcuInQueueRepository.save(pcuInQueue);
                isPCuDeletedFromAllOperations = true;
            }
        }
        return isPCuDeletedFromAllOperations;
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
            throw new PcuInQueueException(800);
        }
        return extensionResponse;
    }
    public List<PcuInQueue> getRecByPcuandRout(PcuInQueueRequest pcuInQueueRequest){
        List<PcuInQueue> pcuInQueuelist= pcuInQueueRepository.findByActiveAndPcuBOAndRouterBO(pcuInQueueRequest.getActive(),pcuInQueueRequest.getPcuBO(),pcuInQueueRequest.getRouterBO());
        return  pcuInQueuelist;
    }

    @Override
    public List<PcuInQueue> retrieveAllPcuBySite(String site)
    {
        List<PcuInQueue> retrievedPcus = pcuInQueueRepository.findByActiveAndSite(1,site);
        return retrievedPcus;
    }

    @Override
    public String getOperationCurrentVer(PcuInQueueReq pcuInQueueReq) throws Exception{
        Operation oper = Operation.builder().site(pcuInQueueReq.getSite()).operation(pcuInQueueReq.getOperation()).build();

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
