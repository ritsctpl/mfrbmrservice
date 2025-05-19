package com.rits.queryBuilder.repository;

import com.rits.queryBuilder.model.ManageFilter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManageFilterationRepository extends MongoRepository<ManageFilter,String> {
    boolean existsByHandleAndSiteAndActive(String handle, String site, int active);
    ManageFilter findByHandleAndSiteAndActive(String handle, String site, int active);
    ManageFilter findByDashBoardNameAndSiteAndActive(String dashBoardName, String site, int active);

}
