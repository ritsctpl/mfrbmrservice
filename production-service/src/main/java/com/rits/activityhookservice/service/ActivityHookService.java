package com.rits.activityhookservice.service;

import com.rits.activityhookservice.dto.ActivityHookRequest;
import com.rits.activityhookservice.models.ActivityHook;
import com.rits.activityhookservice.models.ActivityHookListResponse;
import com.rits.activityhookservice.models.MessageModel;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface ActivityHookService {
    MessageModel create(ActivityHookRequest request);

    MessageModel update(ActivityHookRequest request);

    MessageModel delete(ActivityHookRequest request);

    ActivityHook retrieve(ActivityHookRequest request);

    List<ActivityHookListResponse> retrieveAll(String site);
    List<ActivityHookListResponse> retrieveTop50(String site);
}
