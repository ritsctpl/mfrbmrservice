package com.rits.signoffservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "SIGNOFF_SERVICE")
public class SignOff {
    private String pcuBO;
    private String site;
    private String operation;
    private String resource;
    private String workCenter;
    private String quantity;

}
