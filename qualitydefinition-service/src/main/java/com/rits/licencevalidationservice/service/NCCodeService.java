package com.rits.licencevalidationservice.service;

import com.rits.licencevalidationservice.dto.*;
import com.rits.licencevalidationservice.model.DispositionRoutings;
import com.rits.licencevalidationservice.model.MessageModel;
import com.rits.licencevalidationservice.model.NCCode;

import java.util.List;

public interface NCCodeService {
    MessageModel createNCCode(NCCodeRequest ncCodeRequest) throws Exception;

    MessageModel updateNCCode(NCCodeRequest ncCodeRequest) throws Exception;

    NCCodeResponseList getNCCodeListByCreationDate(NCCodeRequest ncCodeRequest) throws Exception;

    NCCodeResponseList getNCCodeList(NCCodeRequest ncCodeRequest) throws Exception;

    NCCode retrieveNCCode(NCCodeRequest ncCodeRequest) throws Exception;

    MessageModel deleteNCCode(NCCodeRequest ncCodeRequest) throws Exception;

    Boolean isNCCodeExist(NCCodeRequest ncCodeRequest) throws Exception;

    List<NCCodeResponse> retrieveAllBySite(NCCodeRequest ncCodeRequest);

    NCCode associateRoutingToDispositionRoutingList(DispositionRoutingListRequest dispositionRoutingListRequest) throws Exception;


    NCCode removeRoutingFromDispositionRoutingList(DispositionRoutingListRequest dispositionRoutingListRequest) throws Exception;

    AvailableRoutingList getAvailableRoutings(NCCodeRequest ncCodeRequest) throws Exception;

    NCCode associateSecondariesToSecondariesGroupsList(SecondariesGroupsListRequest secondariesGroupsListRequest) throws Exception;

    NCCode removeSecondariesFromSecondariesGroupsList(SecondariesGroupsListRequest secondariesGroupsListRequest) throws Exception;

    AvailableSecondariesGroupsList getAvailableSecondaries(NCCodeRequest ncCodeRequest) throws Exception;

    Boolean associateNCGroupsToNCGroupsList(NCGroupsListRequest ncGroupsListRequest) throws Exception;

    Boolean removeNCGroupsToNCGroupsList(NCGroupsListRequest ncGroupsListRequest) throws Exception;

    AvailableNCGroupsList getAvailableNCGroups(NCCodeRequest ncCodeRequest) throws Exception;

    String callExtension(Extension extension);
    List<DispositionRoutings> getAllDispositionRouting(NCCodeRequest ncCodeRequest);

}
