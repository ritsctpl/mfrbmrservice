package com.rits.groupbuilderservice.service;

import com.rits.groupbuilderservice.dto.GroupBuilderRequest;
import com.rits.groupbuilderservice.dto.PreviewGroupRequest;
import com.rits.groupbuilderservice.exception.GroupBuilderException;
import com.rits.groupbuilderservice.model.GroupBuilder;
import com.rits.groupbuilderservice.model.MessageDetails;
import com.rits.groupbuilderservice.model.MessageModel;
import com.rits.groupbuilderservice.model.SectionBuilder;
import com.rits.groupbuilderservice.repository.GroupBuilderRepository;

import com.rits.templatebuilderservice.dto.GroupList;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        String groupLabelBO = "GroupBO:" + groupBuilderRequest.getSite() + "," + groupBuilderRequest.getGroupLabel().replaceAll("\\s+", "_");
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
        // Extract handles preserving duplicates
        List<String> sectionIds = request.getSectionsIds().stream()
                .map(SectionBuilder::getHandle)
                .collect(Collectors.toList());

        if (sectionIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Create order mapping with duplicate support
        Map<String, List<Integer>> orderMap = new HashMap<>();
        for (int i = 0; i < sectionIds.size(); i++) {
            String id = sectionIds.get(i);
            orderMap.computeIfAbsent(id, k -> new ArrayList<>()).add(i);
        }

        // First get all unique sections
        List<Document> uniqueSections = mongoTemplate.find(
                Query.query(Criteria.where("_id").in(new ArrayList<>(new LinkedHashSet<>(sectionIds)))),
                Document.class,
                "R_SECTION_BUILDER"
        );

        // Build a map of sections by ID
        Map<String, Document> sectionMap = uniqueSections.stream()
                .collect(Collectors.toMap(doc -> doc.getString("_id"), Function.identity()));

        // Reconstruct results with duplicates
        List<Document> result = new ArrayList<>();
        for (String id : sectionIds) {
            Document section = sectionMap.get(id);
            if (section != null) {
                // Create a new document to avoid reference sharing
                result.add(new Document(section));
            }
        }

        // Now process components for all unique sections
        if (!uniqueSections.isEmpty()) {
            MatchOperation matchStage = Aggregation.match(
                    Criteria.where("_id").in(new ArrayList<>(sectionMap.keySet()))
            );

            // Rest of your aggregation pipeline...
            UnwindOperation unwindStage = Aggregation.unwind("componentIds");
            LookupOperation lookupStage = Aggregation.lookup(
                    "R_COMPONENT",
                    "componentIds.handle",
                    "_id",
                    "componentDetails"
            );
            UnwindOperation unwindComponentDetails = Aggregation.unwind("componentDetails");
            GroupOperation groupStage = Aggregation.group("_id")
                    .first("sectionLabel").as("sectionLabel")
                    .push("componentDetails").as("components");

            Aggregation aggregation = Aggregation.newAggregation(
                    matchStage,
                    unwindStage,
                    lookupStage,
                    unwindComponentDetails,
                    groupStage
            );

            AggregationResults<Document> componentResults = mongoTemplate.aggregate(
                    aggregation,
                    "R_SECTION_BUILDER",
                    Document.class
            );

            // Map components to sections
            Map<String, List<Document>> componentsMap = componentResults.getMappedResults()
                    .stream()
                    .collect(Collectors.toMap(
                            doc -> doc.getString("_id"),
                            doc -> doc.getList("components", Document.class)
                    ));

            // Merge components into final results
            for (Document doc : result) {
                List<Document> components = componentsMap.get(doc.getString("_id"));
                doc.put("components", components != null ? components : Collections.emptyList());
            }
        }

        return result;
    }
}
