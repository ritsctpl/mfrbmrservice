package com.rits.pcucompleteservice.repository;

import com.rits.pcucompleteservice.model.PcuComplete;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PcuCompleteRepository extends MongoRepository<PcuComplete,String> {

    

    boolean existsByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(int i, String site, String pcuBO, String operationBO, String shopOrderBO, String resourceBO);

    PcuComplete findByActiveAndSiteAndPcuBOAndOperationBOAndShopOrderBOAndResourceBO(int i, String site, String pcuBO, String operationBO, String shopOrderBO, String resourceBO);

    boolean deleteByHandle(String handle);


    List<PcuComplete> findByActiveAndSiteAndPcuBO(int i, String site, String pcuBO);

    List<PcuComplete> findByActiveAndSite(int i, String site);

    PcuComplete findByActiveAndSiteAndPcuBOAndOperationBO(int i, String site, String pcuBO, String operationBO);

    List<PcuComplete> findByActiveAndSiteAndOperationBO(int i, String site, String operationBO);

    List<PcuComplete> findByActiveAndSiteAndOperationBOAndShopOrderBO(int i, String site, String operationBO, String shopOrderBO);
}
