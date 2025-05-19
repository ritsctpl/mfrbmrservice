package com.rits.hookservice.service;

import com.rits.customhookservice.exception.CustomHookException;
import com.rits.exception.ErrorDetails;
import com.rits.hookservice.model.*;
import com.rits.lineclearancelogservice.service.LineClearanceLogService;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import com.rits.logbuyoffservice.service.LogBuyOffService;
import com.rits.processorderrelease.dto.ProcessOrderReleaseRequest;
import com.rits.processorderstateservice.dto.ProcessOrderCompleteRequest;
import com.rits.processorderstateservice.dto.ProcessOrderStartRequest;
import com.rits.processorderstateservice.dto.ResourceRequest;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import com.rits.processorderstateservice.dto.OperationRequest;
import com.rits.productionlogservice.dto.WorkCenterRequest;
import com.rits.worklistservice.dto.ActivityRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import java.util.function.Function;

@Component("customHook")
public class CustomHook {

    @Autowired
    private LineClearanceLogService lineClearanceLogService;
    @Autowired
    private LogBuyOffService logBuyOffService;
    @Autowired
    @Lazy
    private WebClient.Builder webClientBuilder;
    @Value("${operation-service.url}/retrieveOperationsBySite")
    private String getByOperation;

    @Value("${resource-service.url}/retrieveByResource")
    private String getByResource;

    @Value("${workcenter-service.url}/retrieve")
    private String getByWorkcenter;

    @Value("${base-production-service.url}/")
    private String baseUrl;

    @Value("${core-service.url}/getActivityUrl")
    private String getActivityUrl;

    @Value("${site-service.url}/retrieveBySite")
    private String getBySite;

    private boolean isHookExecuted = false;

    private String hookpoint;

    public void setHookExecuted(boolean value) {
        this.isHookExecuted = value;
    }

    public boolean isHookExecuted() {
        return this.isHookExecuted;
    }

