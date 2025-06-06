package com.rits.templatebuilderservice.service;


import com.rits.templatebuilderservice.dto.GroupList;
import com.rits.templatebuilderservice.dto.TemplateRequest;
import com.rits.templatebuilderservice.dto.TemplateResponse;
import com.rits.templatebuilderservice.model.MessageDetails;
import com.rits.templatebuilderservice.model.Template;
import com.rits.templatebuilderservice.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.rits.templatebuilderservice.model.MessageModel;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService{
    private final MongoTemplate mongoTemplate;
    private final TemplateRepository templateRepository;

    public Template createUpdateBuilder(Template template, TemplateRequest templateRequest, Boolean templateExists) {
        if(templateExists){
            template.setHandle(template.getHandle());
            template.setSite(template.getSite());
            template.setTemplateLabel(template.getTemplateLabel());
            template.setTemplateVersion(template.getTemplateVersion());
            template.setTemplateType(templateRequest.getTemplateType());
            template.setGroupIds(templateRequest.getGroupIds());
            template.setProductGroup(templateRequest.getProductGroup());
            template.setCurrentVersion(templateRequest.getCurrentVersion());
            template.setCreatedDateTime(template.getCreatedDateTime());
            template.setUserId(templateRequest.getUserId());
            template.setUpdatedDateTime(LocalDateTime.now());
            template.setActive(template.getActive());
        }
        else{
            template = Template.builder()
                    .handle("TemplateBO:"+templateRequest.getSite()+","+templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion())
                    .templateLabel(templateRequest.getTemplateLabel())
                    .templateType(templateRequest.getTemplateType())
                    .templateVersion(templateRequest.getTemplateVersion())
                    .groupIds(templateRequest.getGroupIds())
                    .site(templateRequest.getSite())
                    .productGroup(templateRequest.getProductGroup())
                    .userId(templateRequest.getUserId())
                    .currentVersion(templateRequest.getCurrentVersion())
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();
        }
        return template;
    }

    @Override
    public MessageModel createTemplate (TemplateRequest templateRequest) throws Exception
    {
        Boolean templateExists = templateRepository.existsByHandleAndSiteAndActiveEquals("TemplateBO:"+ templateRequest.getSite() + "," + templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(), templateRequest.getSite(), 1);
        if(templateRequest.getCurrentVersion())
        {
            updateCurrentVersion(templateRequest);
        }
        if(templateExists)
        {
            throw new Exception("Template with this label already exists");
        }
        else
        {
            Template template = createUpdateBuilder(null, templateRequest, false);
            return MessageModel.builder().message_details(new MessageDetails(templateRequest.getTemplateLabel() + " Created SuccessFully", "S")).response(templateRepository.save(template)).build();
        }
    }

    @Override
    public MessageModel UpdateTemplate (TemplateRequest templateRequest) throws Exception
    {
        Boolean templateExists = templateRepository.existsByHandleAndSiteAndActiveEquals("TemplateBO:"+ templateRequest.getSite() + "," + templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(), templateRequest.getSite(), 1);
        if(templateRequest.getCurrentVersion())
        {
            updateCurrentVersion(templateRequest);
        }
        if(!templateExists)
        {
            throw new Exception("Template with this label does not exists");
        }
        else
        {
            Template retrievedTemplate = templateRepository.findBySiteAndHandleAndActiveEquals(templateRequest.getSite(),"TemplateBO:"+ templateRequest.getSite() + "," + templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(),1);
            Template template = createUpdateBuilder(retrievedTemplate, templateRequest, true);
            return MessageModel.builder().message_details(new MessageDetails(templateRequest.getTemplateLabel() + " Updated SuccessFully", "S")).response(templateRepository.save(template)).build();
        }
    }

    @Override
    public MessageModel deleteTemplate (TemplateRequest templateRequest) throws Exception
    {
        Boolean templateExists = templateRepository.existsByHandleAndSiteAndActiveEquals("TemplateBO:"+ templateRequest.getSite() + "," + templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(), templateRequest.getSite(), 1);
        if(!templateExists)
        {
            throw new Exception("Template with this label does not exists");
        }
        else
        {
            Template template = templateRepository.findBySiteAndHandleAndActiveEquals(templateRequest.getSite(),"TemplateBO:"+ templateRequest.getSite() + "," + templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(),1);
            template.setActive(0);
            return MessageModel.builder().message_details(new MessageDetails(templateRequest.getTemplateLabel() + " Deleted SuccessFully", "S")).response(templateRepository.save(template)).build();
        }
    }

    @Override
    public Template retrieveTemplate(TemplateRequest templateRequest) throws Exception
    {
        return templateRepository.findBySiteAndHandleAndActiveEquals(templateRequest.getSite(),"TemplateBO:"+ templateRequest.getSite() + "," + templateRequest.getTemplateLabel()+","+templateRequest.getTemplateVersion(),1);
    }

    @Override
    public  List<TemplateResponse> retrieveAllTemplate(TemplateRequest templateRequest)throws Exception
    {
       return templateRepository.findBySiteAndTemplateLabelContainingIgnoreCaseAndActiveEqualsOrderByCreatedDateTimeDesc(templateRequest.getSite(), templateRequest.getTemplateLabel(), 1);
    }

    @Override
    public  List<TemplateResponse> retrieveTop50Template(TemplateRequest templateRequest)throws Exception
    {
        return templateRepository.findTop50BySiteAndActiveEqualsOrderByCreatedDateTimeDesc(templateRequest.getSite(), 1);
    }

    public void updateCurrentVersion(TemplateRequest templateRequest)
    {
        List<Template> retrievedTemplate = templateRepository.findBySiteAndTemplateLabelAndCurrentVersionAndActiveEquals(templateRequest.getSite(), templateRequest.getTemplateLabel(), true,1);
        for(Template template:retrievedTemplate)
        {
            template.setCurrentVersion(false);
            templateRepository.save(template);
        }
    }
    @Override
    public List preview(TemplateRequest templateRequest) throws Exception
    {
        List<String> groupIds = null;
        List<Document> finalList = new ArrayList<>();
        if(!templateRequest.getGroupIds().isEmpty())
        {
            groupIds = templateRequest.getGroupIds().stream()
                    .map(GroupList::getHandle)
                    .collect(Collectors.toList());
        }else {
            throw new Exception("No groups found");
        }
        for (GroupList list : templateRequest.getGroupIds()) {
            if (list.getHandle().contains("GroupBO:")) {
//                List<String> groupHandle = groupLists.stream()
//                        .map(GroupList::getHandle)
//                        .collect(Collectors.toList());

                // First get all groups as Documents
                List<Document> groups = mongoTemplate.find(
                        Query.query(Criteria.where("_id").in(groupIds)),
                        Document.class,
                        "R_GROUP_BUILDER"
                );

                // Get all section handles from groups
                List<String> sectionHandles = groups.stream()
                        .flatMap(g -> ((List<Document>)g.get("sectionIds", List.class)).stream())
                        .map(section -> section.getString("handle"))
                        .distinct()
                        .collect(Collectors.toList());

                // Lookup sections with their components
                LookupOperation lookupComponents = LookupOperation.newLookup()
                        .from("R_COMPONENT")
                        .localField("componentIds.handle")
                        .foreignField("_id")
                        .as("components");

                Aggregation sectionAggregation = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("_id").in(sectionHandles)),
                        lookupComponents,
                        Aggregation.addFields()
                                .addField("componentIds")
                                .withValueOf(ArrayOperators.arrayOf("$components").elementAt(0))
                                .build()
                );

                List<Document> sections = mongoTemplate.aggregate(
                        sectionAggregation,
                        "R_SECTION_BUILDER",
                        Document.class
                ).getMappedResults();

                // Build maps for quick lookup
                Map<String, Document> sectionMap = sections.stream()
                        .collect(Collectors.toMap(section -> section.getString("_id"), Function.identity()));

                Map<String, Document> groupMap = groups.stream()
                        .collect(Collectors.toMap(group -> group.getString("_id"), Function.identity()));

                // Attach sections to groups
                groups.forEach(group -> {
                    List<Document> sectionRefs = group.get("sectionIds", List.class);
                    if (sectionRefs != null) {
                        List<Document> fullSections = sectionRefs.stream()
                                .map(sectionRef -> sectionMap.get(sectionRef.getString("handle")))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        group.put("sectionIds", fullSections);
                    }
                });

                // Return in original order
                Document templateDocument = groupIds.stream()
                        .map(groupMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()).get(0);
                finalList.add(templateDocument);
            } else if (list.getHandle().contains("SectionBO:")) {
////                Map<String, List<Integer>> orderMap = new HashMap<>();
////                for (int i = 0; i < groupIds.size(); i++) {
////                    String id = groupIds.get(i);
////                    orderMap.computeIfAbsent(id, k -> new ArrayList<>()).add(i);
////                }
//
//                // First get all unique sections
//                List<Document> uniqueSections = mongoTemplate.find(
//                        Query.query(Criteria.where("_id").in(new ArrayList<>(new LinkedHashSet<>(groupIds)))),
//                        Document.class,
//                        "R_SECTION_BUILDER"
//                );
//
//                // Build a map of sections by ID
//                Map<String, Document> sectionMap = uniqueSections.stream()
//                        .collect(Collectors.toMap(doc -> doc.getString("_id"), Function.identity()));
//
//                // Reconstruct results with duplicates
//                List<Document> result = new ArrayList<>();
//                for (String id : groupIds) {
//                    Document section = sectionMap.get(id);
//                    if (section != null) {
//                        // Create a new document to avoid reference sharing
//                        result.add(new Document(section));
//                    }
//                }
//
//                // Now process components for all unique sections
//                if (!uniqueSections.isEmpty()) {
                    MatchOperation matchStage = Aggregation.match(
                            Criteria.where("_id").in(list.getHandle())
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
                            .first("handle").as("handle")
                            .first("instructions").as("instructions")
                            .first("effectiveDateTime").as("affectiveDateTime")
                            .first("style").as("style")
                            .first("createdDateTime").as("createdDateTime")
                            .first("modifiedDateTime").as("modifiedDateTime")
                            .first("userId").as("userId")
                            .first("active").as("active")
                            .first("structureType").as("structureType")
                            .push("componentDetails").as("components");

                    Aggregation aggregation = Aggregation.newAggregation(
                            matchStage,
                            unwindStage,
                            lookupStage,
                            unwindComponentDetails,
                            groupStage
                    );

                    Document componentResults = mongoTemplate.aggregate(
                            aggregation,
                            "R_SECTION_BUILDER",
                            Document.class
                    ).getMappedResults().get(0);

                    // Map components to sections
//                    Map<String, List<Document>> componentsMap = componentResults.getMappedResults()
//                            .stream()
//                            .collect(Collectors.toMap(
//                                    doc -> doc.getString("_id"),
//                                    doc -> doc.getList("components", Document.class)
//                            ));
//
//                    // Merge components into final results
//                    for (Document doc : result) {
//                        List<Document> components = componentsMap.get(doc.getString("_id"));
//                        doc.put("components", components != null ? components : Collections.emptyList());
//                    }
//                }
                finalList.add(componentResults);
            } else if (list.getHandle().contains("ComponentBO:")) {
                MatchOperation matchStage = Aggregation.match(
                        Criteria.where("_id").in(list.getHandle())
                );

                ProjectionOperation projectStage = Aggregation.project()
                        .and("_id").as("handle")
                        .and("componentLabel").as("componentLabel")
                        .and("dataType").as("dataType")
                        .and("unit").as("unit")
                        .and("defaultValue").as("defaultValue")
                        .and("required").as("required")
                        .and("validation").as("validation")
                        .and("tableConfig").as("tableConfig")
                        .and("apiUrl").as("apiUrl")
                        .and("minValue").as("minValue")
                        .and("maxValue").as("maxValue");

                          Aggregation aggregation = Aggregation.newAggregation(
                        matchStage,
                        projectStage
                );

                Document sectionDocument = mongoTemplate.aggregate(
                        aggregation,
                        "R_COMPONENT",
                        Document.class
                ).getMappedResults().get(0);
                finalList.add(sectionDocument);
            }
        }
        return finalList;
    }

}



