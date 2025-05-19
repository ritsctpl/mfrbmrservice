package com.rits.scrapservice.model;

import com.rits.Utility.BOConverter;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RetrieveResponseNoBO {
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String operation;
    private String operationVersion;
    private String resource;
    private String processLot;
    private String shopOrder;
    private String bom;
    private String bomVersion;
    private String status;

//    public RetrieveResponseNoBO(RetrieveResponse response) {
//        this.pcu = BOConverter.getPcu(response.getPcuBO());
//        this.item = BOConverter.getItem(response.getItemBO());
//        this.itemVersion = BOConverter.getItemVersion(response.getItemBO());
//        this.router = BOConverter.getRouter(response.getRouterBO());
//        this.routerVersion = BOConverter.getRouterVersion(response.getRouterBO());
//        this.operation = BOConverter.getOperation(response.getOperationBO());
//        this.operationVersion = BOConverter.getOperationVersion(response.getOperationBO());
//        this.resource = BOConverter.getResource(response.getResourceBO());
//        this.processLot = response.getProcessLot();
//        this.shopOrder = BOConverter.getShopOrder(response.getShopOrderBO());
//        this.bom = BOConverter.getBom(response.getBomBO());
//        this.bomVersion = BOConverter.getBomVersion(response.getBomBO());
//        this.status = response.getStatus();
//    }
}
