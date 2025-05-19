package com.rits.workinstructionservice.repository;

import com.rits.workinstructionservice.dto.WorkInstructionList;
import com.rits.workinstructionservice.dto.WorkInstructionResponse;
import com.rits.workinstructionservice.model.WorkInstruction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkInstructionRepository extends MongoRepository<WorkInstruction,String> {
    public Boolean  existsByWorkInstructionAndSiteAndActiveEquals(String workInstruction,String site,int active);

    public WorkInstruction findByWorkInstructionAndRevisionAndSiteAndActiveEquals(String workInstruction,String revision,String site,int active);

    public List<WorkInstructionList> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active,String site);

    public List<WorkInstructionList> findBySiteAndWorkInstructionContainingIgnoreCaseAndActiveEquals(String site,String workInstruction,int active);

    public List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListItem(int active,String site,String item);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListItemAndAttachmentListOperation(int active, String site, String item, String operation);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListPcuAndAttachmentListOperation(int active, String site, String pcu, String operation);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListItemAndAttachmentListOperationAndAttachmentListRouting(int active, String site, String item, String operation, String routing);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListResource(int active, String site, String resource);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListResourceAndAttachmentListPcu(int active, String site, String resource, String pcu);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListShopOrderAndAttachmentListOperation(int active, String site, String shopOrder, String operation);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListPcu(int active, String site, String pcu);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListOperationAndAttachmentListRoutingAndAttachmentListPcu(int active, String site, String operation, String routing, String pcu);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListShopOrder(int active, String site, String shopOrder);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListWorkCenter(int active, String site, String workCenter);

   List< WorkInstruction> findByWorkInstructionAndSiteAndActive(String workInstruction, String site, int active);


   // WorkInstruction findByWorkInstructionAndErpFilenameAndActiveAndSite(String workInstruction, String erpFilename, int active, String site);

    WorkInstruction findByWorkInstructionAndFileNameAndActiveAndSite(String workInstruction, String fileName, int active, String site);

    boolean existsByWorkInstructionAndRevisionAndSiteAndActiveEquals(String workInstruction, String revision, String site, int active);

    WorkInstruction findByWorkInstructionAndRevisionAndSiteAndActive(String workInstruction, String revision, String site, int active);

    WorkInstruction findByWorkInstructionAndCurrentVersionAndSiteAndActive(String workInstruction, boolean currentVersion, String site, int active);

    WorkInstruction findByWorkInstructionAndRevisionAndFileNameAndActiveAndSite(String workInstruction, String revision, String fileName, int i, String site);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListResourceAndAttachmentListOperation(int i, String site, String resource, String operation);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListBom(int active, String site, String bom);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListBomVersion(int active, String site, String bomVersion);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListComponent(int active, String site, String component);

    List<WorkInstructionResponse> findByActiveAndSiteAndAttachmentListComponentVersion(int active, String site, String componentVersion);
}
