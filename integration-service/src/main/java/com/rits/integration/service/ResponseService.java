package com.rits.integration.service;

import com.mongodb.client.result.DeleteResult;
import com.rits.integration.dto.FilterByMultiFieldRequest;
import com.rits.integration.exception.IntegrationException;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.repository.ResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResponseService {

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    //    public List<CustomResponseEntity> getAllResponses() {
//        return responseRepository.findAll();
//    }
    public List<CustomResponseEntity> getAllResponses(String site) {
        if(site== null || site.isEmpty()) throw new IntegrationException(7001);
        return responseRepository.findAllBySite(site);
    }

    //    public Optional<CustomResponseEntity> getResponseById(String id) {
//        return responseRepository.findById(id);
//    }
    public CustomResponseEntity getResponseById(String site, String id) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        if(id == null || id.isEmpty()) throw new IntegrationException(7002);
        return responseRepository.findByMessageIdAndSite(id,site);
    }

    //    public CustomResponseEntity createResponse(CustomResponseEntity responseEntity) {
//        responseEntity.setCreatedDateTime(LocalDateTime.now());
//        return responseRepository.save(responseEntity);
//    }
    public CustomResponseEntity createResponse(CustomResponseEntity responseEntity) {
        if(responseEntity.getSite() == null || responseEntity.getSite().isEmpty()) throw new IntegrationException(7001);
        responseEntity.setCreatedDateTime(LocalDateTime.now());
        return responseRepository.save(responseEntity);
    }

    //    public CustomResponseEntity updateResponse(String id, CustomResponseEntity updatedResponse) {
//        if (responseRepository.existsById(id)) {
//            updatedResponse.setIdentifier(id);
//            return responseRepository.save(updatedResponse);
//        }
//        return null;
//    }
    public CustomResponseEntity updateResponse(String messageId, CustomResponseEntity updatedResponse) {
        if(updatedResponse.getSite() == null || updatedResponse.getSite().isEmpty()) throw new IntegrationException(7001);
        if(messageId == null || messageId.isEmpty()) throw new IntegrationException(7002);
        if (responseRepository.existsByMessageIdAndSite(messageId, updatedResponse.getSite())) {
            updatedResponse.setIdentifier(messageId);
            return responseRepository.save(updatedResponse);
        }
        return null;
    }


    //    public void deleteResponse(String id) {
