package com.rits.pcudoneservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PcuDoneNoBO {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String user;
    private String qtyDone;
    private String shopOrder;
    private int active;
    private String workCenter;
}
