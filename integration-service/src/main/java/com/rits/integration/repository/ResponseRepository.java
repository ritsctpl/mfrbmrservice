package com.rits.integration.repository;



import com.rits.integration.model.CustomResponseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseRepository extends MongoRepository<CustomResponseEntity, String> {
    // You can define additional query methods if needed, for example:


    // Get top 50 by createdDateTime descending
    List<CustomResponseEntity> findTop50ByOrderByCreatedDateTimeDesc();

    // Find responses by createdDateTime range
    List<CustomResponseEntity> findByCreatedDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find responses by identifier
    Optional<CustomResponseEntity> findByIdentifier(String identifier);

    // Find responses by status
    List<CustomResponseEntity> findByStatus(String status);

    List<CustomResponseEntity> findAllBySite(String site);
    CustomResponseEntity findByMessageIdAndSite(String messageId, String site);
    List<CustomResponseEntity> findTop50BySiteOrderByCreatedDateTimeDesc(String site);
    long deleteByMessageIdAndSite(String messageId, String site);
    boolean existsByMessageIdAndSite(String messageId, String site);
}