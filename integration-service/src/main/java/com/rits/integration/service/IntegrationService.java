package com.rits.integration.service;

import com.rits.event.IntegrationEntityChangeEvent; // Import the event
import com.rits.integration.exception.IntegrationException;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.model.IntegrationMessageModel;
import com.rits.integration.model.MessageDetails;
import com.rits.integration.repository.IntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegrationService {


    private final IntegrationRepository integrationRepository;
    private final MessageSource localMessageSource;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher; // Add ApplicationEventPublisher
    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    /**
     * Checks if an identifier exists. If it exists, updates the existing record; if not, creates a new one.
     *
     * @param entity the incoming entity to create or update
     * @return the updated or newly created entity
     */
//    public IntegrationMessageModel createOrUpdateIntegration(IntegrationEntity entity) {
//        // Check if the identifier already exists
//        IntegrationEntity existingEntity = integrationRepository.findByIdentifier(entity.getIdentifier());
//
//        IntegrationEntity savedEntity;
//        if (existingEntity != null) {
//            // Update existing entity
//            existingEntity.setType(entity.getType());
//            existingEntity.setMessageId(entity.getMessageId());
//            existingEntity.setPreprocessJolt(entity.getPreprocessJolt());
//            existingEntity.setPreprocessApi(entity.getPreprocessApi());
//            existingEntity.setApiToProcess(entity.getApiToProcess());
//            existingEntity.setPostProcessJolt(entity.getPostProcessJolt());
//            existingEntity.setPostProcessApi(entity.getPostProcessApi());
//            existingEntity.setPassHandler(entity.getPassHandler());
//            existingEntity.setFailHandler(entity.getFailHandler());
//            existingEntity.setProcessSplitXslt(entity.getProcessSplitXslt());  //processSplitXslt
//            existingEntity.setPreprocessXslt(entity.getPreprocessXslt()); //private String preprocessXslt;
//            existingEntity.setTransformationType(entity.getTransformationType());
//            existingEntity.setExpectedSplits(entity.getExpectedSplits());
//            savedEntity = integrationRepository.save(existingEntity);
//
//            // Set the operation type to "UPDATE"
//            savedEntity.setOperationType("UPDATE");
//            String updateMessage = getFormattedMessage(2,entity.getIdentifier());
//            return IntegrationMessageModel.builder()
//                    .message_details(new MessageDetails(updateMessage,"S"))
//                    .integrationEntityResponse(savedEntity)
//                    .build();
//
//
//        } else {
//            // If the identifier does not exist, create a new entity
//            entity.setCreatedDateTime(LocalDateTime.now());
//            savedEntity = integrationRepository.save(entity);
//
//            // Set the operation type to "CREATE"
//            savedEntity.setOperationType("CREATE");
//        }
//
//        IntegrationEntityChangeEvent changeEvent = new IntegrationEntityChangeEvent(this, savedEntity);
//        applicationEventPublisher.publishEvent(changeEvent);
//
//        String createdMessage = getFormattedMessage(1,entity.getIdentifier());
//
//        return IntegrationMessageModel.builder()
//                .message_details(new MessageDetails(createdMessage, "S"))
//                .integrationEntityResponse(savedEntity)
//                .build();
//    }

    public IntegrationMessageModel createOrUpdateIntegration(IntegrationEntity entity) {
        if(entity.getSite() == null || entity.getSite().isEmpty()) throw new IntegrationException(7001);
        // Check if the identifier already exists
        IntegrationEntity existingEntity = integrationRepository.findByIdentifierAndSite(entity.getIdentifier(),entity.getSite());

        IntegrationEntity savedEntity;
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.setType(entity.getType());
            existingEntity.setMessageId(entity.getMessageId());
            existingEntity.setPreprocessJolt(entity.getPreprocessJolt());
            existingEntity.setPreprocessApi(entity.getPreprocessApi());
            existingEntity.setApiToProcess(entity.getApiToProcess());
            existingEntity.setPostProcessJolt(entity.getPostProcessJolt());
            existingEntity.setPostProcessApi(entity.getPostProcessApi());
            existingEntity.setPassHandler(entity.getPassHandler());
            existingEntity.setFailHandler(entity.getFailHandler());
            existingEntity.setProcessSplitXslt(entity.getProcessSplitXslt());  //processSplitXslt
            existingEntity.setPreprocessXslt(entity.getPreprocessXslt()); //private String preprocessXslt;
            existingEntity.setTransformationType(entity.getTransformationType());
            existingEntity.setExpectedSplits(entity.getExpectedSplits());
            savedEntity = integrationRepository.save(existingEntity);

            // Set the operation type to "UPDATE"
            savedEntity.setOperationType("UPDATE");
            String updateMessage = getFormattedMessage(2,entity.getIdentifier());
            return IntegrationMessageModel.builder()
                    .message_details(new MessageDetails(updateMessage,"S"))
                    .integrationEntityResponse(savedEntity)
                    .build();


        } else {
            // If the identifier does not exist, create a new entity
            entity.setCreatedDateTime(LocalDateTime.now());
            savedEntity = integrationRepository.save(entity);

            // Set the operation type to "CREATE"
            savedEntity.setOperationType("CREATE");
        }

        IntegrationEntityChangeEvent changeEvent = new IntegrationEntityChangeEvent(this, savedEntity);
        applicationEventPublisher.publishEvent(changeEvent);

        String createdMessage = getFormattedMessage(1,entity.getIdentifier());

        return IntegrationMessageModel.builder()
                .message_details(new MessageDetails(createdMessage, "S"))
                .integrationEntityResponse(savedEntity)
                .build();
    }

