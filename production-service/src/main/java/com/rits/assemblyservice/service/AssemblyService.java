package com.rits.assemblyservice.service;

import com.rits.assemblyservice.dto.ComponentResponse;
import com.rits.assemblyservice.dto.DataType;
import com.rits.assemblyservice.dto.Extension;
import com.rits.assemblyservice.model.Assembly;
import com.rits.assemblyservice.model.Component;
import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import com.rits.pcucompleteservice.dto.PcuCompleteRequestInfo;

import java.util.List;


public interface AssemblyService {
    public boolean addComponent(String site, String parentPcuBO,String userId, String pcuBO, Component component)throws Exception;

    public boolean removeComponent(String site,String parentPcuBO, String pcuBO, Component component,boolean inventoryReturn,boolean inventoryScrap)throws Exception;
    public boolean groupAddComponent(String site, String pcuBO, List<Component> component)throws Exception;
    public boolean groupRemoveComponent(String site, String pcuBO, List<Component> component,boolean inventoryReturn,boolean inventoryScrap)throws Exception;
    public  List<Component> findAssembledComponent(String site,String pcuBO) throws Exception;
    public List<Component> findAssembledByComponent(String site,String pcuBo, String component, boolean removedComponentNeeded)throws Exception;
    public List<Component> findAssembledComponentHistory(String site, String pcuBO) throws Exception;
    public List<Component> findAssembledComponentByOperation(String site,String pcuBO, String operation, boolean removedComponentNeeded) throws Exception;
    public List<Component> findAssembledComponentByCustomField(String site,String pcuBO, String dataAttribute, String dataField) throws Exception;
    public Component findAssembledComponentById(String site,String pcuBO, String id) throws Exception;
    public boolean addNonBomComponent( String site,String pcuBO,  Component AssemblyDataItem) throws Exception;
    public String callExtension(Extension extension) throws Exception;
    public DataType retrieveDataType(String site, String bom , String revision, String component) ;
    public ComponentResponse findComponent(String site,String findComponent) throws Exception;
    public Assembly getAssembly(String site, String pcuBO) throws Exception;
    public Assembly retrieveAssembly(String site, String pcuBO) throws Exception;
    public Assembly getSerializedAssembly(String site,String pcuBO) throws Exception;

    Assembly getSerializedAssemblyByPcuAndItem(String site, String pcuBO, String itemBO) throws Exception;

    public List<Assembly> getChildAssembly(String site, String pcuBO) throws Exception;


    boolean addNonBomComponents(String site,String parentPcuBO, String pcuBO, Component component) throws Exception;

    Assembly retrieveSerializedComponentListForReturnAndReplace(String site, String pcuBO);

    Assembly retrieveNonSerializedComponentListForReturnAndReplace(String site, String pcuBO);

    boolean isAllQuantityAssembled(PcuCompleteReq pcuCompleteReqWithBO);
    Assembly retrieveAssemblyAggr(String site, String pcuBO);
    Assembly buildAssemblyHierarchy(String rootPcuBO);

     /* Added for PCU add or Update Component.  - Senthil POC.
     * */
    public boolean addOrUpdateComponent(String site, String parentPcuBO,String userId, String pcuBO, Component component,String parentItem)throws Exception;

    }
