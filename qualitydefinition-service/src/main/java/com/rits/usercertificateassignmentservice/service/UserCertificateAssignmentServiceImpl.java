package com.rits.usercertificateassignmentservice.service;

import com.rits.certificationservice.service.CertificationService;
import com.rits.usercertificateassignmentservice.dto.IsExist;
import com.rits.usercertificateassignmentservice.dto.UserCertificateAssignmentRequest;
import com.rits.usercertificateassignmentservice.exception.UserCertificationAssignmentException;
import com.rits.usercertificateassignmentservice.model.CertificationDetails;
import com.rits.usercertificateassignmentservice.model.MessageDetails;
import com.rits.usercertificateassignmentservice.model.UserCertificateAssignment;
import com.rits.usercertificateassignmentservice.model.UserCertificateAssignmentMessageModel;
import com.rits.usercertificateassignmentservice.repository.UserCertificateAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserCertificateAssignmentServiceImpl implements UserCertificateAssignmentService {
    private final UserCertificateAssignmentRepository userCertificateAssignmentRepository;
    private final WebClient.Builder webClientBuilder;
    private final CertificationService certificationService;
    private final MessageSource localMessageSource;
    @Value("${usergroup-service.url}/isExist")
    private String isUserGroupExist;
    @Value("${user-service.url}/isExist")
    private String isUserExist;
    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public UserCertificateAssignmentMessageModel save(UserCertificateAssignmentRequest userCertificateAssignment) throws Exception {
        boolean userGroupType=false;
        String createdMessage=null;
        if((userCertificateAssignment.getUserGroup()==null || userCertificateAssignment.getUserGroup().isEmpty()) && (userCertificateAssignment.getUser()==null || userCertificateAssignment.getUser().isEmpty())){
            throw new UserCertificationAssignmentException(1004);
        }
        if(userCertificateAssignment.getUserGroup()!=null && !userCertificateAssignment.getUserGroup().isEmpty()){
            if(userCertificateAssignmentRepository.existsByActiveAndSiteAndUserGroup(1,userCertificateAssignment.getSite(),userCertificateAssignment.getUserGroup())){
                throw new UserCertificationAssignmentException(1000,userCertificateAssignment.getUserGroup());
            }
            userGroupType=true;
        }else{
            if(userCertificateAssignment.getUser()!=null && !userCertificateAssignment.getUser().isEmpty()){
                if(userCertificateAssignmentRepository.existsByActiveAndSiteAndUser(1,userCertificateAssignment.getSite(),userCertificateAssignment.getUser())){
                    throw new UserCertificationAssignmentException(1001,userCertificateAssignment.getUser());
                }
            }
        }
        getVerified(userCertificateAssignment,userGroupType);
        if(userGroupType){
           createdMessage = getFormattedMessage(10, userCertificateAssignment.getUserGroup());
           userCertificateAssignment.setHandle("UserCertificateBO:"+userCertificateAssignment.getSite()+","+userCertificateAssignment.getUserGroup());
        }else{
            createdMessage=getFormattedMessage(11,userCertificateAssignment.getUser());
            userCertificateAssignment.setHandle("UserCertificateBO:"+userCertificateAssignment.getSite()+","+userCertificateAssignment.getUser());
        }
        UserCertificateAssignment userCertificateAssignmentObject=createBuilder(userCertificateAssignment);

        return  UserCertificateAssignmentMessageModel.builder()
                .response(userCertificateAssignmentRepository.save(userCertificateAssignmentObject))
                .message_details(new MessageDetails(createdMessage,"S"))
                .build();
    }

    @Override
    public UserCertificateAssignmentMessageModel update(UserCertificateAssignmentRequest userCertificateAssignment) throws Exception {
        boolean userGroupType=false;
        String updatedMessage=null;
        if(userCertificateAssignment.getUserGroup()!=null && !userCertificateAssignment.getUserGroup().isEmpty()){
            if(!userCertificateAssignmentRepository.existsByActiveAndSiteAndUserGroup(1,userCertificateAssignment.getSite(),userCertificateAssignment.getUserGroup())){
                throw new UserCertificationAssignmentException(1002,userCertificateAssignment.getUserGroup());
            }
            userGroupType=true;
        }else{
            if(userCertificateAssignment.getUser()!=null && !userCertificateAssignment.getUser().isEmpty()){
                if(!userCertificateAssignmentRepository.existsByActiveAndSiteAndUser(1,userCertificateAssignment.getSite(),userCertificateAssignment.getUser())){
                    throw new UserCertificationAssignmentException(1003,userCertificateAssignment.getUser());
                }
            }else{
                throw new UserCertificationAssignmentException(1004);
            }
        }
        UserCertificateAssignment existingUserCertificateAssignment=null;
        if(userGroupType){
            existingUserCertificateAssignment= userCertificateAssignmentRepository.findByActiveAndSiteAndUserGroup(1,userCertificateAssignment.getSite(),userCertificateAssignment.getUserGroup());
            updatedMessage = getFormattedMessage(12, userCertificateAssignment.getUserGroup());
        }else{
            existingUserCertificateAssignment= userCertificateAssignmentRepository.findByActiveAndSiteAndUser(1,userCertificateAssignment.getSite(),userCertificateAssignment.getUser());

            updatedMessage=getFormattedMessage(13,userCertificateAssignment.getUser());
        }

        getVerified(userCertificateAssignment,userGroupType);
        UserCertificateAssignment userCertificateAssignmentObject=updateBuilder(userCertificateAssignment,existingUserCertificateAssignment);


        return  UserCertificateAssignmentMessageModel.builder()
                .response(userCertificateAssignmentRepository.save(userCertificateAssignmentObject))
                .message_details(new MessageDetails(updatedMessage,"S"))
                .build();
    }

    private UserCertificateAssignment updateBuilder(UserCertificateAssignmentRequest userCertificateAssignment, UserCertificateAssignment existingUserCertificateAssignment) {
        return UserCertificateAssignment.builder()
                .handle(existingUserCertificateAssignment.getHandle())
                .site(existingUserCertificateAssignment.getSite())
                .user(existingUserCertificateAssignment.getUser())
                .userGroup(existingUserCertificateAssignment.getUserGroup())
                .createdBy(existingUserCertificateAssignment.getCreatedBy())
                .modifiedBy(userCertificateAssignment.getUserId())
                .modifiedDateTime(LocalDateTime.now())
                .createdDateTime(existingUserCertificateAssignment.getCreatedDateTime())
                .active(1)
                .certificationDetailsList(userCertificateAssignment.getCertificationDetailsList())
                .build();
    }

    private UserCertificateAssignment createBuilder(UserCertificateAssignmentRequest userCertificateAssignment) {
        return UserCertificateAssignment.builder()
                .handle(userCertificateAssignment.getHandle())
                .site(userCertificateAssignment.getSite())
                .user(userCertificateAssignment.getUser())
                .userGroup(userCertificateAssignment.getUserGroup())
                .createdBy(userCertificateAssignment.getUserId())
                .createdDateTime(LocalDateTime.now())
                .active(1)
                .certificationDetailsList(userCertificateAssignment.getCertificationDetailsList())
                .build();
    }

    private void getVerified(UserCertificateAssignmentRequest userCertificateAssignment, boolean userGroupType) throws Exception {
        IsExist isExist=null;
        if(userGroupType){
            isExist = IsExist.builder().site(userCertificateAssignment.getSite()).userGroup(userCertificateAssignment.getUserGroup()).build();
            boolean isUserGroupExist= isUserGroupExist(isExist);
            if(!isUserGroupExist){
                throw new UserCertificationAssignmentException(1005,userCertificateAssignment.getUserGroup());
            }

        }else{
            isExist=IsExist.builder().site(userCertificateAssignment.getSite()).user(userCertificateAssignment.getUser()).build();
            boolean isUserExist=isUserExist(isExist);
            if(!isUserExist){
                throw new UserCertificationAssignmentException(1006,userCertificateAssignment.getUser());

            }
        }
        if(userCertificateAssignment.getCertificationDetailsList()!=null && !userCertificateAssignment.getCertificationDetailsList().isEmpty()){
            for(CertificationDetails  certificationDetails: userCertificateAssignment.getCertificationDetailsList()){
                boolean isCertificationExist=certificationService.isExist(userCertificateAssignment.getSite(),certificationDetails.getCertification());
                if(!isCertificationExist){
                    throw new UserCertificationAssignmentException(1007, certificationDetails.getCertification());
                }

            }
        }
    }

    private Boolean isUserGroupExist(IsExist isExist) {
        return webClientBuilder.build()
                .post()
                .uri(isUserGroupExist)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
    private Boolean isUserExist(IsExist isExist) {
        return webClientBuilder.build()
                .post()
                .uri(isUserExist)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    @Override
    public UserCertificateAssignmentMessageModel delete(String site, String user,String userGroup,String userId) {
        UserCertificateAssignment existingUserCertificateAssignment;
        String deletedMessage;
        boolean userGroupType=false;
        if(userGroup!=null&& !userGroup.isEmpty()){
            userGroupType=true;
        }
        else{
            if(user==null || user.isEmpty()){
                throw new UserCertificationAssignmentException(1004);
            }
        }
        if(userGroupType){
            existingUserCertificateAssignment= userCertificateAssignmentRepository.findByActiveAndSiteAndUserGroup(1,site,userGroup);
            deletedMessage=getFormattedMessage(14,userGroup);
        }else{
            existingUserCertificateAssignment= userCertificateAssignmentRepository.findByActiveAndSiteAndUser(1,site,user);
            deletedMessage=getFormattedMessage(15,user);
        }
        if(existingUserCertificateAssignment!=null && existingUserCertificateAssignment.getHandle()!= null && !existingUserCertificateAssignment.getHandle().isEmpty()) {
            existingUserCertificateAssignment.setModifiedBy(userId);
            existingUserCertificateAssignment.setModifiedDateTime(LocalDateTime.now());
            existingUserCertificateAssignment.setActive(0);
            userCertificateAssignmentRepository.save(existingUserCertificateAssignment);

            UserCertificateAssignmentMessageModel response = UserCertificateAssignmentMessageModel.builder()
                    .message_details(new MessageDetails(deletedMessage, "S"))
                    .build();

            return response;
        } else{
            if(userGroupType){
                throw new UserCertificationAssignmentException(1002,userGroup);
            }else{
                throw new UserCertificationAssignmentException(1003,user);
            }
        }
    }
    @Override
    public UserCertificateAssignment retrieveByUser(String site, String user) {
        if(userCertificateAssignmentRepository.existsByActiveAndSiteAndUser(1,site,user)){
            return userCertificateAssignmentRepository.findByActiveAndSiteAndUser(1,site,user);
        }
        else {
            throw new UserCertificationAssignmentException(1003,user);
        }
    }
    @Override
    public UserCertificateAssignment retrieveByUserGroup(String site, String userGroup) {
        if(userCertificateAssignmentRepository.existsByActiveAndSiteAndUserGroup(1,site,userGroup)){
            return userCertificateAssignmentRepository.findByActiveAndSiteAndUserGroup(1,site,userGroup);
        }
        else {
            throw new UserCertificationAssignmentException(1002,userGroup);
        }
    }

}