//    public List<IntegrationEntity> getAllIntegrations() {
//        return integrationRepository.findAllByOrderByCreatedDateTimeDesc();
//    }

    public List<IntegrationEntity> getAllIntegrations(String site) {
        return integrationRepository.findAllBySiteOrderByCreatedDateTimeDesc(site);
    }

//    public Optional<IntegrationEntity> getIntegrationById(String id) {
//        return integrationRepository.findById(id);
//    }

    public IntegrationEntity getIntegrationById(String site, String id) {
        if (site == null || site.isEmpty()) {
            throw new IntegrationException(7001);
        }
        if (id == null || id.isEmpty()) {
            throw new IntegrationException(7002);
        }
        return integrationRepository.findByIdAndSite(id, site);
    }

//    public IntegrationEntity getIntegrationByIdentifier(String identifier) {
//        // Find the entity by its identifier
//        return integrationRepository.findByIdentifier(identifier);
//
//    }

    public IntegrationEntity getIntegrationByIdentifier(String site, String identifier) {
        // Find the entity by its identifier
        return integrationRepository.findByIdentifierAndSite(identifier, site);

    }
//    public IntegrationEntity updateIntegration(String id,IntegrationEntity entity) {
//        Optional<IntegrationEntity> existingEntity = integrationRepository.findById(id);
//        if (existingEntity.isPresent()) {
//            IntegrationEntity updatedEntity = existingEntity.get();
//            updatedEntity.setIdentifier(entity.getIdentifier());
//            updatedEntity.setType(entity.getType());
//            updatedEntity.setMessageId(entity.getMessageId());
//            updatedEntity.setPreprocessJolt(entity.getPreprocessJolt());
//            updatedEntity.setPreprocessApi(entity.getPreprocessApi());
//            updatedEntity.setApiToProcess(entity.getApiToProcess());
//            updatedEntity.setPostProcessJolt(entity.getPostProcessJolt());
//            updatedEntity.setPostProcessApi(entity.getPostProcessApi());
//            updatedEntity.setPassHandler(entity.getPassHandler());
//            updatedEntity.setFailHandler(entity.getFailHandler());
//            updatedEntity.setProcessSplitXslt(entity.getProcessSplitXslt());  //processSplitXslt
//            updatedEntity.setPreprocessXslt(entity.getPreprocessXslt()); //private String preprocessXslt;
//            updatedEntity.setTransformationType(entity.getTransformationType());
//            updatedEntity.setCreatedDateTime(existingEntity.get().getCreatedDateTime());
//            IntegrationEntity savedEntity = integrationRepository.save(updatedEntity);
//
//            // Publish an event to notify that an IntegrationEntity has been updated
//            IntegrationEntityChangeEvent changeEvent = new IntegrationEntityChangeEvent(this, savedEntity);
//            applicationEventPublisher.publishEvent(changeEvent);
//
//            return savedEntity;
//        }
//        return null;
//    }

    public IntegrationEntity updateIntegration(String id, IntegrationEntity entity) {
        if(entity.getSite() == null || entity.getSite().isEmpty()) throw new IntegrationException(7001);
        if(id == null || id.isEmpty()) throw new IntegrationException(7002);
        IntegrationEntity updatedEntity = integrationRepository.findByIdAndSite(id, entity.getSite());
        if (updatedEntity == null) throw new IntegrationException(7003);
        if (updatedEntity!=null) {
            updatedEntity.setIdentifier(entity.getIdentifier());
            updatedEntity.setType(entity.getType());
            updatedEntity.setMessageId(entity.getMessageId());
            updatedEntity.setPreprocessJolt(entity.getPreprocessJolt());
            updatedEntity.setPreprocessApi(entity.getPreprocessApi());
            updatedEntity.setApiToProcess(entity.getApiToProcess());
            updatedEntity.setPostProcessJolt(entity.getPostProcessJolt());
            updatedEntity.setPostProcessApi(entity.getPostProcessApi());
            updatedEntity.setPassHandler(entity.getPassHandler());
            updatedEntity.setFailHandler(entity.getFailHandler());
            updatedEntity.setProcessSplitXslt(entity.getProcessSplitXslt());  //processSplitXslt
            updatedEntity.setPreprocessXslt(entity.getPreprocessXslt()); //private String preprocessXslt;
            updatedEntity.setTransformationType(entity.getTransformationType());
            IntegrationEntity savedEntity = integrationRepository.save(updatedEntity);

            // Publish an event to notify that an IntegrationEntity has been updated
            IntegrationEntityChangeEvent changeEvent = new IntegrationEntityChangeEvent(this, savedEntity);
            applicationEventPublisher.publishEvent(changeEvent);

            return savedEntity;
        }
        return null;
    }

