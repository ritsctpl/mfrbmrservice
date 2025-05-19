package com.rits.templatebuilderservice.model;

import com.rits.templatebuilderservice.dto.GroupList;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "R_TEMPLATE")
public class Template {
    private String handle;
    private String templateLabel;
    private String templateType;
    private String templateVersion;
    private Boolean currentVersion;
    private List<GroupList> groupIds;
    private String site;
    private int active;
    private String userId;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
}
