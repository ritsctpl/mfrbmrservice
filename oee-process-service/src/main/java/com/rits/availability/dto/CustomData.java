package com.rits.availability.dto;
//
//import lombok.*;
//
//@Builder
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//public class CustomData {
//    private String customData;
//    private String value;
//
//}

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String site;
    private String handle;
    private String customData;
    private String value;
    private String shiftRef;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;
    @PrePersist
    private void generateId() {
        this.id = UUID.randomUUID();  // Generate a random UUID
    }
}
