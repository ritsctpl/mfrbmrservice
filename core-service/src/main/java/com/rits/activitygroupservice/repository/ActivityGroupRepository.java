package com.rits.activitygroupservice.repository;

import com.rits.activitygroupservice.dto.ActivityGroupResponse;
import com.rits.activitygroupservice.model.ActivityGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActivityGroupRepository extends MongoRepository<ActivityGroup,String> {

    List<ActivityGroupResponse> findTop50ByOrderByCreatedDateTimeDesc();

    ActivityGroup findByActiveAndActivityGroupName(int active,String activityGroupName);
    List<ActivityGroupResponse> findByActivityGroupNameContainingIgnoreCaseAndActive(String activityGroupName,  int active);

    long countByActivityGroupNameAndActive(String activityGroupName, int active);

    boolean existsByActivityGroupNameAndActive(String activityGroupName,int active);

    List<ActivityGroup> findByActivityGroupNameAndActive( String activityGroupName, int active);

    List<ActivityGroupResponse> findTop50ByActiveOrderByCreatedDateTimeDesc(int active);

    List<ActivityGroup> findByActive( int active);
    List<ActivityGroup> findByActiveAndCurrentSite( int active, String site);
}
