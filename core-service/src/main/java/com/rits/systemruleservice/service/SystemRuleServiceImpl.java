package com.rits.systemruleservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.systemruleservice.model.MessageDetails;
import com.rits.systemruleservice.model.MessageModel;
import com.rits.systemruleservice.model.SystemRule;
import com.rits.systemruleservice.model.SystemRuleGroup;
import com.rits.systemruleservice.repository.SystemRuleGroupRepository;
import com.rits.systemruleservice.repository.SystemRuleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
@RequiredArgsConstructor
@Service
public class SystemRuleServiceImpl implements SystemRuleService {

    private final SystemRuleRepository systemRuleRepository;
    private final SystemRuleGroupRepository systemRuleGroupRepository;



    @Value("${SystemRuleFilePath}")
    private String systemRuleFilePath;

    @Value("${systemRuleGroupFilePath}")
    private String systemRuleGroupFilePath;


    @PostConstruct
    public Boolean uploadSystemRuleRecordsToDataBaseOnLoad()
    {
        ObjectMapper objectMapper = new ObjectMapper();
       // File jsonFile = new File(systemRuleFilePath);

        try {
        //    List<SystemRule> records = objectMapper.readValue(jsonFile, new TypeReference<List<SystemRule>>() {});
            List<SystemRule> records = objectMapper.readValue(systemRuleFilePath, new TypeReference<List<SystemRule>>() {});
            if(records == null || records.isEmpty()){
                return false;
            }
            systemRuleRepository.saveAll(records);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }



    @Override
    public MessageModel uploadSystemRuleRecords(List<SystemRule> systemRuleRequests) throws Exception
    {
        Boolean inserted = false;
        if(systemRuleRequests != null && !systemRuleRequests.isEmpty())
        {
            systemRuleRepository.saveAll(systemRuleRequests);
            inserted = true;
        }
        if(inserted)
        {
            return MessageModel.builder().message_details(new MessageDetails("Records Updated Successfully" , "S")).build();
        }
        return MessageModel.builder().message_details(new MessageDetails("Failed To Update Records" , "E")).build();
    }

    @PostConstruct
    public Boolean uploadSystemRuleGroupRecordsToDataBaseOnLoad()
    {
        ObjectMapper objectMapper = new ObjectMapper();
      //  File jsonFile = new File(systemRuleGroupFilePath);

        try {
            //List<SystemRuleGroup> records = objectMapper.readValue(jsonFile, new TypeReference<List<SystemRuleGroup>>() {});
            List<SystemRuleGroup> records = objectMapper.readValue(systemRuleGroupFilePath, new TypeReference<List<SystemRuleGroup>>() {});
            if(records == null || records.isEmpty()){
                return false;
            }
            systemRuleGroupRepository.saveAll(records);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public MessageModel uploadSystemRuleGroupRecords(List<SystemRuleGroup> systemRuleGroupRequests) throws Exception
    {
        Boolean inserted = false;
        if(systemRuleGroupRequests != null && !systemRuleGroupRequests.isEmpty())
        {
            systemRuleGroupRepository.saveAll(systemRuleGroupRequests);
            inserted = true;
        }
        if(inserted)
        {
            return MessageModel.builder().message_details(new MessageDetails("Records Updated Successfully" , "S")).build();
        }
        return MessageModel.builder().message_details(new MessageDetails("Failed To Update Records" , "E")).build();
    }

    @Override
    public List<SystemRule> retrieveSystemRule(String site)throws Exception
    {
        List<SystemRule> retrieveBySite = systemRuleRepository.findByRequestSystemRuleSettingSite(site);
        return retrieveBySite;
    }







}
