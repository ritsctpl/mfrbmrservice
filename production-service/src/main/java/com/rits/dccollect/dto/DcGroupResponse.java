package com.rits.dccollect.dto;

import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DcGroupResponse {
    private String revision;
    private boolean currentRevision;
    private String dcGroupRef;
    private String site;
    private String dcGroup;
    private String description;
    private String collectDataAt;
    private String collectType;
    private boolean passFailGroup;
    private boolean authenticationRequired;
    private String statusRef;
    private String status;
    private boolean erp;
    private String collectMethod;
    private boolean erpInspection;
    private boolean erpWholeGroupInspection;
    private boolean meEvalInspection;
    private String passFailNumber;
    private String customButtonId;
    private List<Parameter> dcParameterList;
    private List<ShortRunList> shortRunList;
    private List<UdfList> udfList;
    private List<UserOptionList> userOptionList;
    private List<DcPromptTranslationList> dcPromptTranslationList;
    private String qmCharType;
    private boolean qmCritical;
    private boolean autoLogNC;
    private String ncCodeRef;
    private String dataFieldRef;
    private boolean overrideRequired;
    private boolean overrideDone;
    private String overrideStatus;
    private String overrideCertificationRef;
}
