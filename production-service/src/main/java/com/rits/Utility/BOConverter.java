package com.rits.Utility;

public class BOConverter {
    public static String retrievePcuBO(String site, String pcuBO) {
        return "PcuBO:" + site + "," + pcuBO;
    }

    public static String retrieveItemBO(String site, String item, String itemVersion) {
        return "ItemBO:" + site + "," + item + "," + itemVersion;
    }

    public static String retrieveRouterBO(String site, String router, String routerVersion) {
        return "RoutingBO:" + site + "," + router + "," + routerVersion;
    }

    public static String retrieveRoutingBO(String site, String routing, String routingVersion) {
        return "RoutingBO:" + site + "," + routing + "," + routingVersion ;
    }

    public static String retrieveOperationBO(String site, String operation, String operationVersion) {
        return "OperationBO:" + site + "," + operation + "," + operationVersion;
    }

    public static String retriveResourceBO(String site, String resource) {
        return "ResourceBO:" + site + "," + resource;
    }

    public static String retrieveChildRouterBO(String site, String childRouter, String childRouterVersion) {
        return "ChildRouterBO:" + site + "," + childRouter + "," + childRouterVersion;
    }

    public static String retrieveUserBO(String site, String user) {
        return "UserBO:" + site + "," + user;
    }

    public static String retrieveShopOrderBO(String site, String shopOrder) {
        return "ShopOrderBO:" + site + "," + shopOrder;
    }

    public static String retrieveBomBO(String site, String bom, String bomVersion) {
        return "BomBO:" + site + "," + bom + "," + bomVersion;
    }

    public static String retrievePcuRouterBO(String site, String pcuRouter, String pcuRouterVersion){
        return "PcuRouterBO: " + site + "," + pcuRouter + "," + pcuRouterVersion;
    }

    public static String retrieveParentRouteBO(String site, String parentRoute, String parentRouteVersion){
        return "ParentRouteBO: " + site + "," + parentRoute + "," + parentRouteVersion;
    }

    public static String retrieveWorkCenterBO(String site, String workCenter) {
        return "WorkCenterBO:" + site + "," + workCenter;
    }

    public static String retrievePcuBomBO(String site, String pcuBom, String pcuBomVersion) {
        return "PcuBomBO:" + site + "," + pcuBom + "," + pcuBomVersion;
    }

    public static String retrieveBatchNoBO(String site, String batchNo) {
        return "BatchNoBO:" + site + "," + batchNo;
    }

    public static String retrieveOrderNoBO(String site, String orderNo) {
        return "OrderNoBO:" + site + "," + orderNo;
    }

    public static String retrieveRecipeNoBO(String site, String recipeName, String recipeVersion) {
        return "RecipeBO:" + site + "," + recipeName + "," + recipeVersion;
    }

    public static String getPcu(String pcuBO){
        String[] parts = pcuBO.split(":")[1].split(",");
        return parts[1];
    }

    public static String getItem(String itemBO) {
        String[] parts = itemBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getItemVersion(String itemBO) {
        String[] parts = itemBO.split(":")[1].split(",");
        return parts[2];
    }

    public static String getRouter(String routerBO){
        String[] parts = routerBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getRouterVersion(String routerBO){
        String[] parts = routerBO.split(":")[1].split(",");
        return parts[2];
    }

    public static String getOperation(String operationBO){
        String[] parts = operationBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getOperationVersion(String operationBO){
        String[] parts = operationBO.split(":")[1].split(",");
        return parts[2];
    }

    public static String getResource(String resourceBO){
        String[] parts = resourceBO.split(":")[1].split(",");
        return parts[1];
    }

    public static String getUser(String userBO){
        String[] parts = userBO.split(":")[1].split(",");
        return parts[1];
    }

    public static String getChildRouter(String childRouterBO){
        String[] parts = childRouterBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getChildRouterVersion(String childRouterBO){
        String[] parts = childRouterBO.split(":")[1].split(",");
        return parts[2];
    }
    public static String getShopOrder(String shopOrderBO) {
        String[] parts = shopOrderBO.split(":")[1].split(",");
        return parts[1];
    }

    public static String getBom(String bomBO){
        String[] parts = bomBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getBomVersion(String bomBO){
        String[] parts = bomBO.split(":")[1].split(",");
        return parts[2];
    }

    public static String getPcuRouter(String pcuRouterBO){
        String[] parts = pcuRouterBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getPcuRouterVersion(String pcuRouterBO){
        String[] parts = pcuRouterBO.split(":")[1].split(",");
        return parts[2];
    }

    public static String getRouting(String routingBO){
        String[] parts = routingBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getRoutingVersion(String routingBO){
        String[] parts = routingBO.split(":")[1].split(",");
        return parts[2];
    }

    public static String getParentRoute(String parentRouteBO){
        String[] parts = parentRouteBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getParentRouteVersion(String parentRouteBO){
        String[] parts = parentRouteBO.split(":")[1].split(",");
        return parts[2];
    }

    public static String getWorkCenter(String workCenterBO){
        String[] parts = workCenterBO.split(":")[1].split(",");
        return parts[1];
    }

    public static String getPcuBom(String pcuBomBO){
        String[] parts = pcuBomBO.split(":")[1].split(",");
        return parts[1];
    }
    public static String getPcuBomVersion(String pcuBomBO){
        String[] parts = pcuBomBO.split(":")[1].split(",");
        return parts[2];
    }
    public static String getOrderNo(String orderNo){
        String[] parts = orderNo.split(":")[1].split(",");
        return parts[1];
    }
    public static String getShift(String shiftBO) {
        String[] parts = shiftBO.split(":")[1].split(",");
        return parts[2].trim();  // Removing leading/trailing spaces
    }

    public static String getRecipeFromBatchRecipeHeader(String batchNoHeader) {
        String[] parts = batchNoHeader.split(",");
        if (parts.length < 4) {
            throw new IllegalArgumentException("recipe format mismatched");
        }
        return parts[2].trim();
    }

    public static String getRecipeVerFromBatchRecipeHeader(String batchNoHeader) {
        String[] parts = batchNoHeader.split(",");
        if (parts.length < 4) {
            throw new IllegalArgumentException("recipeVersion format mismatched");
        }
        return parts[3].trim();
    }

    public static String getBatchNoFromBatchRecipeHeader(String batchNoHeader) {
        String[] parts = batchNoHeader.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException("batchNo format mismatched");
        }
        return parts[5].trim();
    }

    public static String getBatchNoFromBatchHeader(String batchNoHeader) {
        String[] parts = batchNoHeader.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("batchNo format mismatched");
        }
        return parts[2].trim();
    }

    public static String getBatchNo(String batchNoHeader) {
        String[] parts = batchNoHeader.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("batchNo format mismatched");
        }
        return parts[1].trim();
    }

}