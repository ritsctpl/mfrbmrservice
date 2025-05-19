package com.rits.managementservice.repository;

import com.rits.managementservice.dto.MessageModel;
import com.rits.managementservice.model.ManageColorSchema;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManageColorSchemaRepository extends MongoRepository<ManageColorSchema,String> {
    boolean existsByHandleAndSiteAndActive(String handle, String site, int active);
    ManageColorSchema findByHandleAndSiteAndActive(String handle, String site, int active);
//    ManageColorSchema findByDashBoardNameAndSiteAndActive(String dashBoardName, String site, int active);
    List<ManageColorSchema> findByDashBoardNameAndSiteAndActive(String dashBoardName, String site, int active);

    boolean existsByActiveAndSiteAndDashBoardName(int active, String site, String dashBoardName);

}
