package com.rits.activityhookservice.controller;

import com.rits.activityhookservice.dto.ActivityHookRequest;
import com.rits.activityhookservice.exception.ActivityHookException;
import com.rits.activityhookservice.models.ActivityHook;
import com.rits.activityhookservice.models.ActivityHookListResponse;
import com.rits.activityhookservice.models.MessageModel;
import com.rits.activityhookservice.service.ActivityHookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/activityhook-service")
public class ActivityHookController {

    private final ActivityHookService activityHookService;

    @PostMapping("create")
    public MessageModel createHook(@RequestBody ActivityHookRequest request) {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                 return activityHookService.create(request);
            } catch (ActivityHookException activityHookException) {
                throw activityHookException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ActivityHookException(7001);
    }

    @PostMapping("update")
    public MessageModel updateHook(@RequestBody ActivityHookRequest request) {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return activityHookService.update(request);
            } catch (ActivityHookException activityHookException) {
                throw activityHookException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ActivityHookException(7001);
    }

    @PostMapping("delete")
    public MessageModel delete(@RequestBody ActivityHookRequest request) {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return activityHookService.delete(request);
            } catch (ActivityHookException activityHookException) {
                throw activityHookException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ActivityHookException(7001);
    }

    @PostMapping("retrieve")
    public ActivityHook retrieveHook(@RequestBody ActivityHookRequest request) {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return activityHookService.retrieve(request);
            } catch (ActivityHookException activityHookException) {
                throw activityHookException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ActivityHookException(7001);
    }

    @PostMapping("retrieveAll")
    public List<ActivityHookListResponse> retrieveAllHooks(@RequestBody ActivityHookRequest request) {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
               return activityHookService.retrieveAll(request.getSite());
            } catch (ActivityHookException activityHookException) {
                throw activityHookException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ActivityHookException(7001);
    }

    @PostMapping("retrieveTop50")
    public List<ActivityHookListResponse> retrieveTop50Hooks(@RequestBody ActivityHookRequest request) {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return activityHookService.retrieveTop50(request.getSite());
            } catch (ActivityHookException activityHookException) {
                throw activityHookException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ActivityHookException(7001);
    }
}
