package com.rits.ncgroupservice.repository;

import com.rits.ncgroupservice.dto.NcGroupResponse;
import com.rits.ncgroupservice.model.NcGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NcGroupRepository extends MongoRepository<NcGroup, String> {
    public boolean existsByActiveAndSiteAndNcGroup(int active, String site, String ncGroup);

    public NcGroup findByActiveAndSiteAndNcGroup(int active, String site, String ncGroup);

    public List<NcGroupResponse> findByActiveAndSiteAndNcGroupContainingIgnoreCase(int active, String site, String ncGroup);

    public List<NcGroupResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    public List<NcGroupResponse> findByActiveAndSite(int active, String site);

    List<NcGroupResponse> findByActiveAndSiteAndOperationList_Operation(int i, String site, String operation);


}
