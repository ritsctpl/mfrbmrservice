package com.rits.worklistservice.dto;

import lombok.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuDoneWithoutBO {
    private String site;
    private String handle;
    private String pcu;
    private LocalDateTime dateTime;
    private String item;
    private String router;
    private String user;
    private String qtyDone;
    private String shopOrder;
    private int active;

    public PcuDoneWithoutBO(PcuDone done) {
        this.site = done.getSite();
        this.handle = done.getHandle();
        this.dateTime = done.getDateTime();
        this.pcu = done.getPcuBO();
        this.item = done.getItemBO();
        this.router = done.getRouterBO();
        this.user = done.getUserBO();
        this.shopOrder = done.getShopOrderBO();
        this.active = done.getActive();
        this.qtyDone = done.getQtyDone();
    }
}
