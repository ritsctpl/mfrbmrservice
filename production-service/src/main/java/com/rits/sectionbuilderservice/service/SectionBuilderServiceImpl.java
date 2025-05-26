package com.rits.sectionbuilderservice.service;

import com.rits.sectionbuilderservice.dto.PreviewResponse;
import com.rits.sectionbuilderservice.dto.SectionBuilderRequest;
import com.rits.sectionbuilderservice.dto.SectionResponse;
import com.rits.sectionbuilderservice.exception.SectionBuilderException;
import com.rits.sectionbuilderservice.model.MessageDetails;
import com.rits.sectionbuilderservice.model.MessageModel;
import com.rits.sectionbuilderservice.model.SectionBuilder;

import com.rits.sectionbuilderservice.repository.SectionBuilderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Service
@RequiredArgsConstructor
public class SectionBuilderServiceImpl implements SectionBuilderService {
    private final SectionBuilderRepository sectionBuilderRepository;
    private final MessageSource localMessageSource;

    private final MongoTemplate mongoTemplate;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public MessageModel create(SectionBuilderRequest request) {
        String handle = createHandle(request);
        SectionBuilder existingSectionBuilder = sectionBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingSectionBuilder != null) {
            throw new SectionBuilderException(2002, request.getSectionLabel());
        }
        SectionBuilder sectionBuilder = sectionBuilder(request);
        sectionBuilder.setHandle(handle);
        sectionBuilder.setCreatedBy(request.getUserId());
        sectionBuilder.setCreatedDateTime(LocalDateTime.now());
        sectionBuilder.setModifiedBy(request.getUserId());
        sectionBuilder.setModifiedDateTime(LocalDateTime.now());


        sectionBuilderRepository.save(sectionBuilder);