//    public IntegrationMessageModel deleteIntegration(String id) {
//        Optional<IntegrationEntity> existingEntity = integrationRepository.findById(id);
//
//        if (existingEntity.isPresent()) {
//            IntegrationEntity deletedEntity = existingEntity.get();
//
//            integrationRepository.deleteById(id);
//            deletedEntity.setOperationType("DELETE");
//
//            IntegrationEntityChangeEvent changeEvent = new IntegrationEntityChangeEvent(this, deletedEntity);
//            applicationEventPublisher.publishEvent(changeEvent);
//
//            String deleteMessage = getFormattedMessage(3, deletedEntity.getId());
//
//            return IntegrationMessageModel.builder()
//                    .message_details(new MessageDetails(deleteMessage, "S"))
//                    .integrationEntityResponse(deletedEntity)
//                    .build();
//        } else {
//            throw new IntegrationException(101,id);
//        }
//    }

    public IntegrationMessageModel deleteIntegration(String site, String id) {
        IntegrationEntity deletedEntity = integrationRepository.findByIdAndSite(id, site);

        if (deletedEntity!=null) {

            integrationRepository.deleteByIdAndSite(id, site);
            deletedEntity.setOperationType("DELETE");

            IntegrationEntityChangeEvent changeEvent = new IntegrationEntityChangeEvent(this, deletedEntity);
            applicationEventPublisher.publishEvent(changeEvent);

            String deleteMessage = getFormattedMessage(3, deletedEntity.getIdentifier());

            return IntegrationMessageModel.builder()
                    .message_details(new MessageDetails(deleteMessage, "S"))
                    .integrationEntityResponse(deletedEntity)
                    .build();
        } else {
            throw new IntegrationException(101,id);
        }
    }


