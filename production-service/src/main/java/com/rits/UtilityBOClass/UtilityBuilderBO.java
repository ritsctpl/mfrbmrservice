package com.rits.UtilityBOClass;

public class UtilityBuilderBO {
    public String retrievePcuBO(String site, String pcuBO) {
        return "PcuBO:" + site + "," + pcuBO;
    }

    public String retrieveItemBO(String site, String item, String itemVersion) {
        return "ItemBO:" + site + "," + item + "," + itemVersion;
    }

    public String retrieveRouterBO(String site, String router, String routerVersion) {
        return "RoutingBO:" + site + "," + router + "," + routerVersion;
    }

    public String retrieveOperationBO(String site, String operation, String operationVersion) {
        return "OperationBO:" + site + "," + operation + "," + operationVersion;
    }

    public String retriveResourceBO(String site, String resource) {
        return "ResourceBO:" + site + "," + resource;
    }

    public String retrieveChildRouterBO(String site, String childRouter) {
        return "ChildRouterBO:" + site + "," + childRouter;
    }

    public String retrieveUserBO(String site, String pcu) {
        return "UserBO:" + site + "," + pcu;
    }

    public String retrieveShopOrderBO(String site, String shopOrder) {
        return "ShopOrderBO:" + site + "," + shopOrder;
    }

    public String retrieveBomBO(String site, String bom, String bomVersion) {
        return "BomBO:" + site + "," + bom + "," + bomVersion;
    }

    public String retrievePcuRouterBO(String site, String pcuRouter, String pcuRouterVersion){
        return "PcuRouterBO: " + site + "," + pcuRouter + "," + pcuRouterVersion;
    }

    public String retrieveRoutingBO(String site, String routing, String routingVersion){
        return "RoutingBO: " + site + "," + routing + "," + routingVersion;
    }

    public String retrieveParentRouteBO(String site, String parentRoute, String parentRouteVersion){
        return "ParentRouteBO: " + site + "," + parentRoute + "," + parentRouteVersion;
    }

    public String retrieveWorkCenterBO(String site, String workCenter) {
        return "WorkCenterBO:" + site + "," + workCenter;
    }

    public String retrievePcuBomBO(String site, String pcuBom, String pcuBomVersion) {
        return "PcuBomBO:" + site + "," + pcuBom + "," + pcuBomVersion;
    }
}