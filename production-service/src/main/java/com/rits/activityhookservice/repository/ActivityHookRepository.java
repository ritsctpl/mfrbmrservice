package com.rits.activityhookservice.repository;

import com.rits.activityhookservice.models.ActivityHook;

import com.rits.activityhookservice.models.ActivityHookListResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActivityHookRepository extends MongoRepository<ActivityHook, String> {

    ActivityHook findByActivityHookIdAndSiteAndActive(String activityHookId, String site, int i);
    List<ActivityHookListResponse> findBySiteAndActive(String site, int i);
    List<ActivityHookListResponse> findTop50BySiteAndActive(String site, int active);
    boolean existsBySiteAndActiveAndActivityHookId(String site, int i, String activityHookId);
}
