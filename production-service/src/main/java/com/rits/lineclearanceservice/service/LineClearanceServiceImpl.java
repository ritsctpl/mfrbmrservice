package com.rits.lineclearanceservice.service;

import com.rits.lineclearanceservice.dto.RetrieveLineClearanceLogRequest;
import com.rits.lineclearanceservice.model.*;
import com.rits.lineclearanceservice.dto.LineClearanceRequest;
import com.rits.lineclearanceservice.exception.LineClearanceException;
import com.rits.lineclearanceservice.repository.LineClearanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sound.sampled.Line;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LineClearanceServiceImpl implements LineClearanceService {

    private final LineClearanceRepository lineClearanceRepository;
    private final MessageSource localMessageSource;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }
    @Override
    public MessageModel create(LineClearanceRequest request) {
        String handle = createHandle(request);
        LineClearance existingLineClearance = lineClearanceRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingLineClearance != null) {
            throw new LineClearanceException(9002, request.getTemplateName());
        }
        LineClearance lineClearance = lineClearanceBuilder(request);
        lineClearance.setHandle(handle);
        lineClearance.setCreatedBy(request.getUserId());
        lineClearance.setCreatedDateTime(LocalDateTime.now());

        lineClearanceRepository.save(lineClearance);

        String createMessage = getFormattedMessage(1, request.getTemplateName());
        return MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).checklistTemplates(lineClearance).build();
    }
    @Override
    public MessageModel update(LineClearanceRequest request) {

        String handle = createHandle(request);
        LineClearance existingLineClearance = lineClearanceRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingLineClearance == null) {
            throw new LineClearanceException(9003, request.getTemplateName());
        }

        LineClearance lineClearance = lineClearanceBuilder(request);
        lineClearance.setHandle(handle);
        lineClearance.setCreatedBy(existingLineClearance.getCreatedBy());
        lineClearance.setCreatedDateTime(existingLineClearance.getCreatedDateTime());
        lineClearance.setModifiedBy(request.getUserId());
        lineClearance.setModifiedDateTime(LocalDateTime.now());

        lineClearanceRepository.save(lineClearance);

        String updateMessage = getFormattedMessage(2, request.getTemplateName());
        return MessageModel.builder().message_details(new com.rits.lineclearanceservice.model.MessageDetails(updateMessage, "S")).checklistTemplates(lineClearance).build();
    }
    @Override
    public MessageModel delete(LineClearanceRequest request) {

        String handle = createHandle(request);
        LineClearance existingLineClearance = lineClearanceRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingLineClearance == null) {
            throw new LineClearanceException(9003, request.getTemplateName());
        }
        existingLineClearance.setActive(0);
        existingLineClearance.setModifiedDateTime(LocalDateTime.now());

        lineClearanceRepository.save(existingLineClearance);

        String deleteMessage = getFormattedMessage(3, request.getTemplateName());
        return MessageModel.builder().message_details(new com.rits.lineclearanceservice.model.MessageDetails(deleteMessage, "S")).build();
    }
    @Override
    public List<LineClearanceResponse> retrieveAll(String site) {

        List<LineClearanceResponse> existingLineClearanceList = lineClearanceRepository.findBySiteAndActive(site, 1);
        return existingLineClearanceList;
    }

    @Override
    public LineClearance retrieve(LineClearanceRequest request) {

        String handle = createHandle(request);
        LineClearance existingLineClearance = lineClearanceRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
        if(existingLineClearance == null){
            throw new LineClearanceException(7009);
        }
        return existingLineClearance;
    }

    @Override
    public List<LineClearanceResponse> retrieveTop50(String site) {
        List<LineClearanceResponse> existingLineClearanceList = lineClearanceRepository.findTop50BySiteAndActive(site, 1);
        return existingLineClearanceList;
    }

    @Override
    public boolean isLineClearanceExist(String site, String templateName) {
        if(!StringUtils.hasText(templateName)){
            throw new LineClearanceException(9001);
        }
        return lineClearanceRepository.existsBySiteAndActiveAndTemplateName(site, 1, templateName);
    }

    @Override
    public List<RetrieveLineClearanceLogResponse> retrieveLineClearanceList(RetrieveLineClearanceLogRequest request) {
        List<LineClearance> lineClearances;
        if (request.getResourceId() != null && !request.getResourceId().trim().isEmpty() &&
                request.getWorkCenterId() != null && !request.getWorkCenterId().trim().isEmpty()) {
//        lineClearances = lineClearanceRepository.findBySiteAndResourceIdAndWorkcenterId(request.getSite(), request.getResourceId(), request.getWorkCenterId());
            lineClearances = lineClearanceRepository.findByResourceIdOrWorkcenterId( request.getResourceId(), request.getWorkCenterId());

        } else if (request.getWorkCenterId() != null && !request.getWorkCenterId().trim().isEmpty()) {
            lineClearances = lineClearanceRepository.findBySiteAndWorkcenterId(request.getSite(), request.getWorkCenterId());
        } else {
            lineClearances = lineClearanceRepository.findBySiteAndResourceId(request.getSite(), request.getResourceId());
        }

        return lineClearances.stream()
                .flatMap(lineClearance -> lineClearance.getTasks().stream()
                        .map(task -> RetrieveLineClearanceLogResponse.builder()
                                .templeteName(lineClearance.getTemplateName())
                                .taskName(task.getTaskName())
                                .isMandatory(task.getIsMandatory())
                                .evidenceRequired(task.getEvidenceRequired())
                                .description(lineClearance.getDescription())
                                .isMandatory(task.getIsMandatory())
                                .clearanceTimeLimit(lineClearance.getClearanceTimeLimit())
                                .evidenceRequired(task.getEvidenceRequired())
                                .build()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkPermission(String templateName, String site, Integer active, String userId, String status) {
        // Query the LineClearance repository based on templateName, site, and active status
        LineClearance lineClearance = lineClearanceRepository.findByTemplateNameAndSiteAndActive(templateName, site, active);

        // If no LineClearance is found, return false
        if (lineClearance == null) {
            return false;
        }

        // Iterate through the user roles in the LineClearance
        for (UserRole role : lineClearance.getUserRoles()) {
            // Check if the userId matches the userId in the role (assuming userId is part of the user role)
            if (role.getRoleId().equals(userId)) {
                // Check for permission based on the status provided
                if ("Start".equals(status) && role.getPermissions().contains("Initiate Clearance")) {
                    return true; // If status is "Start" and permission "Initiate Clearance" is found
                }
                if ("Complete".equals(status) && role.getPermissions().contains("Complete Tasks")) {
                    return true; // If status is "Complete" and permission "Complete Tasks" is found
                }
                if ("Approved".equals(status) && role.getPermissions().contains("Approve Clearance")) {
                    return true; // If status is "Complete" and permission "Complete Tasks" is found
                }
                if ("Reject".equals(status) && role.getPermissions().contains("Reject Clearance")) {
                    return true; // If status is "Complete" and permission "Complete Tasks" is found
                }
                // If no matching status or permission found, return false
                return false;
            }
        }
        // If no matching userId found in the user roles, return false
        return false;
    }


    private String createHandle(LineClearanceRequest lineClearanceRequest){
        validateRequest(lineClearanceRequest);
        String templateNameLCBO = "LCBO:" + lineClearanceRequest.getSite() + "," + lineClearanceRequest.getTemplateName();
        return templateNameLCBO;
    }

    public boolean validateRequest(LineClearanceRequest request){
        if(!StringUtils.hasText(request.getSite())){
            throw new LineClearanceException(7001);
        }
        if(!StringUtils.hasText(request.getTemplateName())) {
            throw new LineClearanceException(9001);
        }
        return true;
    }

    private LineClearance lineClearanceBuilder(LineClearanceRequest request) {
        LineClearance lineClearance = LineClearance.builder()
                .site(request.getSite())
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .clearanceTimeLimit(request.getClearanceTimeLimit())
                .notifyOnCompletion(request.getNotifyOnCompletion())
                .maxPendingTasks(request.getMaxPendingTasks())
                .clearanceReminderInterval(request.getClearanceReminderInterval())
                .enablePhotoEvidence(request.getEnablePhotoEvidence())
                .userId(request.getUserId())
                .associatedTo(request.getAssociatedTo() != null ?
                        mapToAssociatedLocations(request.getAssociatedTo()) : new ArrayList<>())
                .tasks(request.getTasks() != null ?
                        mapToTasks(request.getTasks()) : new ArrayList<>())
                .userRoles(request.getUserRoles() != null ?
                        mapToUserRoles(request.getUserRoles()) : new ArrayList<>())
                .active(1)
                .build();

        return lineClearance;
    }

    private List<AssociatedLocation> mapToAssociatedLocations(List<AssociatedLocation> requests) {
        return requests.stream()
                .map(request -> AssociatedLocation.builder()
                        .workcenterId(request.getWorkcenterId())
                        .resourceId(request.getResourceId())
                        .enable(request.getEnable())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Task> mapToTasks(List<Task> requests) {
        return requests.stream()
                .map(request -> Task.builder()
                        .taskId(request.getTaskId())
                        .taskName(request.getTaskName())
                        .taskDescription(request.getTaskDescription())
                        .isMandatory(request.getIsMandatory())
                        .evidenceRequired(request.getEvidenceRequired())
                        .build())
                .collect(Collectors.toList());
    }

    private List<UserRole> mapToUserRoles(List<UserRole> requests) {
        return requests.stream()
                .map(request -> UserRole.builder()
                        .roleId(request.getRoleId())
                        .roleName(request.getRoleName())
                        .permissions(request.getPermissions())
                        .build())
                .collect(Collectors.toList());
    }

}
