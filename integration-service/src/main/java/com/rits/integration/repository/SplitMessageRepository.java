package com.rits.integration.repository;

import com.rits.integration.model.SplitMessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitMessageRepository extends MongoRepository<SplitMessageEntity, String> {

    // Find all unprocessed split messages for a specific parentIdentifier
    List<SplitMessageEntity> findByParentIdentifierAndProcessed(String parentIdentifier, boolean processed);

    // Check if the previous message in sequence has been processed
    boolean existsByParentIdentifierAndSequenceAndProcessed(String parentIdentifier, String sequence, boolean processed);

     // Query to check if the previous message in the sequence is processed
    @Query("{ 'parentIdentifier': ?0, 'sequence': { $lt: ?1 }, 'processed': false }")
    List<SplitMessageEntity> findUnprocessedBeforeSequence(String parentIdentifier, String sequence);

    // Custom query to check if the previous message has been processed
    default boolean isPreviousMessageProcessed(String parentIdentifier, String sequence) {
        // If no unprocessed messages exist before the current sequence, we can assume the previous ones are processed
        List<SplitMessageEntity> unprocessedMessages = findUnprocessedBeforeSequence(parentIdentifier, sequence);
        return unprocessedMessages.isEmpty();
    }

    // Find by splitIdentifier to get the specific SplitMessageEntity
    Optional<SplitMessageEntity> findBySplitIdentifier(String splitIdentifier);
    Optional<SplitMessageEntity> findById(String id);

    // Find by parentIdentifier and processed flag, sorted by sequence
    List<SplitMessageEntity> findByParentIdentifierAndProcessedOrderBySequenceAsc(String parentIdentifier, boolean processed);

}
