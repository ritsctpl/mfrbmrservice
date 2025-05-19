package com.rits.processorderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.processorderservice.dto.ProcessOrderList;
import com.rits.processorderservice.dto.ProcessOrderRequest;
import com.rits.processorderservice.dto.ProcessOrderResponseList;
import com.rits.processorderservice.exception.ProcessOrderException;
import com.rits.processorderservice.model.ProcessOrderMessageModel;
import com.rits.processorderservice.model.BatchNumber;
import com.rits.processorderservice.service.ProcessOrderService;
import com.rits.processorderservice.dto.SerialNumberRequest;
import com.rits.processorderservice.model.ProcessOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/processorder-service")
public class ProcessOrderController {
    private final ProcessOrderService processOrderService;
    private final ObjectMapper objectMapper;


    @PostMapping("create")
    public ResponseEntity<?> createProcessOrder(@RequestBody ProcessOrderRequest processOrderRequest) throws Exception {
        ProcessOrderMessageModel createProcessOrder;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                createProcessOrder = processOrderService.createProcessOrder(processOrderRequest);
                return ResponseEntity.ok(ProcessOrderMessageModel.builder().message_details(createProcessOrder.getMessage_details()).processOrderResponse(createProcessOrder.getProcessOrderResponse()).build());
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("update")
    public ResponseEntity<ProcessOrderMessageModel> updateProcessOrder(@RequestBody ProcessOrderRequest processOrderRequest) throws Exception {
        ProcessOrderMessageModel updateProcessOrder;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                updateProcessOrder = processOrderService.updateProcessOrder(processOrderRequest);
                return ResponseEntity.ok(ProcessOrderMessageModel.builder().message_details(updateProcessOrder.getMessage_details()).processOrderResponse(updateProcessOrder.getProcessOrderResponse()).build());
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
            throw new ProcessOrderException(1);
        }



    @PostMapping("delete")
    public ResponseEntity<ProcessOrderMessageModel> deleteProcessOrder(@RequestBody ProcessOrderRequest processOrderRequest) throws Exception {
        ProcessOrderMessageModel deleteProcessOrder;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                deleteProcessOrder = processOrderService.deleteProcessOrder(processOrderRequest.getSite(),processOrderRequest.getOrderNumber(), processOrderRequest.getUserId());
                return ResponseEntity.ok(deleteProcessOrder);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }


    @PostMapping("retrieve")
    public ResponseEntity<ProcessOrder> retrieveProcessOrder(@RequestBody ProcessOrderRequest processOrderRequest) throws Exception {
        ProcessOrder retrieveProcessOrder;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {

            try {
                retrieveProcessOrder = processOrderService.retrieveProcessOrder(processOrderRequest.getSite(), processOrderRequest.getOrderNumber());

                return ResponseEntity.ok(retrieveProcessOrder);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("retrieveAll")
    public ResponseEntity<ProcessOrderResponseList> getAllProcessOrder(@RequestBody ProcessOrderRequest processOrderRequest) {
        ProcessOrderResponseList retrieveAllProcessOrder;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                retrieveAllProcessOrder = processOrderService.getAllProcessOrder(processOrderRequest.getSite(),processOrderRequest.getOrderNumber());
                return ResponseEntity.ok(retrieveAllProcessOrder);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("getAllProessOrders")
    public ResponseEntity<ProcessOrderResponseList> getAllProessOrders(@RequestBody ProcessOrderRequest processOrderRequest) {
        ProcessOrderResponseList retrieveAllProcessOrder;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                retrieveAllProcessOrder = processOrderService.getAllProessOrders(processOrderRequest.getSite(),processOrderRequest.getOrderNumber());
                return ResponseEntity.ok(retrieveAllProcessOrder);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }


    @PostMapping("retrieveTop50")
    public ResponseEntity<ProcessOrderResponseList> getAllProcessOrderByCreatedDate(@RequestBody ProcessOrderRequest processOrderRequest) {
        ProcessOrderResponseList retrieveTop50ProcessOrder;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                retrieveTop50ProcessOrder = processOrderService.getAllProcessOrderByCreatedDate(processOrderRequest.getSite());
                return ResponseEntity.ok(retrieveTop50ProcessOrder);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("retrieveTop50OrderNos")
    public ResponseEntity<ProcessOrderResponseList> retrieveTop50OrderNos(@RequestBody ProcessOrderRequest processOrderRequest) {
        ProcessOrderResponseList retrieveTop50ProcessOrder;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                retrieveTop50ProcessOrder = processOrderService.retrieveTop50OrderNos(processOrderRequest.getSite());
                return ResponseEntity.ok(retrieveTop50ProcessOrder);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }


    @PostMapping("saveBatchNumber")
        public ProcessOrderMessageModel saveBatchNumber(@RequestBody ProcessOrderRequest processOrderRequest)
        {
            try {
              return  processOrderService.saveBatchNumber(processOrderRequest);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }



    @PostMapping("findActiveProcessOrdersByDate")
    public ResponseEntity< List<ProcessOrderList>> findActiveProcessOrdersByDate(@RequestBody ProcessOrderRequest processOrderRequest) {
        List<ProcessOrderList> findActiveProcessOrdersByDate;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                findActiveProcessOrdersByDate = processOrderService.findActiveProcessOrdersByDate(processOrderRequest.getSite(), processOrderRequest.getProductionStartDate(),processOrderRequest.getProductionFinishDate());
                return ResponseEntity.ok(findActiveProcessOrdersByDate);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("retrieveByBatchNumber")
    public ProcessOrder retrieveProcessOrderListUsingBno(@RequestBody ProcessOrderRequest processOrderRequest) {
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                ProcessOrder retrieveByBno = processOrderService.retrieveProcessOrderListUsingBno(processOrderRequest.getSite(),processOrderRequest.getBatch());
                return retrieveByBno;
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("getBnoList")
    public ResponseEntity< List<BatchNumber>> getBnoList(@RequestBody ProcessOrderRequest processOrderRequest) {
        List<BatchNumber> getSerialNumberList;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                getSerialNumberList = processOrderService.getBnoList(processOrderRequest.getSite(), processOrderRequest.getOrderNumber());
                return ResponseEntity.ok(getSerialNumberList);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("isExist")
    public Boolean isProcessOrderExists(@RequestBody ProcessOrderRequest processOrderRequest)
    {
        try {
            return  processOrderService.isProcessOrderExist(processOrderRequest.getSite(),processOrderRequest.getOrderNumber());
        } catch (ProcessOrderException processOrderException) {
            throw processOrderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("updateBnoList")
    public ResponseEntity< List<BatchNumber>> updateBnoList(@RequestBody SerialNumberRequest serialNumberRequest) {
        List<BatchNumber> updateBnoList;
        if (serialNumberRequest.getSite() != null && !serialNumberRequest.getSite().isEmpty()) {
            try {
                updateBnoList = processOrderService.updateBnoList(serialNumberRequest.getSite(), serialNumberRequest.getOrderNumber(),serialNumberRequest.getBnoList());
                return ResponseEntity.ok(updateBnoList);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("findProcessOrderBnoInWork")
    public ResponseEntity< List<ProcessOrder>> findProcessOrderBnoInWork(@RequestBody SerialNumberRequest serialNumberRequest) {
        List<ProcessOrder> findProcessOrderBnoInWork;
        if (serialNumberRequest.getSite() != null && !serialNumberRequest.getSite().isEmpty()) {
            try {
                findProcessOrderBnoInWork = processOrderService.findProcessOrderBnoInWork(serialNumberRequest.getSite());
                return ResponseEntity.ok(findProcessOrderBnoInWork);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("getProcessOrdersByCriteria")//hold
    public ResponseEntity< List<ProcessOrder>> getProcessOrdersByCriteria(@RequestBody ProcessOrderRequest processOrderRequest) {
        List<ProcessOrder> getProcessOrdersByCriteria;
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                getProcessOrdersByCriteria = processOrderService.getProcessOrdersByCriteria(processOrderRequest.getSite(),processOrderRequest.getOrderNumber(), processOrderRequest.getOrderType(),processOrderRequest.getRecipe(),processOrderRequest.getRecipeVersion(), processOrderRequest.getMaterial(), processOrderRequest.getMaterialVersion(), processOrderRequest.getProductionStartDate(),processOrderRequest.getProductionFinishDate(), processOrderRequest.getWorkCenter());
                return ResponseEntity.ok(getProcessOrdersByCriteria);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

    @PostMapping("getProcessOrdersByMaterial")
    public ResponseEntity< List<ProcessOrder>> getProcessOrdersByMaterial(@RequestBody ProcessOrderRequest processOrderRequest) {
        if (processOrderRequest.getSite() != null && !processOrderRequest.getSite().isEmpty()) {
            try {
                List<ProcessOrder> getProcessOrderByMaterial = processOrderService.getProcessOrdersByMaterial(processOrderRequest);
                return ResponseEntity.ok(getProcessOrderByMaterial);
            } catch (ProcessOrderException processOrderException) {
                throw processOrderException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessOrderException(1);
    }

}