package com.rits.extensionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rits.extensionservice.dto.Activity;
import com.rits.extensionservice.dto.ActivityHook;
import com.rits.extensionservice.dto.ActivityRequest;
import com.rits.extensionservice.dto.ExtensionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtensionServiceImpl implements  ExtensionService {

    private final WebClient.Builder webClientBuilder;

    @Value("${activity-service.url}/retrieve")
    private String activityUrl;

    @Value("${revision-service.url}/addRevisionExtension")
    private String revisionUrl;

    @Override
    public String createExtension(ExtensionRequest extensionRequest) throws Exception {
        String activity = extensionRequest.getActivity();
        ActivityRequest activityRequest=ActivityRequest.builder().site(extensionRequest.getSite()).activityId(activity).build();

        Activity getActivity = webClientBuilder.build()
                .post()
                .uri(activityUrl)
                .bodyValue(activityRequest)
                .retrieve()
                .bodyToMono(Activity.class)
                .block();
        if(getActivity.getActivityHookList() != null && !getActivity.getActivityHookList().isEmpty() )
         {

            List<ActivityHook> activityHookList = getActivity.getActivityHookList();
            for (ActivityHook activityHook : activityHookList
            ) {
                if (extensionRequest.getHookPoint().equalsIgnoreCase(activityHook.getHookPoint())
                        && extensionRequest.getHookableMethod().equalsIgnoreCase(activityHook.getHookableMethod())
                        && activityHook.getEnable().equals(true)
                ) {
//                    String getRequestString = webClientBuilder.build()
//                            .post()
//                            .uri(revisionUrl)
//                            .bodyValue(extensionRequest.getRequest())
//                            .retrieve()
//                            .bodyToMono(String.class)
//                            .block();
//                    return getRequestString;
                    String getRequestString=addRevisionExtension(extensionRequest.getRequest());
                    return getRequestString;

                }

            }

        }
        return extensionRequest.getRequest();
    }
    public String addRevisionExtension(String request) throws JsonProcessingException {



        if (request.contains("\"revision\"")) {
            // Add "A" before the comma after the revision value
            request = request.replaceFirst("(\"revision\"\\s*:\\s*\"[^\"]*)(\")", "$1A$2");

        }

        return request;
    }
}
