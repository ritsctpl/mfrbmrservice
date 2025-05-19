package com.rits.shoporderservice.service;

import com.rits.shoporderservice.dto.*;
import com.rits.shoporderservice.model.ShopOrderMessageModel;
import com.rits.shoporderservice.model.SerialPcu;
import com.rits.shoporderservice.model.ShopOrder;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

public interface ShopOrderService {
    public ShopOrderMessageModel createShopOrder(ShopOrderRequest shopOrderRequest) throws Exception;

    public ShopOrderMessageModel updateShopOrder(ShopOrderRequest shopOrderRequest) throws Exception;

    ShopOrderMessageModel saveSerialPu(ShopOrderRequest shopOrderRequest) throws Exception;

    Boolean isShopOrderExist(String site, String shopOrder) throws Exception;

    public ShopOrder retrieveShopOrder(String site, String shopOrder) throws Exception;

    public ShopOrderResponseList getAllShopOder(String site, String shopOrder) throws Exception;

    public ShopOrderResponseList getAllShopOrderByCreatedDate(String site) throws Exception;

    public ShopOrderMessageModel deleteShopOrder(String site, String shopOrder, String userId) throws Exception;

   // public String callExtension(Extension extension) throws Exception;

    public List<ShopOrderList> findActiveShopOrdersByDate(String site, LocalDateTime plannedStart, LocalDateTime plannedCompletion)throws Exception;

    public List<ShopOrder> findShopOrderPcuInWork(String site) throws Exception;
    public List<ShopOrder> getShopOrderByMaterial(ShopOrderRequest shopOrderRequest) throws Exception;
    public List<SerialPcu> getSerialNumberList(String site, String shopOrder) throws Exception;
    public List<SerialPcu> updateSerialNumberList(String site, String shopOrder, List<String> serialNumberList) throws Exception;
    public ShopOrder retrieveShopOrderListUsingSFCNumber(String site,String sfcNumber) throws Exception;
    public List<ShopOrder> getShopOrdersByCriteria(String site,
            String shopOrder,
            String orderType,
            String routing,
            String routingVersion,
            String material,
            String materialVersion,
            LocalDateTime plannedStartAfter,
            LocalDateTime plannedStartBefore,
            String workcenter) throws Exception;

}
