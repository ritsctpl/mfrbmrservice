package com.rits.scrapservice.model;

import com.rits.Utility.BOConverter;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ScrapNobo {
    @Id
    private String scrap;
    private String site;
    private String pcu;
    private String status;
    private String operation;
    private String operationVersion;
    private String resource;
    private String shopOrder;
    private String processLot;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private String bom;
    private String bomVersion;
    private double scrapQty;
    private String user;
    private LocalDateTime createdDateTime;
    private int active;
    private String pcuHeaderHandle;
    private String routerHeaderHandle;

    public ScrapNobo(Scrap scrap) {
        this.scrap = scrap.getScrapBO();
        this.site = scrap.getSite();
        this.pcu = scrap.getPcuBO() == null ? null : BOConverter.getPcu(scrap.getPcuBO());
        this.status = scrap.getStatus();
        this.operation = scrap.getOperationBO() == null ? null : BOConverter.getOperation(scrap.getOperationBO());
        this.operationVersion = scrap.getOperationBO() == null ? null : BOConverter.getOperationVersion(scrap.getOperationBO());
        this.resource = scrap.getResourceBO() == null ? null : BOConverter.getResource(scrap.getResourceBO());
        this.shopOrder = scrap.getShopOrderBO() == null ? null : BOConverter.getShopOrder(scrap.getShopOrderBO());
        this.processLot = scrap.getProcessLot();
        this.item = scrap.getItemBO() == null ? null : BOConverter.getItem(scrap.getItemBO());
        this.itemVersion = scrap.getItemBO() == null ? null : BOConverter.getItemVersion(scrap.getItemBO());
        this.routing = scrap.getRoutingBO() == null ? null : BOConverter.getRouting(scrap.getRoutingBO());
        this.routingVersion = scrap.getRoutingBO() == null ? null : BOConverter.getRoutingVersion(scrap.getRoutingBO());
        this.bom = scrap.getBomBO() == null ? null : BOConverter.getBom(scrap.getBomBO());
        this.bomVersion = scrap.getBomBO() == null ? null : BOConverter.getBomVersion(scrap.getBomBO());
        this.scrapQty = scrap.getScrapQty();
        this.user = scrap.getUserBO();
        this.createdDateTime = scrap.getCreatedDateTime();
        this.active = scrap.getActive();
        this.pcuHeaderHandle = scrap.getPcuHeaderHandle();
        this.routerHeaderHandle = scrap.getRouterHeaderHandle();
    }
}
