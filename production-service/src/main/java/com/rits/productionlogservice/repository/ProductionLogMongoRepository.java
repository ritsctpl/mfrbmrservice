package com.rits.productionlogservice.repository;

import com.rits.productionlogservice.dto.ActualCycleSum;
import com.rits.productionlogservice.model.ProductionLogMongo;
import org.springframework.data.mongodb.core.MongoAdminOperations;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductionLogMongoRepository extends MongoRepository<ProductionLogMongo,String> {
    ProductionLogMongo findTop1ByPcuAndOperationAndOperationVersionAndShoporderBoAndEventTypeOrderByCreatedDatetimeDesc(String pcu, String operation, String operationVersion, String shopOrder, String eventType);

    ProductionLogMongo findTop1ByPcuAndShoporderBoAndEventTypeAndSiteAndActiveOrderByCreatedDatetime(String pcu, String shopOrder, String eventType, String site, int active);

    @Aggregation("[{ $match: { pcuBO: ?0, shoporderBo: ?1, operationBO: ?2, eventType: ?3 } }, { $project: { actualCycleTimeDouble: { $toLong: '$actualCycleTime' } } }, { $group: { _id: null, totalActualCycleTime: { $sum: '$actualCycleTimeDouble' } } }]")
    List<ActualCycleSum> getActualCycleTimeSum(String pcu, String shopOrder, String operation, String eventType);

    List<ProductionLogMongo> findBySiteAndPcuAndEventType(String site, String pcu, String workInstruction);
    ProductionLogMongo findTop1BySiteAndResourceIdAndEventTypeOrderByCreatedDatetimeDesc(String site, String resource, String eventType);

}
