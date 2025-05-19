package com.rits.queryBuilder.repository;

import com.rits.queryBuilder.model.ManageDashboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManageDashboardRepository extends MongoRepository<ManageDashboard,String> {
    boolean existsByActiveAndSiteAndDashBoardName(int active, String site, String dashBoardName);
    ManageDashboard findByDashBoardNameAndSiteAndActive(String dashBoardName, String site, int active);
    ManageDashboard findByHandleAndSiteAndActive(String handle, String site, int active);

}
