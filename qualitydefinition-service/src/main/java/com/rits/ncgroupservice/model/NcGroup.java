package com.rits.ncgroupservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_NCGROUP")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NcGroup {
    private String site;
    @Id
    private String handle;
    private String ncGroup;
    private String description;
    private String ncGroupFilterPriority;
    private List<NcCodeDPMOCategory> ncCodeDPMOCategoryList;
    private boolean validAtAllOperations;
    private List<Operation> operationList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;

}
