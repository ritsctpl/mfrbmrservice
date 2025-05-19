package com.rits.kafkapojo;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "dedup_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DedupMessage {

    @Id
    private String messageId;

    @Indexed(expireAfterSeconds = 0) // TTL will be configured dynamically
    private LocalDateTime receivedAt = LocalDateTime.now();

    private String status = "processed";

    public DedupMessage(String messageId) {
        this.messageId = messageId;
        this.receivedAt = LocalDateTime.now();
    }
}
