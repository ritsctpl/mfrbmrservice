package com.rits.licencevalidationservice.dto;

import com.rits.ncgroupservice.model.NcCodeDPMOCategory;
import com.rits.ncgroupservice.model.Operation;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NcGroupRequests {
    private String site;
    private String ncGroup;
    private String description;
    private String ncGroupFilterPriority;
    private List<NcCodeDPMOCategory> ncCodeDPMOCategoryList;
    private boolean validAtAllOperations;
    private List<Operation> operationList;
    private String userId;
}
