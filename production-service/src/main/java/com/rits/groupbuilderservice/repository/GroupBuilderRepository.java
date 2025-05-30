package com.rits.groupbuilderservice.repository;

import com.rits.groupbuilderservice.model.GroupBuilder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupBuilderRepository extends MongoRepository<GroupBuilder,String> {
    GroupBuilder findByHandleAndSiteAndActive(String handle, String site, int i);
    List<GroupBuilder> findBySiteAndGroupLabelContainingIgnoreCaseAndActiveEquals(String site,String groupName, int i);
    List<GroupBuilder> findTop50BySiteAndActive(String site, int active);
    boolean existsBySiteAndActiveAndGroupLabel(String site, int i, String groupLabel);
}
