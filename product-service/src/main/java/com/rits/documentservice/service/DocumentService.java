package com.rits.documentservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.documentservice.dto.*;
import com.rits.documentservice.model.Document;
import com.rits.documentservice.model.MessageModel;

import java.util.List;

public interface DocumentService {
    public MessageModel createDocument(DocumentRequest documentRequest) throws Exception;

    public MessageModel updateDocument(DocumentRequest documentRequest) throws Exception;

    public Document retrieveDocument(String site, String document, String version) throws Exception;

    public DocumentResponseList getAllDocument(String site, String document) throws Exception;

    public DocumentResponseList getAllDocumentByCreatedDate(String site) throws Exception;

    public MessageModel deleteDocument(String site, String document, String version,String userId) throws Exception;

    public List<DocumentResponse> getAllDocuments(String site) throws Exception;


    public String callExtension(Extension preExtension) throws Exception;
}
