package com.rits.queryBuilder.service;

import com.rits.queryBuilder.dto.QueryBuilderDTO;
import com.rits.queryBuilder.model.MessageDetails;
import com.rits.queryBuilder.model.MessageModel;
import com.rits.queryBuilder.model.QueryBuilder;
import com.rits.queryBuilder.repository.QueryBuilderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryBuilderService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QueryBuilderRepository repository;

    public MessageModel createQueryBuilder(QueryBuilderDTO dto) {
        if (dto == null || dto.getTemplateName() == null || dto.getTemplateName().isEmpty()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("TemplateName cannot be null or empty", "E"))
                    .build();
        }

        Optional<QueryBuilder> existingQueryBuilder = repository.findByTemplateNameAndSite(dto.getTemplateName(), dto.getSite());
        if (existingQueryBuilder.isPresent()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("TemplateName already exists", "E"))
                    .build();
        }

        QueryBuilder queryBuilder = new QueryBuilder(
                null,
                dto.getSite(),
                dto.getTemplateName(),
                dto.getTemplateType(),
                dto.getValue(),
                dto.getStatus()
        );

        try {
            QueryBuilder savedQueryBuilder = repository.save(queryBuilder);
            return MessageModel.builder()
                    .message_details(new MessageDetails("Query Builder Created Successfully", "S"))
                    .response(savedQueryBuilder)
                    .build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error creating Query Builder: " + e.getMessage(), "E"))
                    .build();
        }
    }

    public MessageModel updateQueryBuilder(String templateName, QueryBuilderDTO dto) {
        if (templateName == null || templateName.isEmpty()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("TemplateName cannot be null or empty", "E"))
                    .build();
        }

        Optional<QueryBuilder> optionalQueryBuilder = repository.findByTemplateNameAndSite(templateName, dto.getSite());
        if (!optionalQueryBuilder.isPresent()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Query Builder not found with templateName: " + templateName, "E"))
                    .build();
        }

        QueryBuilder queryBuilder = optionalQueryBuilder.get();

        if (dto == null) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("QueryBuilderDTO cannot be null", "E"))
                    .build();
        }

        queryBuilder.setTemplateName(dto.getTemplateName());
        queryBuilder.setTemplateType(dto.getTemplateType());
        queryBuilder.setValue(dto.getValue());
        queryBuilder.setStatus(dto.getStatus());

        try {
            QueryBuilder updatedQueryBuilder = repository.save(queryBuilder);
            return MessageModel.builder()
                    .message_details(new MessageDetails("Query Builder Updated Successfully", "S"))
                    .response(updatedQueryBuilder)
                    .build();
        } catch (Exception e) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Error updating Query Builder: " + e.getMessage(), "E"))
                    .build();
        }
    }

    public MessageModel deleteQueryBuilderByTemplateName(String templateName, String site) {
        if (templateName == null || templateName.isEmpty()) {
            return MessageModel.builder()
                    .message_details(new MessageDetails("TemplateName cannot be null or empty", "E"))
                    .build();
        }

        Optional<QueryBuilder> queryBuilderOpt = repository.findByTemplateNameAndSite(templateName, site);
        if (queryBuilderOpt.isPresent()) {
            try {
                repository.delete(queryBuilderOpt.get());
                return MessageModel.builder()
                        .message_details(new MessageDetails("Query Builder Deleted Successfully", "S"))
                        .build();
            } catch (Exception e) {
                return MessageModel.builder()
                        .message_details(new MessageDetails("Error deleting Query Builder: " + e.getMessage(), "E"))
                        .build();
            }
        } else {
            return MessageModel.builder()
                    .message_details(new MessageDetails("Query Builder Not Found", "E"))
                    .build();
        }
    }

    public List<QueryBuilder> retrieveAllQueryBuilders(String site) {
        try {
            return repository.findBySite(site);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Map<String, Object>> retrieveQueryBuilderByTemplateName(String templateName, String site) {
        if (templateName == null || templateName.isEmpty()) {
            return List.of();
        }

        Optional<QueryBuilder> queryBuilderOpt = repository.findByTemplateNameAndSite(templateName, site);
        if (queryBuilderOpt.isPresent()) {
            QueryBuilder queryBuilder = queryBuilderOpt.get();
            try {
                return jdbcTemplate.queryForList(queryBuilder.getValue());
            } catch (Exception e) {
                return List.of();
            }
        }
        return List.of();
    }

}
