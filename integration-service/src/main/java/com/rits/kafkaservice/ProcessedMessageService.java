package com.rits.kafkaservice;

import com.mongodb.client.result.DeleteResult;
import com.rits.kafkapojo.ProcessedMessage;
import com.rits.repository.ProcessedMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcessedMessageService {

    private final ProcessedMessageRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public ProcessedMessageService(ProcessedMessageRepository repository) {
        this.repository = repository;
    }

    public List<ProcessedMessage> getAllProcessedMessages(String topicName, String status, LocalDateTime startDate, LocalDateTime endDate) {
        Criteria criteria = new Criteria();

        if (topicName != null && !topicName.isEmpty()) {
            criteria = criteria.and("topicName").regex(".*" + topicName + ".*", "i");
        }
        if (status != null && !status.isEmpty()) {
            criteria = criteria.and("status").is(status);
        }
        if (startDate != null && endDate != null) {
            criteria = criteria.and("processedAt").gte(startDate).lte(endDate);
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "processedAt"));

        // If no filters are applied, fetch the last 50 records
        if (topicName == null && status == null && startDate == null && endDate == null) {
            query.limit(50);
        }

        return mongoTemplate.find(query, ProcessedMessage.class);
    }

    public List<ProcessedMessage> getAllProcessedMessages() {
        return repository.findAll();
    }

    public boolean isMessageProcessed(String messageId) {
        return repository.existsByMessageId(messageId);
    }

    public ProcessedMessage createProcessedMessage(ProcessedMessage message) {
        return repository.save(message);
    }

    public void deleteProcessedMessage(String messageId) {
        repository.deleteById(messageId);
    }

    public List<ProcessedMessage> getTop50ProcessedMessages() {
        return repository.findTop50ByOrderByProcessedAtDesc();
    }

    public boolean deleteProcessedMessages(String status, Integer hours, Integer minutes, Integer seconds) {
        Query latestRecordQuery = new Query()
                .with(Sort.by(Sort.Direction.DESC, "processedAt"))
                .limit(1); // Get the latest record

        ProcessedMessage latestRecord = mongoTemplate.findOne(latestRecordQuery, ProcessedMessage.class);

        if (latestRecord == null || latestRecord.getProcessedAt() == null) {
            return false; // No records found, nothing to delete
        }

        LocalDateTime cutoffDate = latestRecord.getProcessedAt();
        if (hours != null) {
            cutoffDate = cutoffDate.minusHours(hours);
        }
        if (minutes != null) {
            cutoffDate = cutoffDate.minusMinutes(minutes);
        }
        if (seconds != null) {
            cutoffDate = cutoffDate.minusSeconds(seconds);
        }

        Criteria criteria = Criteria.where("status").is(status)
                .and("processedAt").lte(cutoffDate);
        Query deleteQuery = new Query(criteria);

        DeleteResult result = mongoTemplate.remove(deleteQuery, ProcessedMessage.class);

        return result.getDeletedCount() > 0; // Returns true if any records were deleted
    }

}
