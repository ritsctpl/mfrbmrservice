package com.rits.workinstructionservice.dto;

import com.rits.workinstructionservice.model.Attachment;
import com.rits.workinstructionservice.model.CustomData;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class WorkInstructionRequest {
    private  String site;
    private  String workInstruction;
    private  String revision;
    private  String handle;
    private  String description;
    private  String status;
    private  boolean required;
    private  boolean currentVersion;
    private  boolean alwaysShowInNewWindow;
    private  boolean logViewing;
    private  boolean changeAlert;
    private  boolean erpWi;
    private  String instructionType;
    private  String erpFilename;
    private String fileName;
    private  String url;
    private List<String> tags;
    private String file;
    private String text;
    private List<Attachment> attachmentList;
    private  List<CustomData> customDataList;
    private  int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;
    private String bom;
    private String bomVersion;
    private String component;
    private String componentVersion;
    private String pcuBO;
    private String shopOrderBO;
    private String operationBO;
    private String resourceBO;
}
