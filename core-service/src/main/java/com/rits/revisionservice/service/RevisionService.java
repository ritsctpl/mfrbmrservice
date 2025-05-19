package com.rits.revisionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface RevisionService {
    public String addRevisionExtension(String request) throws JsonProcessingException;
}