        String createMessage = getFormattedMessage(1, request.getSectionLabel());
        return MessageModel.builder().message_details(new MessageDetails(createMessage, "S")).sectionBuilder(sectionBuilder).build();
    }
    @Override
    public MessageModel update(SectionBuilderRequest request) {

        String handle = createHandle(request);
        SectionBuilder existingSectionBuilder = sectionBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingSectionBuilder == null) {
            throw new SectionBuilderException(2003, request.getSectionLabel());
        }

        SectionBuilder sectionBuilder = sectionBuilder(request);
        sectionBuilder.setHandle(handle);
        sectionBuilder.setCreatedBy(existingSectionBuilder.getCreatedBy());
        sectionBuilder.setCreatedDateTime(existingSectionBuilder.getCreatedDateTime());
        sectionBuilder.setModifiedBy(request.getUserId());
        sectionBuilder.setModifiedDateTime(LocalDateTime.now());

        sectionBuilderRepository.save(sectionBuilder);

        String updateMessage = getFormattedMessage(2, request.getSectionLabel());
        return MessageModel.builder().message_details(new MessageDetails(updateMessage, "S")).sectionBuilder(sectionBuilder).build();
    }
    @Override
    public MessageModel delete(SectionBuilderRequest request) {

        String handle = createHandle(request);
        SectionBuilder existingSectionBuilder = sectionBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);

        if (existingSectionBuilder == null) {
            throw new SectionBuilderException(2003, request.getSectionLabel());
        }
        existingSectionBuilder.setActive(0);
        existingSectionBuilder.setModifiedDateTime(LocalDateTime.now());

        sectionBuilderRepository.save(existingSectionBuilder);

        String deleteMessage = getFormattedMessage(3, request.getSectionLabel());
        return MessageModel.builder().message_details(new MessageDetails(deleteMessage, "S")).build();
    }
    @Override
    public List<SectionResponse> retrieveAll(String site, String sectionLabel) {

        List<SectionResponse> existingLineClearanceList = sectionBuilderRepository.findBySiteAndSectionLabelContainingIgnoreCaseAndActiveEquals(site, sectionLabel,1);
        return existingLineClearanceList;
    }

    @Override
    public SectionBuilder retrieve(SectionBuilderRequest request) {

        String handle = createHandle(request);
        SectionBuilder existingSectionBuilder = sectionBuilderRepository.findByHandleAndSiteAndActive(handle, request.getSite(), 1);
        if(existingSectionBuilder == null){
            throw new SectionBuilderException(2004);
        }
        return existingSectionBuilder;
    }

    @Override
    public List<SectionResponse> retrieveTop50(String site) {
        List<SectionResponse> existingSectionBuilderList = sectionBuilderRepository.findTop50BySiteAndActive(site, 1);
        return existingSectionBuilderList;
    }

    @Override
    public boolean isSectionBuilderExist(String site, String sectionLabel) {
        if(!StringUtils.hasText(sectionLabel)){
            throw new SectionBuilderException(2005);
        }
        return sectionBuilderRepository.existsBySiteAndActiveAndSectionLabel(site, 1, sectionLabel);
    }
    private SectionBuilder sectionBuilder(SectionBuilderRequest request) {
        SectionBuilder sectionBuilder = SectionBuilder.builder()
                .site(request.getSite())
                .sectionLabel(request.getSectionLabel())
                .instructions(request.getInstructions())
                .effectiveDateTime(request.getEffectiveDateTime())
                .componentIds(request.getComponentIds())
                .userId(request.getUserId())
                .active(1)
                .build();

        return sectionBuilder;
    }

    private String createHandle(SectionBuilderRequest sectionBuilderRequest){
        validateRequest(sectionBuilderRequest);
        String sectionLabelBO = "SectionBO:" + sectionBuilderRequest.getSite() + "," + sectionBuilderRequest.getSectionLabel().replaceAll("\\s+", "_");
        return sectionLabelBO;
    }

    public boolean validateRequest(SectionBuilderRequest request){
        if(!StringUtils.hasText(request.getSite())){
            throw new SectionBuilderException(2001);
        }
        if(!StringUtils.hasText(request.getSectionLabel())) {
            throw new SectionBuilderException(2005);
        }
        return true;
    }
    @Override
    public PreviewResponse preview(SectionBuilderRequest request) throws Exception {
        // Add componentIds from request
        AddFieldsOperation addFields = Aggregation.addFields()
                .addFieldWithValue("componentIds", request.getComponentIds())
                .build();

        // Lookup components using $lookup with pipeline (more efficient than simple lookup)
        AggregationOperation lookupOperation = context -> new Document("$lookup",
                new Document("from", "R_COMPONENT")
                        .append("let", new Document("handles", "$componentIds.handle"))
                        .append("pipeline", Collections.singletonList(
                                new Document("$match",
                                        new Document("$expr",
                                                new Document("$in", Arrays.asList("$_id", "$$handles"))
                                        )
                                )
                        ))
                        .append("as", "componentList")
        );

        // Project to include all necessary fields
        ProjectionOperation projectFields = Aggregation.project()
//                .and("_id").as("handle")
//                .and("site").as("site")
//                .and("sectionLabel").as("sectionLabel")
//                .and("instructions").as("instructions")
//                .and("effectiveDateTime").as("effectiveDateTime")
//                .and("componentIds").as("componentIds")
                .and("componentList").as("componentList");
//                .and("userId").as("userId")
//                .and("active").as("active")
//                .and("createdDateTime").as("createdDateTime")
//                .and("modifiedDateTime").as("modifiedDateTime")
//                .and("createdBy").as("createdBy")
//                .and("modifiedBy").as("modifiedBy");

        // Build aggregation with only needed stages
        Aggregation aggregation = Aggregation.newAggregation(
                addFields,     // First: add componentIds
                lookupOperation, // Second: lookup components
                projectFields  // Third: project the fields
        );

        AggregationResults<PreviewResponse> results = mongoTemplate.aggregate(
                aggregation,
                "R_SECTION_BUILDER",
                PreviewResponse.class
        );

        if (results.getMappedResults().isEmpty()) {
            throw new Exception("Section not found");
        }

        return results.getMappedResults().get(0);
    }
}
