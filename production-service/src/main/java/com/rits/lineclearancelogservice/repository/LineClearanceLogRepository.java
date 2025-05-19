package com.rits.lineclearancelogservice.repository;

import com.rits.lineclearancelogservice.model.LineClearanceLog;
import com.rits.lineclearancelogservice.model.LineClearanceLogResponse;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface LineClearanceLogRepository extends MongoRepository<LineClearanceLog,String> {
    LineClearanceLog findBySiteAndBatchNo(String site, String batchNo);

//    List<LineClearanceLog> findBySiteAndBatchNoAndTaskName(String site, String batchNo, String taskName);

    List<LineClearanceLogResponse> findByActiveAndSiteAndBatchNoAndResourceIdAndOperationAndPhase(int active, String site, String batchNo, String resourceId, String operation, String phase);

    List<RetrieveLineClearanceLogResponse> findBySiteAndBatchNoAndResourceIdAndWorkCenterId(String site, String batchNo, String resourceId, String workCenterId);

    LineClearanceLog findByBatchNo(String batchNo);

//    List<RetrieveLineClearanceLogResponse> findByResourceIdAndWorkCenterId(String resourceId, String workCenterId);

//    List<RetrieveLineClearanceLogResponse> findBySitAndResourceIdAndWorkCenterId(String resourceId, String workCenterId);

//    List<RetrieveLineClearanceLogResponse> findBySiteAndResourceIdAndWorkCenterId(String site, String resourceId, String workCenterId);

    LineClearanceLog findBySiteAndBatchNoAndActive(String site, String batchNo, Integer active);
    List<LineClearanceLog> findBySiteAndBatchNoAndTaskNameAndActive(String site, String batchNo, String taskName, Integer active);

    LineClearanceLog findByHandleAndSiteAndActive(String handle, String site, int i);


    LineClearanceLog findByTempleteName(String templeteName);

    List<RetrieveLineClearanceLogResponse> findByResourceIdOrWorkCenterId(String resourceId, String workCenterId);

    List<RetrieveLineClearanceLogResponse> findByResourceIdAndWorkCenterId(String resourceId, String workCenterId);

    
    LineClearanceLog findBySiteAndTempleteNameAndBatchNoAndOperationAndPhase(String site, String templeteName, String batchNo, String operation, String phase);


    LineClearanceLog findBySiteAndHandleAndActive(String site, String handle, int i);

    List<RetrieveLineClearanceLogResponse> findByResourceId(String resourceId);

    List<RetrieveLineClearanceLogResponse> findByWorkCenterId(String workCenterId);

    List<RetrieveLineClearanceLogResponse> findByBatchNoAndResourceIdAndWorkCenterId(String batchNo, String resourceId, String workCenterId);

    List<RetrieveLineClearanceLogResponse> findByBatchNoAndWorkCenterId(String batchNo, String workCenterId);

    List<RetrieveLineClearanceLogResponse> findByBatchNoAndResourceId(String batchNo, String resourceId);

    LineClearanceLog findBySiteAndBatchNoAndTempleteNameAndTaskName(String site, String batchNo, String templeteName, String taskName);

    LineClearanceLog findBySiteAndTempleteNameAndBatchNoAndOperationAndPhaseAndTaskName(String site, String templeteName, String batchNo, String operation, String phase, String taskName);

    LineClearanceLog findBySiteAndTempleteNameAndBatchNoAndOperationAndPhaseAndTaskNameAndResourceId(String site, String templeteName, String batchNo, String operation, String phase, String taskName, String resourceId);



    LineClearanceLogResponse findByTempleteNameAndTaskName(String templeteName, String taskName);

    LineClearanceLogResponse findFirstByTempleteNameAndTaskName(String templeteName, String taskName);

//    List<LineClearanceLog> findBySiteAndBatchNoAndTempleteNameAndActive(String site, String batchNo, String templeteName, int active);




        @Query("{ 'site': ?0, $or: [ {'batchNo': ?1}, {'templeteName': ?2} ] , 'active': 1 }")
        List<LineClearanceLog> findBySiteAndOptionalBatchNoOrTempleteName(String site, String batchNo, String templeteName);


    LineClearanceLog findBySiteAndHandleAndResourceIdAndActive(String site, String handle, String resourceId, int i);

    List<LineClearanceLog> findBySiteAndBatchNoAndResourceId(String site, String batchNo, String resourceId);

    void deleteBySiteAndBatchNoAndResourceId(String site, String batchNo, String resourceId);
}
