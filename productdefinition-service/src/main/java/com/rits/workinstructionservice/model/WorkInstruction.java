package com.rits.workinstructionservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Document(collection = "R_WORK_INSTRUCTION")
public class WorkInstruction {
    private  String site;
    private  String workInstruction;
    private  String revision;
    @Id
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
    private String file;
    private List<String> tags;
    private String text;
    private  String url;
    private List<Attachment> attachmentList;
    private  List<CustomData> customDataList;
    private  int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;


    public List<String> getFieldNames() {
        List<String> fieldNames = new ArrayList<>();

        Field[] fields = WorkInstruction.class.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }

}
