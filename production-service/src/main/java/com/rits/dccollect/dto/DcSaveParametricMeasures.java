package com.rits.dccollect.dto;

import com.rits.dccollect.model.ParametricMeasures;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_PARAMETRIC_MEASURE")
public class DcSaveParametricMeasures {
    @Id
    private String handle;
    private String site;
    private String dataCollection;
    private String version;
    private List<ParametricMeasures> parametricMeasuresList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDataTime;
}
