package com.rits.nonconformanceservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NcRequest {
    private String site;
    private String pcuBO;
    private String ncCodeBo;
    private String ncDataTypeBo;
    private String userBo;
    private String dateTime;
    private double qty;
    private double defectCount;
    private String componentBo;
    private String compContextGbo;
    private String refDes;
    private String comments;
    private String resourceBo;
    private String operationBO;
    private String stepID;
    private String routerBo;
    private String workCenterBo;
    private String itemBo;
    private String parentNcCodeBo;
    private String childParentNcCodeBo;
    private List<DataField> dataFieldsList;
    private int count;
}
