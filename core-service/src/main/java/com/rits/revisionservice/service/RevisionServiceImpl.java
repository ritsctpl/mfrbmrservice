package com.rits.revisionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.revisionservice.dto.ItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class RevisionServiceImpl implements RevisionService{
    private final WebClient.Builder webClientBuilder;
    @Override
    public String addRevisionExtension(String request) throws JsonProcessingException {

        String requestJson =request;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ItemRequest itemRequest = objectMapper.readValue(requestJson, ItemRequest.class);
       String revision= itemRequest.getRevision();
       itemRequest.setRevision(revision+"A");
        String updatedRequestJson = objectMapper.writeValueAsString(itemRequest);
        return updatedRequestJson;

    }
}
