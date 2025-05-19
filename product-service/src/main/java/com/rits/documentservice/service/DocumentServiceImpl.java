package com.rits.documentservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.containermaintenanceservice.dto.AuditLogRequest;
import com.rits.documentservice.dto.*;
import com.rits.documentservice.exception.DocumentException;
import com.rits.documentservice.model.Document;
import com.rits.documentservice.model.MessageDetails;
import com.rits.documentservice.model.MessageModel;
import com.rits.documentservice.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;
    @Value("${auditlog-service.url}/producer")
    private String auditlogUrl;

    @Override
    public MessageModel createDocument(DocumentRequest documentRequest) throws Exception {
        if (documentRepository.existsByActiveAndSiteAndDocumentAndVersion(1, documentRequest.getSite(), documentRequest.getDocument(), documentRequest.getVersion())) {
            throw new DocumentException(901, documentRequest.getDocument(), documentRequest.getVersion());
        } else {
            List<Document> existing = documentRepository.findByActiveAndSiteAndDocument(1, documentRequest.getSite(), documentRequest.getDocument());

            if (documentRequest.getDescription() == null || documentRequest.getDescription().isEmpty()) {
                documentRequest.setDescription(documentRequest.getDocument());
            }

            existing.stream()
                    .map(document -> {
                        document.setCurrentVersion(false);
                        document.setModifiedDateTime(LocalDateTime.now());
                        return document;
                    })
                    .forEach(documentRepository::save);

            Document document = Document.builder()
                    .site(documentRequest.getSite())
                    .document(documentRequest.getDocument())
                    .handle("DocumentBo: " + documentRequest.getSite() + "," + documentRequest.getDocument() + "," + documentRequest.getVersion())
                    .version(documentRequest.getVersion())
                    .description(documentRequest.getDescription())
                    .printQty(documentRequest.getPrintQty())
                    .documentType(documentRequest.getDocumentType())
                    .printBy(documentRequest.getPrintBy())
                    .printMethods(documentRequest.getPrintMethods())
                    .status(documentRequest.getStatus())
                    .currentVersion(documentRequest.isCurrentVersion())
                    .template(documentRequest.getTemplate())
                    .documentOptions(documentRequest.getDocumentOptions())
                    .printIntegration(documentRequest.getPrintIntegration())
                    .customDataList(documentRequest.getCustomDataList())
                    .inUse(documentRequest.isInUse())
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();

            return MessageModel.builder().message_details(new MessageDetails(documentRequest.getDocument()+" with version"+documentRequest.getVersion()+" Created SuccessFully","S")).response(documentRepository.save(document)).build();

        }
    }

    @Override
    public MessageModel updateDocument(DocumentRequest documentRequest) throws Exception {
        if (!documentRepository.existsByActiveAndSiteAndDocumentAndVersion(1, documentRequest.getSite(), documentRequest.getDocument(), documentRequest.getVersion())) {
            throw new DocumentException(902, documentRequest.getDocument(), documentRequest.getVersion());
        }
        Document existingDocument = documentRepository.findByActiveAndSiteAndDocumentAndVersion(1, documentRequest.getSite(), documentRequest.getDocument(), documentRequest.getVersion());
        if (existingDocument.isInUse()) {
            throw new DocumentException(903, documentRequest.getDocument(), documentRequest.getVersion());
        } else {
            if (documentRequest.getDescription() == null || documentRequest.getDescription().isEmpty()) {
                documentRequest.setDescription(documentRequest.getDocument());
            }
            Document document = Document.builder()
                    .site(existingDocument.getSite())
                    .document(existingDocument.getDocument())
                    .version(existingDocument.getVersion())
                    .handle(existingDocument.getHandle())
                    .description(documentRequest.getDescription())
                    .printQty(documentRequest.getPrintQty())
                    .documentType(documentRequest.getDocumentType())
                    .printBy(documentRequest.getPrintBy())
                    .printMethods(documentRequest.getPrintMethods())
                    .status(documentRequest.getStatus())
                    .currentVersion(documentRequest.isCurrentVersion())
                    .template(documentRequest.getTemplate())
                    .documentOptions(documentRequest.getDocumentOptions())
                    .printIntegration(documentRequest.getPrintIntegration())
                    .customDataList(documentRequest.getCustomDataList())
                    .inUse(documentRequest.isInUse())
                    .active(1)
                    .createdDateTime(existingDocument.getCreatedDateTime())
                    .modifiedDateTime(LocalDateTime.now())
                    .build();

            return MessageModel.builder().message_details(new MessageDetails(documentRequest.getDocument()+" with version"+documentRequest.getVersion()+" updated SuccessFully","S")).response(documentRepository.save(document)).build();

        }
    }

    @Override
    public Document retrieveDocument(String site, String document, String version) throws Exception {
        Document existingDocument;
        if (version != null && !version.isEmpty()) {
            existingDocument = documentRepository.findByActiveAndSiteAndDocumentAndVersion(1, site, document, version);
            if (existingDocument == null) {
                throw new DocumentException(902, document, version);

            }
        } else {
            existingDocument = documentRepository.findByActiveAndSiteAndDocumentAndCurrentVersion(1, site, document, true);
            if (existingDocument == null) {
                throw new DocumentException(902, document, "currentVersion");

            }
        }
        return existingDocument;
    }

    @Override
    public DocumentResponseList getAllDocument(String site, String document) throws Exception {
        List<DocumentResponse> documentResponses;
        if (document != null && !document.isEmpty()) {
            documentResponses = documentRepository.findByActiveAndSiteAndDocumentContainingIgnoreCase(1, site, document);

            if (documentResponses.isEmpty()) {
                throw new DocumentException(902, document, "currentVersion");
            }
            return DocumentResponseList.builder().documentResponseList(documentResponses).build();
        } else {
            return getAllDocumentByCreatedDate(site);
        }
    }

    @Override
    public DocumentResponseList getAllDocumentByCreatedDate(String site) throws Exception {
        List<DocumentResponse> documentResponseList = documentRepository.findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);
        return DocumentResponseList.builder().documentResponseList(documentResponseList).build();
    }

    @Override
    public MessageModel deleteDocument(String site, String document, String version,String userId) throws Exception {

        if (documentRepository.existsByActiveAndSiteAndDocumentAndVersion(1, site, document, version)) {
            Document existingDocument = documentRepository.findByActiveAndSiteAndDocumentAndVersion(1, site, document, version);
            if (existingDocument.isInUse()) {
               throw new DocumentException(903,document,version);
            }
            existingDocument.setActive(0);
            documentRepository.save(existingDocument);

            return MessageModel.builder().message_details(new MessageDetails(document+" with version"+version+" deleted SuccessFully","S")).build();

        } else {
          throw new DocumentException(902,document,version);
        }
    }

    @Override
    public List<DocumentResponse> getAllDocuments(String site) throws Exception {
        return documentRepository.findByActiveAndSite(1, site);
    }

    @Override
    public String callExtension(Extension extension) throws Exception {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new DocumentException(800);
        }
        return extensionResponse;
    }

}
