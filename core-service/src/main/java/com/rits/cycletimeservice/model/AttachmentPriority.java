package com.rits.cycletimeservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_CYCLE_TIME")
public class AttachmentPriority {
    @Id
    private String handle;
    private String tag;
    private int priority;
}
