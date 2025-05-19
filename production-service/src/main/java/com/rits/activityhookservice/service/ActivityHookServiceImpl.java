package com.rits.activityhookservice.service;

import com.rits.activityhookservice.dto.ActivityHookRequest;
import com.rits.activityhookservice.exception.ActivityHookException;
import com.rits.activityhookservice.models.ActivityHook;
import com.rits.activityhookservice.models.ActivityHookListResponse;
import com.rits.activityhookservice.models.MessageDetails;
import com.rits.activityhookservice.models.MessageModel;
import com.rits.activityhookservice.repository.ActivityHookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
@Service
@RequiredArgsConstructor
public class ActivityHookServiceImpl implements ActivityHookService{

    private final ActivityHookRepository activityHookRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public MessageModel create(ActivityHookRequest request) {
        if(request.getActivityHookId() == null || request.getActivityHookId().isEmpty()){
            throw new ActivityHookException(4401);
        }
        ActivityHook existingActivityHook = activityHookRepository.findByActivityHookIdAndSiteAndActive(request.getActivityHookId(), request.getSite(), 1);
        if (existingActivityHook != null) {
            throw new ActivityHookException(4402, request.getActivityHookId());
        }
        ActivityHook activityHook = activityHookBuilder(request);
        activityHook.setCreatedBy(request.getUserId());
        activityHook.setCreatedDateTime(LocalDateTime.now());
        activityHookRepository.save(activityHook);

        String createMessage = getFormattedMessage(1, request.getActivityHookId());
        return MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).activityHook(activityHook).build();
    }

    @Override
    public MessageModel update(ActivityHookRequest request) {
        if(request.getActivityHookId() == null || request.getActivityHookId().isEmpty()){
            throw new ActivityHookException(4401);
        }
        ActivityHook existingActivityHook = activityHookRepository.findByActivityHookIdAndSiteAndActive(request.getActivityHookId(), request.getSite(), 1);
        if (existingActivityHook == null) {
            throw new ActivityHookException(4403, request.getActivityHookId());
        }
        ActivityHook activityHook = activityHookBuilder(request);
        activityHook.setCreatedBy(existingActivityHook.getCreatedBy());
        activityHook.setCreatedDateTime(existingActivityHook.getCreatedDateTime());
        activityHook.setModifiedBy(request.getUserId());
        activityHook.setModifiedDateTime(LocalDateTime.now());
        activityHookRepository.save(activityHook);
        String updateMessage = getFormattedMessage(2, request.getActivityHookId());
        return MessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).activityHook(activityHook).build();
    }

    @Override
    public MessageModel delete(ActivityHookRequest request) {
        if(request.getActivityHookId() == null || request.getActivityHookId().isEmpty()){
            throw new ActivityHookException(4401);
        }
        ActivityHook existingActivityHook = activityHookRepository.findByActivityHookIdAndSiteAndActive(request.getActivityHookId(), request.getSite(), 1);
        if (existingActivityHook == null) {
            throw new ActivityHookException(4403, request.getActivityHookId());
        }
        existingActivityHook.setActive(0);
        existingActivityHook.setModifiedDateTime(LocalDateTime.now());
        activityHookRepository.save(existingActivityHook);
        String deleteMessage = getFormattedMessage(3, request.getActivityHookId());
        return MessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
    }

    @Override
    public ActivityHook retrieve(ActivityHookRequest request) {
        if(request.getActivityHookId() == null || request.getActivityHookId().isEmpty()){
            throw new ActivityHookException(4401);
        }
        ActivityHook existingActivityHook = activityHookRepository.findByActivityHookIdAndSiteAndActive(request.getActivityHookId(), request.getSite(), 1);
        if (existingActivityHook == null) {
            throw new ActivityHookException(4403, request.getActivityHookId());
        }
        return existingActivityHook;
    }

    @Override
    public List<ActivityHookListResponse> retrieveAll(String site) {
        List<ActivityHookListResponse> existingActivityHookList = activityHookRepository.findBySiteAndActive(site, 1);
        return existingActivityHookList;
    }

    @Override
    public List<ActivityHookListResponse> retrieveTop50(String site) {
        List<ActivityHookListResponse> existingActivityHookList = activityHookRepository.findTop50BySiteAndActive(site, 1);
        return existingActivityHookList;
    }

    private ActivityHook activityHookBuilder(ActivityHookRequest request) {
        return ActivityHook.builder()
                .activityHookId(request.getActivityHookId())
                .description(request.getDescription())
                .targetClass(request.getTargetClass())
                .targetMethod(request.getTargetMethod())
                .hookType(request.getHookType())
                .hookPoint(request.getHookPoint())
                .hookClass(request.getHookClass())
                .hookMethod(request.getHookMethod())
                .executionMode(request.getExecutionMode())
                .attachmentList(request.getAttachmentList())
                .site(request.getSite())
                .userId(request.getUserId())
                .active(1)
                .build();
    }
}
