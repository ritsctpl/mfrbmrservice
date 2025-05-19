package com.rits.startservice.repository;

import com.rits.pcuinqueueservice.model.PcuInQueue;
import com.rits.startservice.dto.RetrieveAllPcuList;
import com.rits.startservice.model.PcuInWork;
import com.rits.startservice.model.Start;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface StartRepository extends MongoRepository<PcuInWork,String> {

    PcuInWork findByActiveAndSiteAndPcuBOAndOperationBO(int i, String site, String pcuBo, String operation);

    PcuInWork findByActiveAndSiteAndPcuBOAndOperationBOAndItemBO(int i, String site, String pcuBo, String operation, String item);

    PcuInWork findByActiveAndSiteAndPcuBOAndOperationBOAndResourceBO(int i, String site, String pcuBo, String operation, String resource);

    boolean existsByActiveAndSiteAndPcuBOAndOperationBO(int active, String site, String pcuBO, String operationBO);

    List<PcuInWork> findByActiveAndSiteAndOperationBOAndResourceBO(int i, String site, String operationBO, String resourceBO);

    List<PcuInWork> findByActiveAndSiteAndOperationBO(int i, String site, String operationBO, Pageable pageable);

    List<PcuInWork> findByActiveAndSite(int i, String site);

    List<PcuInWork> findByActiveAndSiteAndPcuBO(int i, String site, String pcu);

    PcuInWork findByActiveAndSiteAndPcuBOAndOperationBOContaining(int i, String site, String pcuBo, String operation);

    List<PcuInWork> findByActiveAndPcuBOAndRouterBO(int active, String pcuBO, String routerBO, String site);
}
