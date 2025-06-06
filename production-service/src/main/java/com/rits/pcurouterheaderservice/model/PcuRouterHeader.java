package com.rits.pcurouterheaderservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_PCUROUTERHEADER")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuRouterHeader {
    private String site;
    @Id
    private String handle;
    private String pcuBo;
    private String pcuRouterBo;
    private List<Router> router;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
