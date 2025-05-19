package com.rits.datacollectionservice.service;

import com.rits.datacollectionservice.dto.DataCollectionList;
import com.rits.datacollectionservice.dto.DataCollectionRequest;
import com.rits.datacollectionservice.dto.DataCollectionResponseList;
import com.rits.datacollectionservice.dto.Extension;
import com.rits.datacollectionservice.model.Attachment;
import com.rits.datacollectionservice.model.DataCollection;
import com.rits.datacollectionservice.model.DataCollectionMessageModel;
import com.rits.datacollectionservice.model.Parameter;

import java.util.List;

public interface DataCollectionService {
    DataCollectionMessageModel createDataCollection(DataCollectionRequest dataCollectionRequest) throws Exception;

    DataCollectionMessageModel updateDataCollection(DataCollectionRequest dataCollectionRequest) throws Exception;

    DataCollection retrieveDataCollection(String dataCollection, String version, String site) throws Exception;

    DataCollectionResponseList getDataCollectionList(String dataCollection, String site) throws Exception;

    DataCollectionResponseList getDataCollectionListByCreationDate(String site) throws Exception;

    DataCollectionMessageModel deleteDataCollection(String dataCollection, String version, String site, String userId) throws Exception;

    List<String> getDcGroupNameListByItem(String item, String site) throws Exception;

    List<String> getDcGroupNameListByPCU(String pcu, String item, String site) throws Exception;

    List<String> getDcGroupNameListByItemOperation(String item, String operation, String site) throws Exception;

    List<String> getDcGroupNameListByPcuOperation(String pcu, String operation, String site) throws Exception;

    List<String> getDcGroupNameListByItemOperationRouting(String item, String operation, String routing, String site) throws Exception;

    List<String> getDcGroupNameListByOperationRoutingPcu(String operation, String routing, String pcu, String site, String item) throws Exception;//this should call the getDcGroupNameListByItem also and merge the output

    List<String> getDcGroupNameListByResource(String resource, String site) throws Exception;

    List<String> getDcGroupNameListByResourcePcu(String resource, String pcu, String site) throws Exception;

    List<String> getDcGroupNameListByWorkCenter(String workCenter, String site, String resource) throws Exception;//it should call getDcGroupNameListByResource and merge the output.Before that logic should get all the resource from the workCenter

    List<String> getDcGroupNameListByShopOrder(String shopOrder, String site, String item) throws Exception;//this should call the getDcGroupNameListByItem also and merge the output

    List<String> getDcGroupNameListByShopOrderOperation(String shopOrder, String operation, String site) throws Exception;

    DataCollectionList findByOperationPcuAndResource(String site, String operation, String resource, String pcu) throws Exception;

    String callExtension(Extension extension) throws Exception;

    List<String> generateCombinations(List<String> elements);

    void generateCombinationsHelper(List<String> elements, String prefix, int startIndex, List<String> result);

    DataCollectionList retrieveByAttachment(List<Attachment> attachmentList, String pcu, String operation, String resource,String site) throws Exception;

    List<Parameter> retrieveAllParameterNames(String site) throws Exception;
}
