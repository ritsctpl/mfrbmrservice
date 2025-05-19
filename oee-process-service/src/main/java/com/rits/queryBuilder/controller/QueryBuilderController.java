package com.rits.queryBuilder.controller;

import com.rits.queryBuilder.dto.DataSourceDTO;
import com.rits.queryBuilder.dto.ManageRequest;
import com.rits.queryBuilder.dto.QueryBuilderDTO;
import com.rits.queryBuilder.exception.QueryBuilderException;
import com.rits.queryBuilder.dto.MessageModel;
import com.rits.queryBuilder.model.QueryBuilder;
import com.rits.queryBuilder.service.QueryBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/queryBuilder-service")
public class QueryBuilderController {

    private final QueryBuilderService queryBuilderService;

    @PostMapping("/create")
    public ResponseEntity<MessageModel> createQueryBuilder(@RequestBody QueryBuilderDTO dto) {
        if(dto.getSite() == null || dto.getSite().isEmpty()){
            throw new QueryBuilderException(5);
        }
        MessageModel queryBuilder = queryBuilderService.createQueryBuilder(dto);
        return new ResponseEntity<>(queryBuilder, HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<MessageModel> updateQueryBuilder(@RequestBody QueryBuilderDTO dto) {
        if(dto.getSite() == null || dto.getSite().isEmpty()){
        throw new QueryBuilderException(5);
        }
        String templateName = dto.getTemplateName();
        MessageModel queryBuilder = queryBuilderService.updateQueryBuilder(templateName, dto);
        return new ResponseEntity<>(queryBuilder, HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<MessageModel> deleteQueryBuilder(@RequestBody QueryBuilderDTO dto) {
        if(dto.getSite() == null || dto.getSite().isEmpty()){
            throw new QueryBuilderException(5);
        }
        MessageModel response = queryBuilderService.deleteQueryBuilderByTemplateName(dto.getTemplateName(), dto.getSite());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/retrieveAll")
    public ResponseEntity<List<QueryBuilder>> getAllQueryBuilders(@RequestBody QueryBuilderDTO dto) {
        if(dto.getSite() == null || dto.getSite().isEmpty()){
            throw new QueryBuilderException(5);
        }
        List<QueryBuilder> queryBuilders = queryBuilderService.retrieveAllQueryBuilders(dto.getSite());
        return new ResponseEntity<>(queryBuilders, HttpStatus.OK);
    }

    @PostMapping("/fetchDataByTemplateOrQuery")
    public ResponseEntity<?> fetchDataByTemplateOrQuery(@RequestBody QueryBuilderDTO dto) {
        if(StringUtils.isBlank(dto.getSite()))
            throw new QueryBuilderException(5);

        try {
            List<Map<String, Object>> resultList = queryBuilderService.fetchDataByTemplateOrQuery(dto);
            return ResponseEntity.ok(resultList);
        } catch (QueryBuilderException queryBuilderException) {
            throw queryBuilderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getTablesAndColumns")
    public ResponseEntity<?> getTablesAndColumns(@RequestBody QueryBuilderDTO dto) {
//        if(StringUtils.isBlank(dto.getSite()))
//            throw new QueryBuilderException(5);

        try {
            MessageModel response = queryBuilderService.getTablesAndColumns(dto);
            return ResponseEntity.ok(response);
        } catch (QueryBuilderException queryBuilderException) {
            throw queryBuilderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    

    @PostMapping("/createDataSource")
    public ResponseEntity<MessageModel> createDataSource(@RequestBody DataSourceDTO dto) {
        if(StringUtils.isBlank(dto.getSite()))
            throw new QueryBuilderException(5);

        try {
            return ResponseEntity.ok(queryBuilderService.createDataSource(dto));
        } catch (QueryBuilderException queryBuilderException) {
            throw queryBuilderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/updateDataSource")
    public ResponseEntity<MessageModel> updateById(@RequestBody DataSourceDTO dto) {
        if(StringUtils.isBlank(dto.getSite()))
            throw new QueryBuilderException(5);

        try {
            MessageModel dataSource = queryBuilderService.updateDataSource(dto);
            return ResponseEntity.ok(dataSource);
        } catch (QueryBuilderException queryBuilderException) {
            throw queryBuilderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/deleteDataSource")
    public ResponseEntity<MessageModel> deleteById(@RequestBody DataSourceDTO dto) {
        if(StringUtils.isBlank(dto.getSite()))
            throw new QueryBuilderException(5);

        try {
            MessageModel response = queryBuilderService.deleteDataSourceById(dto);
            return ResponseEntity.ok(response);
        } catch (QueryBuilderException queryBuilderException) {
            throw queryBuilderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveAllDataSource")
    public ResponseEntity<MessageModel> retrieveAllDataSource(@RequestBody DataSourceDTO dto) {
        if(StringUtils.isBlank(dto.getSite()))
            throw new QueryBuilderException(5);

        try {
            MessageModel response = queryBuilderService.retrieveAllDataSource(dto);
            return ResponseEntity.ok(response);
        } catch (QueryBuilderException queryBuilderException) {
            throw queryBuilderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveDataSource")
    public ResponseEntity<MessageModel> retrieveDataSource(@RequestBody DataSourceDTO dto) {
        if(StringUtils.isBlank(dto.getSite()))
            throw new QueryBuilderException(5);

        try {
            MessageModel response = queryBuilderService.retrieveDataSource(dto);
            return ResponseEntity.ok(response);
        } catch (QueryBuilderException queryBuilderException) {
            throw queryBuilderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/dataSourceConfig")
    public ResponseEntity<MessageModel> configureDataSource(@RequestBody DataSourceDTO config) {
        try {
            MessageModel response = queryBuilderService.configureDataSource(config);
            return ResponseEntity.ok(response);
        } catch (QueryBuilderException queryBuilderException) {
            throw queryBuilderException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/applyQueryFilters")
    public String applyQueryFilters(@RequestParam String query, @RequestBody Map<String, Object> parameters) {
        try {
            queryBuilderService.getJdbcTemplate().update(query, parameters.values().toArray());
            return "Query executed successfully!";
        } catch (Exception e) {
            return "Error executing query: " + e.getMessage();
        }
    }

    @PostMapping("createManagementDasgboard")
    public MessageModel createManagementDasgboard(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new QueryBuilderException(5);

        try {
            return queryBuilderService.createManagementDasgboard(manageRequest);
        } catch (QueryBuilderException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("updateManagementDasgboard")
    public MessageModel updateManagementDasgboard(@RequestBody ManageRequest managementRequest)
    {
        if(StringUtils.isBlank(managementRequest.getSite()))
            throw new QueryBuilderException(5);

        try {
            return queryBuilderService.updateManagementDasgboard(managementRequest);
        } catch (QueryBuilderException managementException) {
            throw managementException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("deleteManagementDasgboard")
    public MessageModel deleteManagementDasgboard(@RequestBody ManageRequest managementRequest)
    {
        if(StringUtils.isBlank(managementRequest.getSite()))
            throw new QueryBuilderException(5);

        try {
            return queryBuilderService.deleteManagementDasgboard(managementRequest);
        } catch (QueryBuilderException managementException) {
            throw managementException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveForColorScheme")
    public MessageModel retrieveForColorScheme(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new QueryBuilderException(5);

        try {
            return queryBuilderService.retrieveForColorScheme(manageRequest);
        } catch (QueryBuilderException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveForFilter")
    public MessageModel retrieveForFilter(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new QueryBuilderException(5);

        try {
            return queryBuilderService.retrieveForFilter(manageRequest);
        } catch (QueryBuilderException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveManagementDasgboard")
    public MessageModel retrieveManagementDasgboard(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new QueryBuilderException(5);

        try {
            return queryBuilderService.retrieveManagementDasgboard(manageRequest);
        } catch (QueryBuilderException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAllManagementDasgboard")
    public MessageModel retrieveAllManagementDasgboard(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new QueryBuilderException(5);

        try {
            return queryBuilderService.retrieveAllManagementDasgboard(manageRequest);
        } catch (QueryBuilderException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}