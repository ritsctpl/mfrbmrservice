package com.rits.shoporderservice.service;



import com.rits.assemblyservice.dto.AuditLogRequest;
import com.rits.shoporderservice.dto.*;
import com.rits.shoporderservice.exception.ShopOrderException;
import com.rits.shoporderservice.model.MessageDetails;
import com.rits.shoporderservice.model.ShopOrderMessageModel;
import com.rits.shoporderservice.model.SerialPcu;
import com.rits.shoporderservice.model.ShopOrder;
import com.rits.shoporderservice.repository.ShopOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.mongodb.core.MongoTemplate;


import java.time.LocalDateTime;
import java.util.*;


@RequiredArgsConstructor
@Service
public class ShopOrderServiceImpl implements ShopOrderService {
    private final ShopOrderRepository shopOrderRepository;
    private final WebClient.Builder webClientBuilder;
    private final MongoTemplate mongoTemplate;
    private final MessageSource localMessageSource;



    @Value("${bom-service.url}/isExist")
    private String bomUrl;
    @Value("${item-service.url}/isExist")
    private String itemUrl;
    @Value("${routing-service.url}/isExist")
    private String routingUrl;
    @Value("${workcenter-service.url}/isExist")
    private String workCenterUrl;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${nextnumbergenerator-service.url}/retrieveSampleNextNumber")
    private String nextNumberUrl;
    @Value("${pcuheader-service.url}/isExist")
    private String readPcuUrl;

    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;


    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }


    @Override
    public ShopOrderMessageModel createShopOrder(ShopOrderRequest shopOrderRequest) throws Exception {
        if (shopOrderRepository.existsBySiteAndActiveAndShopOrder(shopOrderRequest.getSite(), 1, shopOrderRequest.getShopOrder())) {
            throw new ShopOrderException(3201, shopOrderRequest.getShopOrder());
        } else {
            if(shopOrderRequest.getShopOrder()==null|| shopOrderRequest.getShopOrder().isEmpty()){
                String nextNumberResponse = sampleNextNumber(shopOrderRequest.getSite(),shopOrderRequest.getParentOrder());
                if (nextNumberResponse==null || nextNumberResponse.isEmpty()) {
                    throw new ShopOrderException(5100, shopOrderRequest.getParentOrder());
                }
                shopOrderRequest.setShopOrder(nextNumberResponse);
            }
            validations(shopOrderRequest);
            ShopOrder shopOrder = shopOrderBuilder(shopOrderRequest);
            shopOrder.setCreatedDateTime(LocalDateTime.now());
            shopOrder.setParentOrderBO(shopOrderRequest.getParentOrderBO());
            shopOrder.setParentPcuBO(shopOrderRequest.getParentPcuBO());
            shopOrder.setAvailableQtyToRelease(shopOrderRequest.getBuildQty());
            if(shopOrder.getProductionQuantities().size()!=0) {
                shopOrder.getProductionQuantities().get(0).setReleasedQty("0");
            }
            shopOrder.setCreatedBy(shopOrderRequest.getUserId());
            shopOrder.setShopOrderBO("ShopOrderBO:"+shopOrderRequest.getSite()+","+shopOrderRequest.getShopOrder());
            ShopOrder shopOrderCreate = shopOrderRepository.save(shopOrder);

            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(shopOrderRequest.getSite())
                    .change_stamp("Create")
                    .action_code("SHOPORDER-CREATE")
                    .action_detail("ShopOrder Created "+shopOrderRequest.getShopOrder())
                    .action_detail_handle("ActionDetailBO:"+shopOrderRequest.getSite()+","+"SHOPORDER-CREATE"+","+shopOrderRequest.getUserId()+":"+"com.rits.shoporderservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(shopOrderRequest.getUserId())
                    .router(shopOrderRequest.getPlannedRouting())
                    .router_revision(shopOrderRequest.getRoutingVersion())
                    .item(shopOrderRequest.getPlannedMaterial())
                    .item_revision(shopOrderRequest.getMaterialVersion())
                    .txnId("SHOPORDER-CREATE"+String.valueOf(LocalDateTime.now())+shopOrderRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("SHOP_ORDER")
                    .build();
            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();
            String createdMessage = getFormattedMessage(1, shopOrderRequest.getShopOrder());
            return ShopOrderMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).shopOrderResponse(shopOrderCreate).build();
        }
    }



    @Override
    public ShopOrderMessageModel updateShopOrder(ShopOrderRequest shopOrderRequest) throws Exception {
        if (shopOrderRepository.existsBySiteAndActiveAndShopOrder(shopOrderRequest.getSite(), 1, shopOrderRequest.getShopOrder())) {
            ShopOrder existingShopOrder = shopOrderRepository.findBySiteAndActiveAndShopOrder(shopOrderRequest.getSite(), 1, shopOrderRequest.getShopOrder());
            if (existingShopOrder.isInUse()) {
                throw new ShopOrderException(3223);
            }
                validations(shopOrderRequest);
                ShopOrder shopOrder = shopOrderBuilder(shopOrderRequest);
                shopOrder.setSite(existingShopOrder.getSite());
                shopOrder.setHandle(existingShopOrder.getHandle());
                shopOrder.setShopOrder(existingShopOrder.getShopOrder());
                shopOrder.setParentOrderBO(existingShopOrder.getParentOrderBO());
                shopOrder.setParentPcuBO(existingShopOrder.getParentPcuBO());
            if(shopOrder.getProductionQuantities().size()!=0) {
                shopOrder.setAvailableQtyToRelease(String.valueOf(Double.parseDouble(shopOrderRequest.getBuildQty()) - Double.parseDouble(shopOrderRequest.getProductionQuantities().get(0).getReleasedQty())));
            }shopOrder.setCreatedBy(existingShopOrder.getCreatedBy());
                shopOrder.setCreatedDateTime(existingShopOrder.getCreatedDateTime());
                shopOrder.setModifiedDateTime(LocalDateTime.now());
                shopOrder.setModifiedBy(shopOrderRequest.getUserId());

                ShopOrder shopOrderUpdate = shopOrderRepository.save(shopOrder);

            AuditLogRequest activityLog = AuditLogRequest.builder()
                    .site(shopOrderRequest.getSite())
                    .change_stamp("Update")
                    .action_code("SHOPORDER-UPDATE")
                    .action_detail("ShopOrder Updated "+shopOrderRequest.getShopOrder())
                    .action_detail_handle("ActionDetailBO:"+shopOrderRequest.getSite()+","+"SHOPORDER-UPDATE"+","+shopOrderRequest.getUserId()+":"+"com.rits.shoporderservice.service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(shopOrderRequest.getUserId())
                    .router(shopOrderRequest.getPlannedRouting())
                    .router_revision(shopOrderRequest.getRoutingVersion())
                    .item(shopOrderRequest.getPlannedMaterial())
                    .item_revision(shopOrderRequest.getMaterialVersion())
                    .txnId("SHOPORDER-UPDATE"+String.valueOf(LocalDateTime.now())+shopOrderRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("SHOP_ORDER")
                    .build();

            webClientBuilder.build()
                    .post()
                    .uri(auditlogUrl)
                    .bodyValue(activityLog)
                    .retrieve()
                    .bodyToMono(AuditLogRequest.class)
                    .block();
                String updateMessage = getFormattedMessage(2,shopOrderRequest.getShopOrder());
                return ShopOrderMessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).shopOrderResponse(shopOrderUpdate).build();

            } else {
                throw new ShopOrderException(3202, shopOrderRequest.getShopOrder());
            }
    }

    public void validations(ShopOrderRequest shopOrderRequest) throws Exception
    {
        if (shopOrderRequest.getPlannedBom() != null && !shopOrderRequest.getPlannedBom().isEmpty() && shopOrderRequest.getBomVersion()!=null && !shopOrderRequest.getBomVersion().isEmpty()) {
            Boolean bomExist = isBomExist(shopOrderRequest.getSite(),shopOrderRequest.getPlannedBom(),shopOrderRequest.getBomVersion());
            if (!bomExist) {
                throw new ShopOrderException(200, shopOrderRequest.getPlannedBom());
            }
        }
        if(shopOrderRequest.getPlannedBom() != null && !shopOrderRequest.getPlannedBom().isEmpty() && (shopOrderRequest.getBomVersion()==null || shopOrderRequest.getBomVersion().isEmpty())){
            throw new ShopOrderException(200,shopOrderRequest.getPlannedBom(),shopOrderRequest.getBomVersion());
        }
        if (shopOrderRequest.getPlannedMaterial() != null && !shopOrderRequest.getPlannedMaterial().isEmpty() && shopOrderRequest.getMaterialVersion()!=null && !shopOrderRequest.getMaterialVersion().isEmpty()) {
            Boolean itemExist = isItemExist(shopOrderRequest.getSite(),shopOrderRequest.getPlannedMaterial(),shopOrderRequest.getMaterialVersion());
            if (!itemExist) {
                throw new ShopOrderException(100, shopOrderRequest.getPlannedMaterial(),shopOrderRequest.getMaterialVersion());
            }
        }
        if(shopOrderRequest.getPlannedMaterial() != null && !shopOrderRequest.getPlannedMaterial().isEmpty() && (shopOrderRequest.getMaterialVersion()==null || shopOrderRequest.getMaterialVersion().isEmpty())){
            throw new ShopOrderException(100,shopOrderRequest.getPlannedMaterial(),shopOrderRequest.getMaterialVersion());
        }
        if (shopOrderRequest.getPlannedRouting() != null && !shopOrderRequest.getPlannedRouting().isEmpty() && shopOrderRequest.getPlannedRouting()!=null && !shopOrderRequest.getPlannedRouting().isEmpty()) {
            Boolean routingExist = isRoutingExist(shopOrderRequest.getSite(),shopOrderRequest.getPlannedRouting(),shopOrderRequest.getRoutingVersion());
            if (!routingExist) {
                throw new ShopOrderException(500, shopOrderRequest.getPlannedRouting());
            }
        }
        if(shopOrderRequest.getPlannedRouting() != null && !shopOrderRequest.getPlannedRouting().isEmpty() && (shopOrderRequest.getRoutingVersion()==null || shopOrderRequest.getRoutingVersion().isEmpty())){
            throw new ShopOrderException(500, shopOrderRequest.getPlannedRouting(),shopOrderRequest.getRoutingVersion());
        }
        if (shopOrderRequest.getPlannedWorkCenter() != null && !shopOrderRequest.getPlannedWorkCenter().isEmpty()) {
            Boolean workCenterExist = isWorkCenterExist(shopOrderRequest.getSite(),shopOrderRequest.getPlannedWorkCenter());
            if (!workCenterExist) {
                throw new ShopOrderException(600, shopOrderRequest.getPlannedWorkCenter());
            }
        }
        if(shopOrderRequest.getProductionQuantities().size()!=0) {
            if (shopOrderRequest.getProductionQuantities().get(0).getReleasedQty() == null || shopOrderRequest.getProductionQuantities().get(0).getReleasedQty().isEmpty()) {
                shopOrderRequest.getProductionQuantities().get(0).setReleasedQty("0");
            }
        }
        if(shopOrderRequest.getSerialPcu()!=null&& !shopOrderRequest.getSerialPcu().isEmpty()){
            for(SerialPcu serialPcu: shopOrderRequest.getSerialPcu())
            {
                if(serialPcu.getState().equals("New")) {
                    String pcuBO = "PcuBO:" + shopOrderRequest.getSite() + "," + serialPcu.getPcuNumber();
                    Boolean pcuHeaderResponse = isPcuHeaderExist(shopOrderRequest.getSite(),pcuBO);
                    if (pcuHeaderResponse != null && pcuHeaderResponse) {
                        throw new ShopOrderException(8, serialPcu.getPcuNumber());
                    }
                }
            }
        }
        if(shopOrderRequest.getPlannedStart()!=null && shopOrderRequest.getPlannedCompletion()!=null)
        {
            if (!shopOrderRequest.getPlannedStart().isBefore(shopOrderRequest.getPlannedCompletion())) {
                throw new ShopOrderException(3226);
            }
        }
        if(shopOrderRequest.getScheduledStart()!=null && shopOrderRequest.getScheduledEnd()!=null)
        {
            if (!shopOrderRequest.getScheduledStart().isBefore(shopOrderRequest.getScheduledEnd())) {
                throw new ShopOrderException(3227);
            }
        }
    }

    public String sampleNextNumber(String site,String parentOrder)throws Exception
    {
        NextNumber nextNumberRequest= NextNumber.builder().site(site).numberType("Shop Order").parentOrder(parentOrder).build();
        String nextNumberResponse = webClientBuilder.build()
                .post()
                .uri(nextNumberUrl)
                .bodyValue(nextNumberRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return nextNumberResponse;
    }
    public Boolean isBomExist(String site,String bom,String revision) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).bom(bom).revision(revision).build();
        Boolean bomExist = webClientBuilder.build()
                .post()
                .uri(bomUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return bomExist;
    }
    public Boolean isItemExist(String site,String item,String revision) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).item(item).revision(revision).build();
        Boolean itemExist = webClientBuilder.build()
                .post()
                .uri(itemUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return itemExist;
    }
    public Boolean isRoutingExist(String site,String routing,String version) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).routing(routing).version(version).build();
        Boolean routingExist = webClientBuilder.build()
                .post()
                .uri(routingUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return routingExist;
    }
    public Boolean isWorkCenterExist(String site,String workCenter) throws Exception
    {
        IsExist isExist = IsExist.builder().site(site).workCenter(workCenter).build();
        Boolean workCenterExist = webClientBuilder.build()
                .post()
                .uri(workCenterUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return workCenterExist;
    }
    public Boolean isPcuHeaderExist(String site,String pcuBO) throws Exception
    {
        PcuHeaderRequest pcuHeaderRequest = PcuHeaderRequest.builder().site(site).pcuBO(pcuBO).build();
        Boolean pcuHeaderResponse = webClientBuilder.build()
                .post()
                .uri(readPcuUrl)
                .bodyValue(pcuHeaderRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return pcuHeaderResponse;
    }

    public ShopOrder shopOrderBuilder(ShopOrderRequest shopOrderRequest)
    {
        ShopOrder shopOrder = ShopOrder.builder()
                .site(shopOrderRequest.getSite())
                .handle("ShopOrderBo:" + shopOrderRequest.getSite() + "," + shopOrderRequest.getShopOrder())
                .shopOrder(shopOrderRequest.getShopOrder())
                .status(shopOrderRequest.getStatus())
                .orderType(shopOrderRequest.getOrderType())
                .plannedMaterial(shopOrderRequest.getPlannedMaterial())
                .materialVersion(shopOrderRequest.getMaterialVersion())
                .bomType(shopOrderRequest.getBomType())
                .plannedBom(shopOrderRequest.getPlannedBom())
                .bomVersion(shopOrderRequest.getBomVersion())
                .plannedRouting(shopOrderRequest.getPlannedRouting())
                .routingVersion(shopOrderRequest.getRoutingVersion())
                .lcc(shopOrderRequest.getLcc())
                .plannedWorkCenter(shopOrderRequest.getPlannedWorkCenter())
                .priority(shopOrderRequest.getPriority())
                .orderedQty(shopOrderRequest.getOrderedQty())
                .buildQty(shopOrderRequest.getBuildQty())
                .erpUom(shopOrderRequest.getErpUom())
                .plannedStart(shopOrderRequest.getPlannedStart())
                .plannedCompletion(shopOrderRequest.getPlannedCompletion())
                .scheduledStart(shopOrderRequest.getScheduledStart())
                .scheduledEnd(shopOrderRequest.getScheduledEnd())
                .customerOrder(shopOrderRequest.getCustomerOrder())
                .customer(shopOrderRequest.getCustomer())
                .productionQuantities(shopOrderRequest.getProductionQuantities())
                .serialPcu(shopOrderRequest.getSerialPcu())
                .customDataList(shopOrderRequest.getCustomDataList())
                .autoRelease(shopOrderRequest.getAutoRelease())
                .parentOrder(shopOrderRequest.getParentOrder())
                .availableQtyToRelease(shopOrderRequest.getAvailableQtyToRelease())
                .inUse(shopOrderRequest.isInUse())
                .shopOrderBO("ShopOrderBO:"+shopOrderRequest.getSite()+","+shopOrderRequest.getShopOrder())
                .itemBO("ItemBO:"+shopOrderRequest.getSite()+","+shopOrderRequest.getPlannedMaterial()+","+shopOrderRequest.getMaterialVersion())
                .routingBO("RoutingBO:"+shopOrderRequest.getSite()+","+shopOrderRequest.getPlannedRouting()+","+shopOrderRequest.getRoutingVersion())
                .bomBO("BomBO:"+shopOrderRequest.getSite()+","+shopOrderRequest.getPlannedBom()+","+shopOrderRequest.getBomVersion())
                .workCenterBO("WorkCenterBO:"+shopOrderRequest.getSite()+","+shopOrderRequest.getPlannedWorkCenter())
                .active(1)
                .build();
        return shopOrder;
    }

    @Override
    public ShopOrderMessageModel saveSerialPu(ShopOrderRequest shopOrderRequest) throws Exception {

        ShopOrder existingShopOrder = shopOrderRepository.findBySiteAndActiveAndShopOrder(shopOrderRequest.getSite(), 1, shopOrderRequest.getShopOrder());

        List<SerialPcu> pcu = existingShopOrder.getSerialPcu();
        List<ShopOrder> shopOrders = new ArrayList<>();
        Map<String, Integer> pcuNumberList = shopOrderRequest.getPcuNumberList();
        for(String p: shopOrderRequest.getNewPcus()) {
            for (SerialPcu serialPcu : pcu) {
                String s= "PcuBO:" + shopOrderRequest.getSite() + "," +p;
                if (serialPcu.getPcuNumber().equals(p)) {
                    serialPcu.setPcuQuantity(String.valueOf(pcuNumberList.get(s)));
                    serialPcu.setState("Released");
                    serialPcu.setEnabled(true);
                    pcuNumberList.remove(s);
                    break;
                }
            }
        }
        List<SerialPcu> existingSerialPcus = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : pcuNumberList.entrySet()) {
            Integer value = entry.getValue();
            String key = entry.getKey();
            String[] pcuArray = key.split(",");

            SerialPcu newSerialPcu = new SerialPcu();
            newSerialPcu.setPcuNumber(pcuArray[1]);
            newSerialPcu.setPcuQuantity(String.valueOf(value));
            newSerialPcu.setState("Released");
            newSerialPcu.setEnabled(true);

            existingSerialPcus.add(newSerialPcu);
        }
        existingShopOrder.setAvailableQtyToRelease(String.valueOf(Integer.parseInt(existingShopOrder.getBuildQty()) - Double.parseDouble(shopOrderRequest.getProductionQuantities().get(0).getReleasedQty())));
        existingShopOrder.setProductionQuantities(shopOrderRequest.getProductionQuantities());
        existingShopOrder.setInUse(true);
        existingShopOrder.getSerialPcu().addAll(existingSerialPcus);
        shopOrders.add(existingShopOrder);

        String updateMessage = getFormattedMessage(2,shopOrderRequest.getShopOrder());
       return ShopOrderMessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).shopOrderResponses(shopOrderRepository.saveAll(shopOrders)).build();
    }


   @Override
   public Boolean isShopOrderExist(String site,String shopOrder) throws Exception
   {
      return shopOrderRepository.existsBySiteAndActiveAndShopOrder(site,1,shopOrder);
   }


    @Override
    public ShopOrder retrieveShopOrder(String site, String shopOrder) throws Exception {
        ShopOrder existingShopOrder = shopOrderRepository.findBySiteAndActiveAndShopOrder(site, 1, shopOrder);
        if (existingShopOrder == null) {
            throw new ShopOrderException(3202, shopOrder);
        }
        return existingShopOrder;
    }





    @Override
    public ShopOrderResponseList getAllShopOder(String site, String shopOrder) throws Exception {
        if (shopOrder != null && !shopOrder.isEmpty()) {
            List<ShopOrderResponse> shopOrderResponses = shopOrderRepository.findByActiveAndSiteAndShopOrderContainingIgnoreCase(1, site, shopOrder);
            if (shopOrderResponses.isEmpty()) {
                throw new ShopOrderException(3202, shopOrder);
            }
            return ShopOrderResponseList.builder().shopOrderResponseList(shopOrderResponses).build();
        }
        return getAllShopOrderByCreatedDate(site);
    }



    @Override
    public ShopOrderResponseList getAllShopOrderByCreatedDate(String site) throws Exception {
        List<ShopOrderResponse> shopOrderResponses = shopOrderRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return ShopOrderResponseList.builder().shopOrderResponseList(shopOrderResponses).build();
    }



    @Override
    public ShopOrderMessageModel deleteShopOrder(String site, String shopOrder, String userId) throws Exception {
        if (shopOrderRepository.existsBySiteAndActiveAndShopOrder(site, 1, shopOrder)) {
            ShopOrder existingShopOrder = shopOrderRepository.findBySiteAndActiveAndShopOrder(site, 1, shopOrder);
            if(!existingShopOrder.isInUse()) {
                existingShopOrder.setActive(0);
                existingShopOrder.setModifiedDateTime(LocalDateTime.now());
                existingShopOrder.setModifiedBy(userId);
                shopOrderRepository.save(existingShopOrder);
                AuditLogRequest activityLog = AuditLogRequest.builder()
                        .site(site)
                        .change_stamp("Update")
                        .action_code("SHOPORDER-UPDATE")
                        .action_detail("ShopOrder Updated " + shopOrder)
                        .action_detail_handle("ActionDetailBO:" + site + "," + "SHOPORDER-UPDATE" + "," + userId + ":" + "com.rits.shoporderservice.service")
                        .date_time(String.valueOf(LocalDateTime.now()))
                        .userId(userId)
                        .txnId("SHOPORDER-UPDATE" + String.valueOf(LocalDateTime.now()) + userId)
                        .created_date_time(String.valueOf(LocalDateTime.now()))
                        .category("SHOP_ORDER")
                        .build();

                webClientBuilder.build()
                        .post()
                        .uri(auditlogUrl)
                        .bodyValue(activityLog)
                        .retrieve()
                        .bodyToMono(AuditLogRequest.class)
                        .block();
                String deleteMessage = getFormattedMessage(3, shopOrder);
                return ShopOrderMessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
            }
            throw new ShopOrderException(3234);
        }
        throw new ShopOrderException(3202,shopOrder);
    }



