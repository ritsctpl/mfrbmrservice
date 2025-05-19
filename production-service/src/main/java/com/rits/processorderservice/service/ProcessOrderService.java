package com.rits.processorderservice.service;

import com.rits.processorderservice.dto.ProcessOrderList;
import com.rits.processorderservice.dto.ProcessOrderRequest;
import com.rits.processorderservice.dto.ProcessOrderResponse;
import com.rits.processorderservice.dto.ProcessOrderResponseList;
import com.rits.processorderservice.model.ProcessOrder;
import com.rits.processorderservice.model.ProcessOrderMessageModel;
import com.rits.processorderservice.model.BatchNumber;



import java.time.LocalDateTime;
import java.util.List;

public interface ProcessOrderService {
    public ProcessOrderMessageModel createProcessOrder(ProcessOrderRequest processOrderRequest) throws Exception;

    public ProcessOrderMessageModel updateProcessOrder(ProcessOrderRequest processOrderRequest) throws Exception;

    ProcessOrderMessageModel saveBatchNumber(ProcessOrderRequest processOrderRequest) throws Exception;

    Boolean isProcessOrderExist(String site, String orderNumber) throws Exception;

   public ProcessOrder retrieveProcessOrder(String site, String orderNumber) throws Exception;

    ProcessOrderResponseList getAllProcessOrder(String site, String orderNumber) throws Exception;
    ProcessOrderResponseList getAllProessOrders(String site, String orderNumber) throws Exception;

    public ProcessOrderResponseList getAllProcessOrderByCreatedDate(String site) throws Exception;
    public ProcessOrderResponseList retrieveTop50OrderNos(String site) throws Exception;

   public ProcessOrderMessageModel deleteProcessOrder(String site, String orderNumber, String userId) throws Exception;

  //  public String callExtension(Extension extension) throws Exception;

    public List<ProcessOrderList> findActiveProcessOrdersByDate(String site, LocalDateTime productionStartDate, LocalDateTime productionFinishDate)throws Exception;

    List<BatchNumber> updateBnoList(String site, String orderNumber, List<String> bnoList) throws Exception;

       public List<ProcessOrder> findProcessOrderBnoInWork(String site) throws Exception;
    public List<ProcessOrder> getProcessOrdersByMaterial(ProcessOrderRequest processOrderRequest) throws Exception;

    ProcessOrder retrieveProcessOrderListUsingBno(String site, String bnoNumber) throws Exception;

    public List<BatchNumber> getBnoList(String site, String orderNumber) throws Exception;

    ProcessOrderResponse getBySiteAndOrderNumber(String site, String orderNumber);

    List<ProcessOrder> getProcessOrdersByCriteria(
            String site,
            String orderNumber,
            String orderType,
            String recipeNo,
            String recipeVersion,
            String material,
            String materialVersion,
            LocalDateTime productionStartDate,
            LocalDateTime productionFinishDate,
            String workCenter) throws Exception;
    //  public List<BatchNumber> updateSerialNumberList(String site, String processOrder, List<String> serialNumberList) throws Exception;
//    public ProcessOrder retrieveProcessOrderListUsingSFCNumber(String site,String sfcNumber) throws Exception;
//    public List<ProcessOrder> getProcessOrdersByCriteria(String site,
//            String processOrder,
//            String orderType,
//            String routing,
//            String routingVersion,
//            String material,
//            String materialVersion,
//            LocalDateTime plannedStartAfter,
//            LocalDateTime plannedStartBefore,
//            String workcenter) throws Exception;

}
