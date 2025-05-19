package com.rits.logbuyoffservice.repository;

import com.rits.logbuyoffservice.model.BuyoffLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LogBuyOffRepository extends MongoRepository<BuyoffLog, String> {
    List<BuyoffLog> findByActiveAndSiteAndBuyOffBO(int i, String site, String handle);

    List<BuyoffLog> findByActiveAndSiteAndBatchNo(int i, String site, String batchNo);

    List<BuyoffLog> findByActiveAndSiteAndBatchNoAndBuyOffBO(int active, String site, String batchNo, String buyOffBO, String userId);

    BuyoffLog findByActiveAndSiteAndBatchNoAndOrderNumberAndOperationAndStateIgnoreCase(int i, String site, String batchNo, String orderNumber, String operation, String closed);

    boolean existsBySiteAndBatchNoAndBuyOffActionAndActive(String site, String batchNo, String r, int active);

    List<BuyoffLog> findBySiteAndBatchNoAndBuyOffActionAndActive(String site, String batchNo, String r, int i);
}
