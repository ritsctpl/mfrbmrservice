package com.rits.service;

import com.rits.integration.model.SplitMessageEntity;
import com.rits.integration.repository.SplitMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DependencyManager {

    @Autowired
    private SplitMessageRepository splitMessageRepository;

    /**
     * Fetch all dependent messages based on parentIdentifier and the processed flag.
     *
     * @param parentIdentifier The identifier of the parent message.
     * @return List of unprocessed dependent split messages.
     */
    public List<SplitMessageEntity> getDependentMessages(String parentIdentifier) {
        return splitMessageRepository.findByParentIdentifierAndProcessed(parentIdentifier, false);
    }

    /**
     * Mark a message as processed in the database.
     *
     * @param splitMessageEntity The split message to mark as processed.
     */
    public void markMessageAsProcessed(SplitMessageEntity splitMessageEntity) {
        splitMessageEntity.setProcessed(true);
        splitMessageEntity.setCreatedDateTime(LocalDateTime.now());
        splitMessageRepository.save(splitMessageEntity);
    }

    /**
     * Check if the previous message in the sequence has been processed.
     *
     * @param splitMessageEntity The current split message.
     * @return true if the previous message has been processed, false otherwise.
     */
    public boolean checkPreviousMessageProcessed(SplitMessageEntity splitMessageEntity) {
        return splitMessageRepository.isPreviousMessageProcessed(
                splitMessageEntity.getParentIdentifier(), splitMessageEntity.getSequence());
    }

    /**
     * Fetch the next unprocessed message in the sequence.
     *
     * @param parentIdentifier The identifier of the parent message.
     * @return The next unprocessed message in the sequence.
     */
    public SplitMessageEntity getNextUnprocessedMessage(String parentIdentifier) {
        List<SplitMessageEntity> dependentMessages = getDependentMessages(parentIdentifier);
        if (!dependentMessages.isEmpty()) {
            return dependentMessages.get(0); // Assuming sequential order is handled
        }
        return null;
    }
}
