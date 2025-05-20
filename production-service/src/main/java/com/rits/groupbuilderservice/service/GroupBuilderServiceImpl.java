package com.rits.groupbuilderservice.service;

import com.rits.groupbuilderservice.dto.GroupBuilderRequest;
import com.rits.groupbuilderservice.dto.PreviewGroupRequest;
import com.rits.groupbuilderservice.exception.GroupBuilderException;
import com.rits.groupbuilderservice.model.GroupBuilder;
import com.rits.groupbuilderservice.model.MessageDetails;
import com.rits.groupbuilderservice.model.MessageModel;
import com.rits.groupbuilderservice.repository.GroupBuilderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.bson.Document;
@Service
@RequiredArgsConstructor
public class GroupBuilderServiceImpl implements GroupBuilderService {
    private final GroupBuilderRepository groupBuilderRepository;
    private final MessageSource localMessageSource;
    private final MongoTemplate mongoTemplate;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public MessageModel create(GroupBuilderRequest request) {
        String handle = createHandle(request);
        GroupBuilder existingGroupBuilder = groupBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingGroupBuilder != null) {
            throw new GroupBuilderException(2006, request.getGroupLabel());
        }
        GroupBuilder groupBuilder = groupBuilder(request);
        groupBuilder.setHandle(handle);
        groupBuilder.setCreatedBy(request.getUserId());
        groupBuilder.setCreatedDateTime(LocalDateTime.now());
        groupBuilder.setModifiedBy(request.getUserId());
        groupBuilder.setModifiedDateTime(LocalDateTime.now());


        groupBuilderRepository.save(groupBuilder);

        String createMessage = getFormattedMessage(1, request.getGroupLabel());
        return MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).groupBuilder(groupBuilder).build();
    }
    @Override
    public MessageModel update(GroupBuilderRequest request) {

        String handle = createHandle(request);
        GroupBuilder existingGroupBuilder = groupBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingGroupBuilder == null) {
            throw new GroupBuilderException(2007, request.getGroupLabel());
        }

        GroupBuilder groupedBuilder = groupBuilder(request);
        groupedBuilder.setHandle(handle);
        groupedBuilder.setCreatedBy(existingGroupBuilder.getCreatedBy());
        groupedBuilder.setCreatedDateTime(existingGroupBuilder.getCreatedDateTime());
        groupedBuilder.setModifiedBy(request.getUserId());
        groupedBuilder.setModifiedDateTime(LocalDateTime.now());

        groupBuilderRepository.save(groupedBuilder);

        String updateMessage = getFormattedMessage(2, request.getGroupLabel());
        return MessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).groupBuilder(groupedBuilder).build();
    }
    @Override
    public MessageModel delete(GroupBuilderRequest request) {

        String handle = createHandle(request);
        GroupBuilder existingGroupBuilder = groupBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingGroupBuilder == null) {
            throw new GroupBuilderException(2007, request.getGroupLabel());
        }
        existingGroupBuilder.setActive(0);
        existingGroupBuilder.setModifiedDateTime(LocalDateTime.now());

        groupBuilderRepository.save(existingGroupBuilder);

        String deleteMessage = getFormattedMessage(3, request.getGroupLabel());
        return MessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
    }
    @Override
    public List<GroupBuilder> retrieveAll(String site) {

        List<GroupBuilder> existingGroupBuilderList = groupBuilderRepository.findBySiteAndActive(site, 1);
        return existingGroupBuilderList;
    }

    @Override
    public GroupBuilder retrieve(GroupBuilderRequest request) {

        String handle = createHandle(request);
        GroupBuilder existingGroupBuilder = groupBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
        if(existingGroupBuilder == null){
            throw new GroupBuilderException(2004);
        }
        return existingGroupBuilder;
    }

    @Override
    public List<GroupBuilder> retrieveTop50(String site) {
        List<GroupBuilder> existingGroupBuilderList = groupBuilderRepository.findTop50BySiteAndActive(site, 1);
        return existingGroupBuilderList;
    }

    @Override
    public boolean isGroupBuilderExist(String site, String groupLabel) {
        if(!StringUtils.hasText(groupLabel)){
            throw new GroupBuilderException(2008);
        }
        return groupBuilderRepository.existsBySiteAndActiveAndGroupLabel(site, 1, groupLabel);
    }
    private GroupBuilder groupBuilder(GroupBuilderRequest request) {
        GroupBuilder groupBuilder = GroupBuilder.builder()
                .site(request.getSite())
                .groupLabel(request.getGroupLabel())
                .sectionIds(request.getSectionIds())
                .userId(request.getUserId())
                .active(1)
                .build();

        return groupBuilder;
    }

    private String createHandle(GroupBuilderRequest groupBuilderRequest){
        validateRequest(groupBuilderRequest);
        String groupLabelBO = "GroupBO:" + groupBuilderRequest.getSite() + "," + groupBuilderRequest.getGroupLabel();
        return groupLabelBO;
    }

    public boolean validateRequest(GroupBuilderRequest request){
        if(!StringUtils.hasText(request.getSite())){
            throw new GroupBuilderException(2001);
        }
        if(!StringUtils.hasText(request.getGroupLabel())) {
            throw new GroupBuilderException(2008);
        }
        return true;
    }

    @Override
    public List<Document> previewGroups(PreviewGroupRequest request) {
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("_id").in(request.getSectionsIds())
        );

        UnwindOperation unwindStage = Aggregation.unwind("componentIds");

        AddFieldsOperation addComponentId = Aggregation.addFields()
                .addFieldWithValue("componentId", "$componentIds.handle")
                .build();

        LookupOperation lookupStage = Aggregation.lookup(
                "R_COMPONENT", // from collection
                "componentId",         // localField
                "_id",                 // foreignField
                "componentDetails"     // as
        );

        UnwindOperation unwindComponentDetails = Aggregation.unwind("componentDetails");

        GroupOperation groupStage = Aggregation.group("sectionLabel")
                .push("componentDetails").as("components");

        AddFieldsOperation addSectionLabelBack = Aggregation.addFields()
                .addFieldWithValue("sectionLabel", "$_id")
                .build();

        ProjectionOperation projectStage = Aggregation.project("sectionLabel", "components");

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                unwindStage,
                addComponentId,
                lookupStage,
                unwindComponentDetails,
                groupStage,
                addSectionLabelBack,
                projectStage
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "R_SECTION_BUILDER",
                Document.class
        );

        return results.getMappedResults();
    }
}