//    public List<Map<String, Object>> getAllIntegrationSummaries() {
//        return integrationRepository.findAllByOrderByCreatedDateTimeDesc().stream()
//                .map(entity -> {
//                    Map<String, Object> summaryMap = new HashMap<>();
//                    summaryMap.put("identifier", entity.getIdentifier());
//                    summaryMap.put("messageId", entity.getMessageId());
//                    summaryMap.put("type", entity.getType());
//                    return summaryMap;
//                })
//                .collect(Collectors.toList());
//    }

//    public List<Map<String, Object>> getAllIntegrationSummaries(IntegrationEntity integrationEntity) {
//        String identifier = integrationEntity.getIdentifier();
////        String site = integrationEntity.getSite();
//        if (identifier != null) {
//            List<Map<String, Object>> identifierResults = integrationRepository
//                    .findAllByIdentifierContainingIgnoreCase(identifier).stream()
//                    .map(entity -> {
//                        Map<String, Object> summaryMap = new HashMap<>();
//                        summaryMap.put("identifier", entity.getIdentifier());
//                        summaryMap.put("messageId", entity.getMessageId());
//                        summaryMap.put("type", entity.getType());
//                        return summaryMap;
//                    })
//                    .collect(Collectors.toList());
//
//
//            if (identifierResults.isEmpty()) {
//                return Collections.emptyList();
//            }
//            return identifierResults;
//        }
//
//        return integrationRepository.findTop50ByOrderByCreatedDateTimeDesc()
//                .stream()
//                .map(entity -> {
//                    Map<String, Object> summaryMap = new HashMap<>();
//                    summaryMap.put("identifier", entity.getIdentifier());
//                    summaryMap.put("messageId", entity.getMessageId());
//                    summaryMap.put("type", entity.getType());
//                    return summaryMap;
//                })
//                .collect(Collectors.toList());
//    }

    public List<Map<String, Object>> getAllIntegrationSummaries(IntegrationEntity integrationEntity) {
        if(integrationEntity.getSite() == null || integrationEntity.getSite().isEmpty()) throw new IntegrationException(7001);
        String identifier = integrationEntity.getIdentifier();
        String site = integrationEntity.getSite();
        if (identifier != null && site != null) {
            List<Map<String, Object>> identifierResults = integrationRepository
                    .findAllByIdentifierContainingIgnoreCaseAndSite(identifier,site).stream()
                    .map(entity -> {
                        Map<String, Object> summaryMap = new HashMap<>();
                        summaryMap.put("identifier", entity.getIdentifier());
                        summaryMap.put("messageId", entity.getMessageId());
                        summaryMap.put("type", entity.getType());
                        return summaryMap;
                    })
                    .collect(Collectors.toList());


            if (identifierResults.isEmpty()) {
                return Collections.emptyList();
            }
            return identifierResults;
        }

        return integrationRepository.findTop50BySiteOrderByCreatedDateTimeDesc(site)
                .stream()
                .map(entity -> {
                    Map<String, Object> summaryMap = new HashMap<>();
                    summaryMap.put("identifier", entity.getIdentifier());
                    summaryMap.put("messageId", entity.getMessageId());
                    summaryMap.put("type", entity.getType());
                    return summaryMap;
                })
                .collect(Collectors.toList());
    }

    /*public boolean isValidOrderIdentifier(String identifier) {
        IntegrationEntity entity = integrationRepository.findByIdentifier(identifier);
        return entity != null && "split".equalsIgnoreCase(entity.getType());
    }*/
}

/*
package com.rits.integration.service;

import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.IntegrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IntegrationService {

    @Autowired
    private IntegrationRepository integrationRepository;

    */
