package com.rits.operationservice.repository;

import com.rits.operationservice.dto.OperationResponse;
import com.rits.operationservice.dto.OperationResponseList;
import com.rits.operationservice.model.Operation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OperationRepository extends MongoRepository<Operation,String> {
    boolean existsByOperationAndSiteAndActive(String operation, String site, int active);

    List<OperationResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    Operation findByOperationAndActive(String operation, int active);

    long countByOperationAndSiteAndActive(String operation, String site, int active);

    List<OperationResponse> findByOperationContainingIgnoreCaseAndSiteAndActive(String operation, String site, int active);

    List<Operation> findByOperationAndSiteAndActive(String operation, String site, int active);

    Operation findByActiveAndOperationAndSite(int active,String operation, String site);

    List<OperationResponse> findByActiveAndSiteAndAddAsErpOperation(int i, String site, boolean b);

    Boolean existsBySiteAndActiveAndHandle(String site, int i, String operation);

    Operation findByOperationAndRevisionAndSiteAndActive(String operation, String revision, String site, int i);

    Operation findByOperationAndCurrentVersionAndSiteAndActive(String operation, boolean b, String site, int i);

    boolean existsByOperationAndRevisionAndSiteAndActive(String operation, String revision, String site, int i);

    boolean existsByOperationAndCurrentVersionAndSiteAndActive(String operation, boolean b, String site, int i);

    Operation findByOperationAndRevisionAndActiveAndSite(String operation, String revision, int i, String site);

    List<OperationResponse> findBySiteAndActive(String site, int i);

    OperationResponse findByActiveAndSiteAndOperationAndCurrentVersion(int active, String site, String operation, boolean currentVersion);
    Operation findBySiteAndActiveAndOperationAndCurrentVersion(String site, int active, String operation, boolean currentVersion);

//    List<Operation> findByOperationAndSite(String operation, String site);
//    List<Operation> findBySiteAndOperationAndActive(String site, String operation, int i);

//    Operation findBySiteAndOperationAndCurrentVersion(String site, String operation);
}

