package com.rits.site.model;

import com.rits.site.dto.Theme;
import com.rits.site.dto.TimeZone;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "r_site")
public class Site {
    private String site;
    @Id
    private String handle;
    private String description;
    private String type;
    private String timeZone;
    private List<ActivityHookList> activityHookLists;
    private Theme theme;
    private boolean local;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
    private String userId;
}