//    @Override
//    public String callExtension(Extension extension) throws Exception {
//        String extensionResponse = webClientBuilder.build()
//                .post()
//                .uri(extensionUrl)
//                .bodyValue(extension)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        if (extensionResponse == null) {
//            throw new ShopOrderException(800);
//        }
//        return extensionResponse;
//    }




    @Override
    public List<ShopOrderList> findActiveShopOrdersByDate(String site, LocalDateTime plannedStart, LocalDateTime plannedCompletion) throws Exception {
        int active = 1;
        String status = "active";
        List<ShopOrderList> activeShopOrders;
        if (plannedStart != null) {
            activeShopOrders = shopOrderRepository.findByActiveAndSiteAndStatusIgnoreCaseAndPlannedStartAfter(active, site, status, plannedStart);
            if(activeShopOrders==null || activeShopOrders.isEmpty()){
                throw new ShopOrderException(3205);
            }
        } else if (plannedCompletion != null) {
            activeShopOrders = shopOrderRepository.findByActiveAndSiteAndStatusIgnoreCaseAndPlannedCompletionAfter(active, site, status, plannedCompletion);
            if(activeShopOrders==null || activeShopOrders.isEmpty()){
                throw new ShopOrderException(3205);
            }
        }
        else{
            throw new ShopOrderException(3203);
        }
        return activeShopOrders;
    }


    @Override
    public List<ShopOrder> findShopOrderPcuInWork(String site) throws Exception {
        List<ShopOrder> shopOrderList = shopOrderRepository.findByActiveAndSiteAndInUse(1, site,true);
        if(shopOrderList==null ||shopOrderList.isEmpty()){
            throw new ShopOrderException(3208);
        }
        return shopOrderList;
    }

    @Override
    public List<ShopOrder> getShopOrderByMaterial(ShopOrderRequest shopOrderRequest) throws Exception {
        List<ShopOrder> shopOrderList = shopOrderRepository.findByActiveAndSiteAndPlannedMaterialAndMaterialVersion(1, shopOrderRequest.getSite(),shopOrderRequest.getPlannedMaterial(), shopOrderRequest.getMaterialVersion());
        if(shopOrderList != null || !shopOrderList.isEmpty()){
            return shopOrderList;
        }
        throw new ShopOrderException(6);
    }


    @Override
    public List<SerialPcu> getSerialNumberList(String site, String shopOrder) throws Exception {
        ShopOrder existingShopOrder = shopOrderRepository.findBySiteAndActiveAndShopOrder(site, 1,shopOrder);
        if(existingShopOrder==null){
            throw new ShopOrderException(3202,shopOrder);
        }
        return existingShopOrder.getSerialPcu();
    }



    @Override
    public List<SerialPcu> updateSerialNumberList(String site, String shopOrder, List<String> serialNumberList) throws Exception {
        ShopOrder existingShopOrder = shopOrderRepository.findBySiteAndActiveAndShopOrder(site, 1, shopOrder);



        if (existingShopOrder == null) {
            throw new ShopOrderException(3202, shopOrder);
        }
        if (existingShopOrder.getSerialPcu().isEmpty()) {
            throw new ShopOrderException(3206);
        }
        List<SerialPcu> serialPcuToRemove = new ArrayList<>();
        for (SerialPcu serialPcu : existingShopOrder.getSerialPcu()) {
            if (serialNumberList.contains(serialPcu.getSerialNumber())) {
                if (!serialPcu.getState().equalsIgnoreCase("created")) {
                    throw new ShopOrderException(3207, serialPcu.getSerialNumber(), serialPcu.getState());
                } else {
                    serialPcuToRemove.add(serialPcu);
                }
            }
        }
        existingShopOrder.getSerialPcu().removeAll(serialPcuToRemove);
        shopOrderRepository.save(existingShopOrder);
        return existingShopOrder.getSerialPcu();
    }



    @Override
    public ShopOrder retrieveShopOrderListUsingSFCNumber(String site, String sfcNumber) throws Exception {
        ShopOrder shopOrderList=shopOrderRepository.findByActiveAndSiteAndSerialPcu_PcuNumber(1,site,sfcNumber);
        return shopOrderList;
    }
    @Override
    public List<ShopOrder> getShopOrdersByCriteria(
            String site,
            String shopOrder,
            String orderType,
            String routing ,
            String routingVersion,
            String material,
            String materialVersion ,
            LocalDateTime plannedStart,
            LocalDateTime plannedCompletion,
            String workCenter) throws Exception {



        Query query = new Query();
        if (site != null && !site.isEmpty()) {
            query.addCriteria(Criteria.where("site").is(site));
        }



        if (shopOrder != null && !shopOrder.isEmpty()) {
            query.addCriteria(Criteria.where("shopOrder").is(shopOrder));
        }



        if (orderType != null && !orderType.isEmpty()) {
            query.addCriteria(Criteria.where("orderType").is(orderType));
        }



        if (material != null && !material.isEmpty()) {
            query.addCriteria(Criteria.where("plannedMaterial").is(material));



            if (materialVersion != null && !materialVersion.isEmpty()) {
                query.addCriteria(Criteria.where("materialVersion").is(materialVersion));
            }
        }
        if (routing != null && !routing.isEmpty()) {
            query.addCriteria(Criteria.where("plannedRouting").is(routing));



            if (routingVersion != null && !routingVersion.isEmpty()) {
                query.addCriteria(Criteria.where("routingVersion").is(routingVersion));
            }
        }



        if (plannedStart != null) {
            query.addCriteria(Criteria.where("plannedStart").gte(plannedStart));
        }



        if (plannedCompletion != null) {
            query.addCriteria(Criteria.where("plannedCompletion").lte(plannedCompletion));
        }



        if (workCenter != null && !workCenter.isEmpty()) {
            query.addCriteria(Criteria.where("plannedWorkCenter").is(workCenter));
        }



        List<ShopOrder> shopOrderList = mongoTemplate.find(query, ShopOrder.class);
        return shopOrderList;
    }





}