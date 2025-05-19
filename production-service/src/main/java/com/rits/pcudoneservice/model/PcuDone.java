package com.rits.pcudoneservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "R_PCUDONE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuDone {
    private String site;
    private String handle;
    @Id
    private String pcuBO;
    private LocalDateTime dateTime;
    private String itemBO;
    private String routerBO;
    private String userBO;
    private String qtyDone;
    private String shopOrderBO;
    private int active;
    private String workCenter;
}
