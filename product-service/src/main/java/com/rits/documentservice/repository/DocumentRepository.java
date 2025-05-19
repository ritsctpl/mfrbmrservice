package com.rits.documentservice.repository;

import com.rits.documentservice.dto.DocumentResponse;
import com.rits.documentservice.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentRepository extends MongoRepository<Document, String> {
    public Document findByActiveAndSiteAndDocumentAndVersion(int active, String site, String document, String version);

    public List<DocumentResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    public List<DocumentResponse> findByActiveAndSiteAndDocumentContainingIgnoreCase(int active, String site, String document);

    public Document findByActiveAndSiteAndDocumentAndCurrentVersion(int active, String site, String document, boolean currentVersion);

    public boolean existsByActiveAndSiteAndDocumentAndVersion(int active, String site, String document, String version);

    public List<Document> findByActiveAndSiteAndDocument(int active, String site, String document);

    public List<DocumentResponse> findByActiveAndSite(int active, String site);
}
