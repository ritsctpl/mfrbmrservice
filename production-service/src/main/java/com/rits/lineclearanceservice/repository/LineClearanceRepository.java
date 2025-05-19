package com.rits.lineclearanceservice.repository;

import com.rits.lineclearanceservice.model.LineClearance;
import com.rits.lineclearanceservice.model.LineClearanceResponse;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface LineClearanceRepository extends MongoRepository<LineClearance,String> {

    LineClearance findByHandleAndSiteAndActive(String handle, String site, int i);
    LineClearance findByTemplateNameAndSiteAndActive(String templateName, String site, int i);
    List<LineClearanceResponse> findBySiteAndActive(String site, int i);
    List<LineClearanceResponse> findTop50BySiteAndActive(String site, int active);
    boolean existsBySiteAndActiveAndTemplateName(String site, int i, String templateName);
    @Query("{ 'site': ?0, 'active': 1, 'associatedTo': { $elemMatch: { 'resourceId': ?1, 'workcenterId': ?2 } } }")
    List<LineClearance> findBySiteAndResourceIdAndWorkcenterId(String site, String resourceId, String workcenterId);
    @Query("{ 'site': ?0, 'active': 1, 'associatedTo': { $elemMatch: { 'workcenterId': ?1 } } }")
    List<LineClearance> findBySiteAndWorkcenterId(String site, String workcenterId);
    @Query("{ 'site': ?0, 'active': 1, 'associatedTo': { $elemMatch: { 'resourceId': ?1 } } }")
    List<LineClearance> findBySiteAndResourceId(String site, String resourceId);

    @Query("SELECT l FROM R_LINE_CLEARANCE_TEMPLATE l WHERE " +
            "(:resourceId IS NULL OR l.resourceId = :resourceId) " +
            "OR (:workCenterId IS NULL OR l.workCenterId = :workCenterId)")
    List<LineClearance> findByResourceIdOrWorkcenterId(String resourceId, String workCenterId);
}
