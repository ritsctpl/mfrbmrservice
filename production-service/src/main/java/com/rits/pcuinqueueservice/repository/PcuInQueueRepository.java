package com.rits.pcuinqueueservice.repository;

import com.rits.pcuinqueueservice.dto.PcuInQueueResponse;
import com.rits.pcuinqueueservice.model.PcuInQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PcuInQueueRepository  extends MongoRepository<PcuInQueue,String> {
    PcuInQueue findByActiveAndSiteAndPcuBOAndOperationBO(int active, String site, String pcuBo, String operation);

    PcuInQueue findByActiveAndSiteAndPcuBOAndOperationBOContaining(int active, String site, String pcuBo, String operation);

    PcuInQueue findByActiveAndSiteAndPcuBOAndOperationBOAndItemBO(int active, String site, String pcuBo, String operation, String item);

    PcuInQueue findByActiveAndSiteAndPcuBOAndOperationBOAndResourceBO(int active, String site, String pcuBo, String operation, String resource);

    boolean existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(int active, String site, String pcuBO, String operationBO, String shopOrderBO, String resourceBO);

    PcuInQueue findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(int active, String site, String pcuBO, String operationBO, String shopOrderBO, String resourceBO);

    List<PcuInQueue> findByActiveAndSiteAndOperationBOAndResourceBO(int active, String site, String operation, String resource, Pageable pageable);

    List<PcuInQueue> findByActiveAndSiteAndOperationBOAndResourceBOAndPcuBO(int active, String site, String operation, String resource, String pcuBO, Pageable pageable);

    List<PcuInQueue> findByActiveAndSiteAndOperationBO(int active, String site, String operation, Pageable pageable);

    PcuInQueue findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBO(int active, String site, String pcuBO, String operationBO, String shopOrderBO);

    boolean existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBO(int active, String site, String pcuBO, String operationBO, String shopOrderBO);

    List<PcuInQueue> findByActiveAndSiteAndPcuBO(int i, String site, String pcu);
    List<PcuInQueue> findByActiveAndPcuBOAndRouterBO(int active, String pcuBO, String routerBO);

    List<PcuInQueue> findByActiveAndSiteAndOperationBOAndShopOrderBO(int active, String site, String operation, String shopOrderBO);

    List<PcuInQueue> findByActiveAndSite(int i, String site);
}
