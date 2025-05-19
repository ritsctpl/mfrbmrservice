package com.rits.queryBuilder.controller;

import com.rits.queryBuilder.dto.QueryBuilderDTO;
import com.rits.queryBuilder.exception.QueryBuilderException;
import com.rits.queryBuilder.model.MessageModel;
import com.rits.queryBuilder.model.QueryBuilder;
import com.rits.queryBuilder.service.QueryBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @PostMapping("/retrieve")
    public ResponseEntity<?> getQueryBuilder(@RequestBody QueryBuilderDTO dto) {
        if(dto.getSite() == null || dto.getSite().isEmpty()){
            throw new QueryBuilderException(5);
        }
        List<Map<String, Object>> resultList = queryBuilderService.retrieveQueryBuilderByTemplateName(dto.getTemplateName(), dto.getSite());
        if (resultList.isEmpty()) {
            return new ResponseEntity<>(resultList, HttpStatus.OK);
        }
        return ResponseEntity.ok(resultList);
    }


}