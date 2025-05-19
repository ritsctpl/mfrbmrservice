package com.rits.worklistservice.repository;

import com.rits.worklistservice.model.WorkList;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkListRepository extends MongoRepository<WorkList,String> {
    String findByHandleContainingIgnoreCaseAndFieldName(String workList, String fieldName);

    List<WorkList> findByHandleContainingIgnoreCase(String workList);

    List<WorkList> findByPreDefinedFieldGroupContainingIgnoreCase(String category);

    WorkList findByPreDefinedFieldGroup(String dummy);
}