/**
     * Checks if an identifier exists. If it exists, updates the existing record; if not, creates a new one.
     *
     * @param entity the incoming entity to create or update
     * @return the updated or newly created entity
     *//*

    public IntegrationEntity createOrUpdateIntegration(IntegrationEntity entity) {
        // Check if the identifier already exists
        IntegrationEntity existingEntity = integrationRepository.findByIdentifier(entity.getIdentifier());

        if (existingEntity != null) {
            // Update existing entity
            existingEntity.setType(entity.getType());
            existingEntity.setMessageId(entity.getMessageId());
            existingEntity.setPreprocessJolt(entity.getPreprocessJolt());
            existingEntity.setPreprocessApi(entity.getPreprocessApi());
            existingEntity.setApiToProcess(entity.getApiToProcess());
            existingEntity.setPostProcessJolt(entity.getPostProcessJolt());
            existingEntity.setPostProcessApi(entity.getPostProcessApi());
            existingEntity.setPassHandler(entity.getPassHandler());
            existingEntity.setFailHandler(entity.getFailHandler());
            return integrationRepository.save(existingEntity);
        } else {
            // If the identifier does not exist, create a new entity
            return integrationRepository.save(entity);
        }
    }

    public List<IntegrationEntity> getAllIntegrations() {
        return integrationRepository.findAll();
    }

    public Optional<IntegrationEntity> getIntegrationById(String id) {
        return integrationRepository.findById(id);
    }

    public IntegrationEntity getIntegrationByIdentifier(String identifier) {
        return integrationRepository.findByIdentifier(identifier);
    }

    public IntegrationEntity updateIntegration(String id, IntegrationEntity entity) {
        Optional<IntegrationEntity> existingEntity = integrationRepository.findById(id);
        if (existingEntity.isPresent()) {
            IntegrationEntity updatedEntity = existingEntity.get();
            updatedEntity.setIdentifier(entity.getIdentifier());
            updatedEntity.setType(entity.getType());
            updatedEntity.setMessageId(entity.getMessageId());
            updatedEntity.setPreprocessJolt(entity.getPreprocessJolt());
            updatedEntity.setPreprocessApi(entity.getPreprocessApi());
            updatedEntity.setApiToProcess(entity.getApiToProcess());
            updatedEntity.setPostProcessJolt(entity.getPostProcessJolt());
            updatedEntity.setPostProcessApi(entity.getPostProcessApi());
            updatedEntity.setPassHandler(entity.getPassHandler());
            updatedEntity.setFailHandler(entity.getFailHandler());
            return integrationRepository.save(updatedEntity);
        }
        return null;
    }

    public void deleteIntegration(String id) {
        integrationRepository.deleteById(id);
    }
}


*/
/*
package com.rits.integration.service;

import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.IntegrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IntegrationService {

    @Autowired
    private IntegrationRepository integrationRepository;

    public IntegrationEntity createIntegration(IntegrationEntity entity) {
        return integrationRepository.save(entity);
    }

    public List<IntegrationEntity> getAllIntegrations() {
        return integrationRepository.findAll();
    }

    public Optional<IntegrationEntity> getIntegrationById(String id) {
        return integrationRepository.findById(id);
    }

    public IntegrationEntity getIntegrationByIdentifier(String identifier) {
        return integrationRepository.findByIdentifier(identifier);
    }

    public IntegrationEntity updateIntegration(String id, IntegrationEntity entity) {
        Optional<IntegrationEntity> existingEntity = integrationRepository.findById(id);
        if (existingEntity.isPresent()) {
            IntegrationEntity updatedEntity = existingEntity.get();
            updatedEntity.setIdentifier(entity.getIdentifier());
            updatedEntity.setType(entity.getType());
            updatedEntity.setMessageId(entity.getMessageId());
            updatedEntity.setPreprocessJolt(entity.getPreprocessJolt());
            updatedEntity.setPreprocessApi(entity.getPreprocessApi());
            updatedEntity.setApiToProcess(entity.getApiToProcess());
            updatedEntity.setPostProcessJolt(entity.getPostProcessJolt());
            updatedEntity.setPostProcessApi(entity.getPostProcessApi());
            updatedEntity.setPassHandler(entity.getPassHandler());
            updatedEntity.setFailHandler(entity.getFailHandler());
            return integrationRepository.save(updatedEntity);
        }
        return null;
    }

    public void deleteIntegration(String id) {
        integrationRepository.deleteById(id);
    }
}
*/