    /**
     * BEFORE hook for testHookableProcessStart.
     * Note the parameter is exactly the type of the target method's input.
     */
    public void beforeStartProcess(ProcessOrderStartRequest request) {
        System.out.println("CustomHook.beforeStartProcess invoked with request: " + request);

        List<OperationRequest> operationRequests = request.getStartBatches().stream()
                .map(batch -> OperationRequest.builder()
                        .site(batch.getSite())
                        .operation(batch.getOperation())
                        .build())
                .collect(Collectors.toList());

        for (OperationRequest operationRequest : operationRequests) {

            List<Operation> operations = webClientBuilder.build()
                    .post()
                    .uri(getByOperation)
                    .bodyValue(operationRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Operation>>() {
                    })
                    .block();


            for (Operation operation : operations) {

                if (operation.getActivityHookList() != null) {

                    for (ActivityHook activityHook : operation.getActivityHookList()) {

                        if (activityHook.getHookPoint() != null &&
                                activityHook.getActivity() != null &&
                                activityHook.isEnable()) {
                            try {
                                ActivityRequest activityRequest = new ActivityRequest();
                                activityRequest.setActivityId(activityHook.getActivity());
                                String url = webClientBuilder.build()
                                        .post()
                                        .uri(getActivityUrl)
                                        .bodyValue(activityRequest)
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<String>() {
                                        })
                                        .block();
                                ErrorDetails errorDetails = webClientBuilder.build()
                                        .post()
                                        .uri(baseUrl + url)
                                        .bodyValue(request)
                                        .retrieve()
                                        .bodyToMono(ErrorDetails.class)
                                        .block();

                                if (errorDetails != null && errorDetails.getErrorCode() != null) {
                                    throw new ProcessOrderStateException(Integer.parseInt(errorDetails.getErrorCode()));
                                }
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * AFTER hook for testHookableProcessStart.
     * Note the parameter is exactly the return type of the target method.
     */
    public void afterStartProcess(String result) {
        System.out.println("CustomHook.afterStartProcess invoked with result: " + result);
        // For example, log the outcome.
    }

    public void beforeReleaseOrders(ProcessOrderReleaseRequest request) {
        System.out.println("beforeReleaseOrders called");
        List<SiteRequest> siteRequests = request.getOrders().stream()
                .map(batch -> SiteRequest.builder()
                        .site(batch.getSite())
                        .build())
                .collect(Collectors.toList());

        for (SiteRequest siteRequest : siteRequests) {

            Site site = webClientBuilder.build()
                    .post()
                    .uri(getBySite)
                    .bodyValue(siteRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Site>() {
                    })
                    .block();

            if (site.getActivityHookLists() != null) {

                for (ActivityHookList activityHook : site.getActivityHookLists()) {

                    if (activityHook.getHookPoint() != null &&
                            activityHook.getActivity() != null &&
                            activityHook.isEnabled()) {
                        try {
                            ActivityRequest activityRequest = new ActivityRequest();
                            activityRequest.setActivityId(activityHook.getActivity());
                            String url = webClientBuilder.build()
                                    .post()
                                    .uri(getActivityUrl)
                                    .bodyValue(activityRequest)
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<String>() {
                                    })
                                    .block();
                            ErrorDetails errorDetails = webClientBuilder.build()
                                    .post()
                                    .uri(baseUrl + url)
                                    .bodyValue(request)
                                    .retrieve()
                                    .bodyToMono(ErrorDetails.class)
                                    .block();

                            if (errorDetails != null && errorDetails.getErrorCode() != null) {
                                throw new CustomHookException(Integer.parseInt(errorDetails.getErrorCode()));
                            }
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                }
            }
        }
    }

    public void beforeProcessOrderComplete(ProcessOrderCompleteRequest request) {
        System.out.println("CustomHook.beforeStartProcess invoked with request: " + request);

        List<ResourceRequest> resourceRequests = request.getCompleteBatches().stream()
                .map(batch -> ResourceRequest.builder()
                        .site(batch.getSite())
                        .resource(batch.getResource())
                        .build())
                .collect(Collectors.toList());

        for (ResourceRequest resourceRequest : resourceRequests) {

            Resource resource = webClientBuilder.build()
                    .post()
                    .uri(getByResource)
                    .bodyValue(resourceRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Resource>() {
                    })
                    .block();
            if (resource.getActivityHookList() != null && !resource.getActivityHookList().isEmpty()) {

                for (ActivityHook activityHook : resource.getActivityHookList()) {

                    if (activityHook.getHookPoint() != null &&
                            activityHook.getActivity() != null &&
                            activityHook.isEnable()) {
                        try {
                            ActivityRequest activityRequest = new ActivityRequest();
                            activityRequest.setActivityId(activityHook.getActivity());
                            String url = webClientBuilder.build()
                                    .post()
                                    .uri(getActivityUrl)
                                    .bodyValue(activityRequest)
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<String>() {
                                    })
                                    .block();
                            ErrorDetails errorDetails = webClientBuilder.build()
                                    .post()
                                    .uri(baseUrl + url)
                                    .bodyValue(request)
                                    .retrieve()
                                    .bodyToMono(ErrorDetails.class)
                                    .block();

                            if (errorDetails != null && errorDetails.getErrorCode() != null) {
                                throw new ProcessOrderStateException(Integer.parseInt(errorDetails.getErrorCode()));
                            }
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                }
            }else {
                 isHookExecuted =true;
            }
        }
    }

    public void execute(ProceedingJoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();
        Object request = arguments[0];
        try {
            Method getStartBatchesMethod = request.getClass().getMethod("getStartBatches");
            Object batches = getStartBatchesMethod.invoke(request);

            if (batches instanceof List<?>) {
                List<?> batchList = (List<?>) batches;

                List<OperationRequest> operationRequests = batchList.stream()
                        .map(batch -> {
                            try {
                                Method getSiteMethod = batch.getClass().getMethod("getSite");
                                Method getOperationMethod = batch.getClass().getMethod("getOperation");

                                String site = (String) getSiteMethod.invoke(batch);
                                String operation = (String) getOperationMethod.invoke(batch);

                                return OperationRequest.builder()
                                        .site(site)
                                        .operation(operation)
                                        .build();

                            } catch (Exception e) {
                                throw new RuntimeException("Failed to extract fields from batch", e);
                            }
                        })
                        .collect(Collectors.toList());

                List<ResourceRequest> resourceRequests = batchList.stream()
                        .map(batch -> {
                            try {
                                Method getSiteMethod = batch.getClass().getMethod("getSite");
                                Method getResourceMethod = batch.getClass().getMethod("getResource");

                                String site = (String) getSiteMethod.invoke(batch);
                                String resource = (String) getResourceMethod.invoke(batch);

                                return ResourceRequest.builder()
                                        .site(site)
                                        .resource(resource)
                                        .build();

                            } catch (Exception e) {
                                throw new RuntimeException("Failed to extract fields from batch", e);
                            }
                        })
                        .collect(Collectors.toList());

                List<WorkCenterRequest> workCenterRequests = batchList.stream()
                        .map(batch -> {
                            try {
                                Method getSiteMethod = batch.getClass().getMethod("getSite");
                                Method getWorkcenterMethod = batch.getClass().getMethod("getWorkcenter");

                                String site = (String) getSiteMethod.invoke(batch);
                                String workcenter = (String) getWorkcenterMethod.invoke(batch);

                                return WorkCenterRequest.builder()
                                        .site(site)
                                        .workCenter(workcenter)
                                        .build();

                            } catch (Exception e) {
                                throw new RuntimeException("Failed to extract fields from batch", e);
                            }
                        })
                        .collect(Collectors.toList());
                for (OperationRequest operationRequest : operationRequests) {

                    List<Operation> operations = webClientBuilder.build()
                            .post()
                            .uri(getByOperation)
                            .bodyValue(operationRequest)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<Operation>>() {
                            })
                            .block();

                    for (Operation operation : operations) {
                        if (operation.getActivityHookList() != null) getHook(operation.getActivityHookList(), request);
                    }
                }
                if(!((hookpoint == "Pre_Start") || (hookpoint == "Pre_Batch_start"))){
                for (ResourceRequest resourceRequest : resourceRequests) {

                    Resource resource = webClientBuilder.build()
                            .post()
                            .uri(getByResource)
                            .bodyValue(resourceRequest)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Resource>() {
                            })
                            .block();
                    if (resource.getActivityHookList() != null) getHook(resource.getActivityHookList(), request);
                }
                }
                for (WorkCenterRequest workCenterRequest : workCenterRequests) {

                    WorkCenter workcenter = webClientBuilder.build()
                            .post()
                            .uri(getByWorkcenter)
                            .bodyValue(workCenterRequest)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<WorkCenter>() {
                            })
                            .block();
                    if (workcenter.getActivityHookList() != null) getHook(workcenter.getActivityHookList(), request);
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method using reflection", e);
        }
    }

    private void getHook(List<ActivityHook> activityHookList, Object request) {
        for (ActivityHook activityHook : activityHookList) {

            if (activityHook.getHookPoint() != null &&
                    activityHook.getActivity() != null &&
                    activityHook.isEnable()) {
                try {
                    hookpoint = activityHook.getHookPoint();
                    ActivityRequest activityRequest = new ActivityRequest();
                    activityRequest.setActivityId(activityHook.getActivity());
                    String url = webClientBuilder.build()
                            .post()
                            .uri(getActivityUrl)
                            .bodyValue(activityRequest)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<String>() {
                            })
                            .block();
                    ErrorDetails errorDetails = webClientBuilder.build()
                            .post()
                            .uri(baseUrl + url)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(ErrorDetails.class)
                            .block();
                } catch (CustomHookException e) {
                    throw e;
                }
            }
        }
    }

    private void getHookList(List<ActivityHookList> activityHookList, Object request) {
        for (ActivityHookList activityHook : activityHookList) {

            if (activityHook.getHookPoint() != null &&
                    activityHook.getActivity() != null &&
                    activityHook.isEnabled()) {
                try {
                    hookpoint = activityHook.getHookPoint();
                    ActivityRequest activityRequest = new ActivityRequest();
                    activityRequest.setActivityId(activityHook.getActivity());
                    String url = webClientBuilder.build()
                            .post()
                            .uri(getActivityUrl)
                            .bodyValue(activityRequest)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<String>() {
                            })
                            .block();
                    ErrorDetails errorDetails = webClientBuilder.build()
                            .post()
                            .uri(baseUrl + url)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(ErrorDetails.class)
                            .block();
                } catch (CustomHookException e) {
                    throw e;
                }
            }
        }
    }
}