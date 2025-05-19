package com.rits.usergroupservice.repository;

import com.rits.site.model.Site;
import com.rits.usergroupservice.dto.AvailableUserGroup;
import com.rits.usergroupservice.dto.UserGroupResponse;
import com.rits.usergroupservice.model.UserGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserGroupRepository extends MongoRepository<UserGroup, String> {
    public boolean existsByActiveAndSiteAndUserGroup(int active, String site, String userGroup);

    public UserGroup findByActiveAndSiteAndUserGroup(int active, String site, String userGroup);

    public List<UserGroupResponse> findByActiveAndSiteAndUserGroupContainingIgnoreCase(int active, String site, String userGroup);

    public List<UserGroupResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    public List<AvailableUserGroup> findByActiveAndSite(int active, String site);


    List<UserGroup> findBySiteAndActive(String site, int active);

}