//        responseRepository.deleteById(id);
//    }
    public void deleteResponse(String site, String messageId) {
        if(site == null || site.isEmpty()) throw new IntegrationException(7001);
        if(messageId == null || messageId.isEmpty()) throw new IntegrationException(7002);
        responseRepository.deleteByMessageIdAndSite(messageId,site);
    }

    // New Methods:

    public List<CustomResponseEntity> getTop50Responses(String site) {
        if(site== null || site.isEmpty()) throw new IntegrationException(7001);
        return responseRepository.findTop50BySiteOrderByCreatedDateTimeDesc(site);
    }

    public List<CustomResponseEntity> getResponsesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return responseRepository.findByCreatedDateTimeBetween(startDate, endDate);
    }

    public Optional<CustomResponseEntity> getResponseByIdentifier(String identifier) {
        return responseRepository.findByIdentifier(identifier);
    }

    public List<CustomResponseEntity> getResponsesByStatus(String status) {
        return responseRepository.findByStatus(status);
    }

    public List<CustomResponseEntity> getFilteredResponses(String identifier, String status,
                                                           LocalDateTime startDate, LocalDateTime endDate) {
        List<CustomResponseEntity> responses = new ArrayList<>();

        if (identifier != null && !identifier.isEmpty()) {
            Optional<CustomResponseEntity> optionalResponse = responseRepository.findByIdentifier(identifier);
            optionalResponse.ifPresent(responses::add);
        }
        else if (status != null && !status.isEmpty()) {
            responses = responseRepository.findByStatus(status);
        }
        else if (startDate != null && endDate != null) {
            responses = responseRepository.findByCreatedDateTimeBetween(startDate, endDate);
        }

        else {
            responses = responseRepository.findAll();
        }

        if (responses.isEmpty()) {
            throw new IntegrationException(102);
        }

        return responses;
    }

    public boolean deleteResponseEntities(String site, String status, Integer hours, Integer minutes, Integer seconds) {
        Query latestRecordQuery = new Query(Criteria.where("site").is(site))
                .with(Sort.by(Sort.Direction.DESC, "createdDateTime"))
                .limit(1); // Get the latest record

        CustomResponseEntity latestRecord = mongoTemplate.findOne(latestRecordQuery, CustomResponseEntity.class);

        if (latestRecord == null || latestRecord.getCreatedDateTime() == null) {
            return false; // No records found, nothing to delete
        }

        LocalDateTime cutoffDate = latestRecord.getCreatedDateTime();
        if (hours != null) {
            cutoffDate = cutoffDate.minusHours(hours);
        }
        if (minutes != null) {
            cutoffDate = cutoffDate.minusMinutes(minutes);
        }
        if (seconds != null) {
            cutoffDate = cutoffDate.minusSeconds(seconds);
        }

        Criteria criteria = Criteria.where("site").is(site)  // Added site filter for deletion
                .and("status").is(status)
                .and("createdDateTime").lte(cutoffDate);

        Query deleteQuery = new Query(criteria);

        DeleteResult result = mongoTemplate.remove(deleteQuery, CustomResponseEntity.class);

        return result.getDeletedCount() > 0; // Returns true if any records were deleted
    }

    public List<CustomResponseEntity> getDataByCombination(FilterByMultiFieldRequest request) {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new IntegrationException(7001);
        }

        List<CustomResponseEntity> responses = responseRepository.findAllBySite(request.getSite());

        // Convert empty fields to null
        String identifier = (request.getIdentifier() != null && !request.getIdentifier().trim().isEmpty()) ? request.getIdentifier() : null;
        String status = (request.getStatus() != null && !request.getStatus().trim().isEmpty()) ? request.getStatus() : null;

        return responses.stream()
                .filter(response -> identifier == null ||
                        (response.getIdentifier() != null &&
                                response.getIdentifier().toLowerCase().contains(identifier.toLowerCase())))
                .filter(response -> status == null || response.getStatus().equals(status))
                .filter(response -> {
                    if (request.getStartDate() != null && request.getEndDate() != null) {
                        if (response.getCreatedDateTime() == null) {
                            return false; // Exclude records with null createdDateTime
                        }
                        return !response.getCreatedDateTime().isBefore(request.getStartDate())
                                && !response.getCreatedDateTime().isAfter(request.getEndDate());
                    }
                    return true; // If no date filter, keep the record
                })
                .sorted(Comparator.comparing(CustomResponseEntity::getCreatedDateTime, Comparator.nullsLast(Comparator.reverseOrder()))) // Sorting in descending order
                .collect(Collectors.toList());
    }
}


/*
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.repository.ResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResponseService {

    @Autowired
    private ResponseRepository responseRepository;

    public CustomResponseEntity saveResponse(CustomResponseEntity customResponseEntity) {
        return responseRepository.save(customResponseEntity);
    }

    public Optional<CustomResponseEntity> getResponseByIdentifier(String identifier) {
        return responseRepository.findByIdentifier(identifier);
    }



    public List<CustomResponseEntity> getAllResponses() {
        return responseRepository.findAll();
    }

    public Optional<CustomResponseEntity> getResponseById(String id) {
        return responseRepository.findById(id);
    }

    public CustomResponseEntity createResponse(CustomResponseEntity responseEntity) {
        return responseRepository.save(responseEntity);
    }

    public CustomResponseEntity updateResponse(String id, CustomResponseEntity updatedResponse) {
        Optional<CustomResponseEntity> existingResponse = responseRepository.findById(id);
        if (existingResponse.isPresent()) {
            CustomResponseEntity responseEntity = existingResponse.get();
            responseEntity.setPreprocessJoltResponse(updatedResponse.getPreprocessJoltResponse());
            responseEntity.setPreprocessApiResponse(updatedResponse.getPreprocessApiResponse());
            responseEntity.setApiToProcessResponse(updatedResponse.getApiToProcessResponse());
            responseEntity.setPostProcessJoltResponse(updatedResponse.getPostProcessJoltResponse());
            responseEntity.setPostProcessApiResponse(updatedResponse.getPostProcessApiResponse());
            responseEntity.setPassHandlerResponse(updatedResponse.getPassHandlerResponse());
            responseEntity.setFailHandlerResponse(updatedResponse.getFailHandlerResponse());
            return responseRepository.save(responseEntity);
        }
        return null;  // Handle cases where the response doesn't exist
    }

    public void deleteResponse(String id) {
        responseRepository.deleteById(id);
    }

    // Any additional CRUD operations can be added here as needed.
}
*/