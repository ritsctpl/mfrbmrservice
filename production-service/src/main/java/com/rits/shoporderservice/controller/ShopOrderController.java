package com.rits.shoporderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.shoporderservice.dto.*;
import com.rits.shoporderservice.exception.ShopOrderException;
import com.rits.shoporderservice.model.ShopOrderMessageModel;
import com.rits.shoporderservice.model.SerialPcu;
import com.rits.shoporderservice.model.ShopOrder;
import com.rits.shoporderservice.service.ShopOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/shoporder-service")
public class ShopOrderController {
    private final ShopOrderService shopOrderService;
    private final ObjectMapper objectMapper;


    @PostMapping("create")
    public ResponseEntity<?> createShopOrder(@RequestBody ShopOrderRequest shopOrderRequest) throws Exception {
        ShopOrderMessageModel createShopOrder;

            try {
                createShopOrder = shopOrderService.createShopOrder(shopOrderRequest);

                return ResponseEntity.ok( ShopOrderMessageModel.builder().message_details(createShopOrder.getMessage_details()).shopOrderResponse(createShopOrder.getShopOrderResponse()).build());

            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    @PostMapping("update")
    public ResponseEntity<ShopOrderMessageModel> updateShopOrder(@RequestBody ShopOrderRequest shopOrderRequest) throws Exception {
        ShopOrderMessageModel updateShopOrder;

            try {
                updateShopOrder = shopOrderService.updateShopOrder(shopOrderRequest);

                return ResponseEntity.ok( ShopOrderMessageModel.builder().message_details(updateShopOrder.getMessage_details()).shopOrderResponse(updateShopOrder.getShopOrderResponse()).build());
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @PostMapping("saveSerialPCu")
        public ShopOrderMessageModel saveSerialPu(@RequestBody ShopOrderRequest shopOrderRequest)
        {
            try {
              return  shopOrderService.saveSerialPu(shopOrderRequest);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    @PostMapping("delete")
    public ResponseEntity<ShopOrderMessageModel> deleteShopOrder(@RequestBody ShopOrderRequest shopOrderRequest) throws Exception {
        ShopOrderMessageModel deleteResponse;
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {

            try {
                deleteResponse = shopOrderService.deleteShopOrder(shopOrderRequest.getSite(), shopOrderRequest.getShopOrder(),shopOrderRequest.getUserId());

                return ResponseEntity.ok(deleteResponse);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

    @PostMapping("retrieve")
    public ResponseEntity<ShopOrder> retrieveShopOrder(@RequestBody ShopOrderRequest shopOrderRequest) throws Exception {
        ShopOrder retrieveShopOrder;
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {

            try {
                retrieveShopOrder = shopOrderService.retrieveShopOrder(shopOrderRequest.getSite(), shopOrderRequest.getShopOrder());

                return ResponseEntity.ok(retrieveShopOrder);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

    @PostMapping("retrieveAll")
    public ResponseEntity<ShopOrderResponseList> getShopOrderList(@RequestBody ShopOrderRequest shopOrderRequest) {
        ShopOrderResponseList retrieveAllShopOrder;
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {
            try {
                retrieveAllShopOrder = shopOrderService.getAllShopOder(shopOrderRequest.getSite(), shopOrderRequest.getShopOrder());
                return ResponseEntity.ok(retrieveAllShopOrder);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

    @PostMapping("retrieveTop50")
    public ResponseEntity<ShopOrderResponseList> getShopOrderListByCreationDate(@RequestBody ShopOrderRequest shopOrderRequest) {
        ShopOrderResponseList retrieveTop50ShopOrder;
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {
            try {
                retrieveTop50ShopOrder = shopOrderService.getAllShopOrderByCreatedDate(shopOrderRequest.getSite());
                return ResponseEntity.ok(retrieveTop50ShopOrder);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

    @PostMapping("findActiveShopOrdersByDate")
    public ResponseEntity< List<ShopOrderList>> findActiveShopOrdersByDate(@RequestBody ShopOrderRequest shopOrderRequest) {
        List<ShopOrderList> findActiveShopOrdersByDate;
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {

            try {
                findActiveShopOrdersByDate = shopOrderService.findActiveShopOrdersByDate(shopOrderRequest.getSite(), shopOrderRequest.getPlannedStart(),shopOrderRequest.getPlannedCompletion());
                return ResponseEntity.ok(findActiveShopOrdersByDate);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

    @PostMapping("retrieveByPcu")
    public ShopOrder retrieveShopOrderListUsingSFCNumber(@RequestBody ShopOrderRequest shopOrderRequest) {
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {
            try {
                ShopOrder retrieveByPcu = shopOrderService.retrieveShopOrderListUsingSFCNumber(shopOrderRequest.getSite(),shopOrderRequest.getPcu());
                return retrieveByPcu;
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

    @PostMapping("getSerialNumberList")
    public ResponseEntity< List<SerialPcu>> getSerialNumberList(@RequestBody ShopOrderRequest shopOrderRequest) {
        List<SerialPcu> getSerialNumberList;
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {
            try {
                getSerialNumberList = shopOrderService.getSerialNumberList(shopOrderRequest.getSite(), shopOrderRequest.getShopOrder());
                return ResponseEntity.ok(getSerialNumberList);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }
    @PostMapping("isExist")
    public Boolean isShopOrderExists(@RequestBody ShopOrderRequest shopOrderRequest)
    {
        try {
            return  shopOrderService.isShopOrderExist(shopOrderRequest.getSite(),shopOrderRequest.getShopOrder());
        } catch (ShopOrderException shopOrderException) {
            throw shopOrderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("updateSerialNumberList")
    public ResponseEntity< List<SerialPcu>> updateSerialNumberList(@RequestBody SerialNumberRequest serialNumberRequest) {
        List<SerialPcu> updateSerialNumberList;
        if (serialNumberRequest.getSite() != null && !serialNumberRequest.getSite().isEmpty()) {
            try {
                updateSerialNumberList = shopOrderService.updateSerialNumberList(serialNumberRequest.getSite(), serialNumberRequest.getShopOrder(),serialNumberRequest.getSerialNumberList());
                return ResponseEntity.ok(updateSerialNumberList);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

    @PostMapping("findShopOrderPcuInWork")
    public ResponseEntity< List<ShopOrder>> findShopOrderPcuInWork(@RequestBody SerialNumberRequest serialNumberRequest) {
        List<ShopOrder> findShopOrderPcuInWork;
        if (serialNumberRequest.getSite() != null && !serialNumberRequest.getSite().isEmpty()) {
            try {
                findShopOrderPcuInWork = shopOrderService.findShopOrderPcuInWork(serialNumberRequest.getSite());
                return ResponseEntity.ok(findShopOrderPcuInWork);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }
    @PostMapping("getShopOrdersByCriteria")
    public ResponseEntity< List<ShopOrder>> getShopOrdersByCriteria(@RequestBody ShopOrderRequest shopOrderRequest) {
        List<ShopOrder> getShopOrdersByCriteria;
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {
            try {
                getShopOrdersByCriteria = shopOrderService.getShopOrdersByCriteria(shopOrderRequest.getSite(),shopOrderRequest.getShopOrder(), shopOrderRequest.getOrderType(),shopOrderRequest.getPlannedRouting(),shopOrderRequest.getRoutingVersion(), shopOrderRequest.getPlannedMaterial(), shopOrderRequest.getMaterialVersion(), shopOrderRequest.getPlannedStart(),shopOrderRequest.getPlannedCompletion(), shopOrderRequest.getPlannedWorkCenter());
                return ResponseEntity.ok(getShopOrdersByCriteria);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

    @PostMapping("getShopOrderByMaterial")
    public ResponseEntity< List<ShopOrder>> getShopOrderByMaterial(@RequestBody ShopOrderRequest shopOrderRequest) {
        if (shopOrderRequest.getSite() != null && !shopOrderRequest.getSite().isEmpty()) {
            try {
                List<ShopOrder> getShopOrdersByMaterial = shopOrderService.getShopOrderByMaterial(shopOrderRequest);
                return ResponseEntity.ok(getShopOrdersByMaterial);
            } catch (ShopOrderException shopOrderException) {
                throw shopOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShopOrderException(1);
    }

}