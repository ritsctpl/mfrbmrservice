package com.rits.activityservice.repository;
import com.rits.activityservice.dto.ActivityHookListResponse;
import com.rits.activityservice.dto.ActivityListResponse;
import com.rits.activityservice.dto.ActivityResponse;
import com.rits.activityservice.model.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
public interface ActivityRepository extends MongoRepository<Activity,String> {
    List<ActivityListResponse> findByActivityIdContainingIgnoreCaseAndActive(String activityId, int active);
    Activity findByActivityId(String activityId);

    long countByActivityIdAndActive(String activityId, int active);


    Boolean existsByActivityIdAndActive(String activityId, int active);

    List<Activity> findByActivityGroupListActivityGroupName(String activityGroup);

    List<ActivityListResponse> findTop50ByActiveOrderByCreatedDateTimeDesc(int active);
    List<Activity> findTop50ByTypeAndActiveOrderByCreatedDateTimeDesc(String type, int active);


    Activity findByActivityIdAndActive(String activityId, int active);

    List<Activity> findByActive(int active);

    List<Activity> findByActivityHookList_EnableAndActivityHookList_ActivityAndActive(boolean b, String activity, int active);

/*    Activity findByActivityIdAndActive(String activityId, int active);*/

    List<Activity> findByActivityHookList_EnableAndActivityIdAndActive(boolean enable, String activityId, int active);

    //List<Activity> findByActiveAndCurrentSite(int i,String currentSite);

    List<Activity> findByActiveAndType(int i, String service);

    List<ActivityHookListResponse> findByTypeAndActive(String hooks, int i);

    List<Activity> findByActivityIdContainingIgnoreCaseAndTypeAndActive(String activity, String service, int i);

    Activity findBySiteAndActivityIdAndActive(String site, String activityId, int i);
}
