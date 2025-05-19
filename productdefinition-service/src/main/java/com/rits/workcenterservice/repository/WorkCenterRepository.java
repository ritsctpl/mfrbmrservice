package com.rits.workcenterservice.repository;

import com.rits.workcenterservice.dto.AvailableWorkCenter;
import com.rits.workcenterservice.dto.WorkCenterResponse;
import com.rits.workcenterservice.model.WorkCenter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface WorkCenterRepository extends MongoRepository<WorkCenter, String> {
    public boolean existsByWorkCenterAndSiteAndActiveEquals(String workCenter,String site, int active);

    public WorkCenter findByWorkCenterAndActiveAndSite(String workCenter,int active, String site);

    public List<WorkCenterResponse> findByWorkCenterCategoryAndActiveAndSite (String workCenterCategory, int active, String site);

    public List<WorkCenterResponse> findByWorkCenterContainingIgnoreCaseAndSiteAndActiveEquals(String workCenter, String site,int active);

    public List<WorkCenterResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active,String  site);


    public List<AvailableWorkCenter> findByActiveAndSite(int active, String site);


    List<WorkCenterResponse> findByActiveAndSiteAndAddAsErpWorkCenter(int active, String site, boolean addAsErpWorkCenter);


    WorkCenter findByActiveAndSiteAndAssociationListTypeAndAssociationListAssociateId(int active, String site, String type, String associateId);



    List<WorkCenter> findTop50BySite(String site);

    List<WorkCenter> findByTrackOeeAndActiveAndSite(boolean trackOee, int active, String site);


    @Query("{ 'trackOee': ?0, 'active': ?1, 'site': ?2, 'workCenterCategory': ?3 }")
    List<WorkCenter> findByTrackOeeAndActiveAndSiteAndWorkCenterCategory(
            boolean trackOee, int active, String site, String workCenterCategory);
//    public boolean existsByAssociationListAssociateIdAndSiteAndActiveEquals(String associateId,String site, int active);

    /**
     * Find a cell workcenter (parent) for a given line workcenter id.
     * It looks for a WorkCenter document with workCenterCategory "Cell" that contains
     * the given child workcenter id in its associationList.
     */
    @Query("{ 'site': ?0, 'workCenterCategory': 'Cell', 'associationList.associateId': ?1 }")
    WorkCenter findCellForLine(String site, String childWorkCenterId);

    /**
     * Find a cell group workcenter (parent) for a given cell id.
     * It looks for a WorkCenter document with workCenterCategory "Cell Group" that contains
     * the given cell id in its associationList.
     */
    @Query("{ 'site': ?0, 'workCenterCategory': 'Cell Group', 'associationList.associateId': ?1 }")
    WorkCenter findCellGroupForCell(String site, String cellId);

}
