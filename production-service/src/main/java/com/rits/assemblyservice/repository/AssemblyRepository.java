package com.rits.assemblyservice.repository;

import com.rits.assemblyservice.model.Assembly;
import com.rits.assemblyservice.model.Component;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssemblyRepository extends MongoRepository<Assembly,String> {
    boolean existsByActiveAndSiteAndPcuBO(int i, String site, String pcuBO);

    List<Component> findByActiveAndSiteAndPcuBOAndComponentList_Removed(int i, String site, String pcuBO, boolean removed);

    List<Component> findByActiveAndSiteAndPcuBOAndComponentList_ComponentAndComponentList_Removed(int i, String site, String pcuBO, boolean removedComponentNeeded);

    Assembly findByActiveAndSiteAndPcuBO(int i, String site, String pcuBO);

//    @Query("{'active': 1, 'site': ?1, 'pcuBO': ?2, 'componentList.OperationBO': ?3, 'componentList.removed': ?4}")
    List<Component> findByActiveAndSiteAndPcuBOAndComponentList_OperationAndComponentList_Removed(int i, String site, String pcuBO, String operation, boolean removedComponentNeeded);

    Component findByActiveAndSiteAndPcuBOAndComponentList_UniqueIDAndComponentList_Removed(int i, String site, String pcuBO, String id, boolean b);

    Component findByActiveAndSiteAndPcuBOAndPcuRouterBOAndComponentList_StepId(int i, String site, String pcuBO, String pcuRouterBO,String stepId);

    List<Assembly> findByActiveAndSiteAndParentPcuBO(int i, String site, String pcuBO);

    Assembly findByActiveAndSiteAndPcuBOAndItemBO(int i, String site, String pcuBO, String itemBO);

    List<Assembly> findByActiveAndSiteAndParentPcuBOAndItemBO(int i, String site, String pcuBO, String itemBO);

    /* Added for PCU add or Update Component.  - Senthil POC. findById,findByParentPcuBO,findByPcuBO,findByAncestryPcuBO,findByComponentCriteria
     * */
    // This method is provided by MongoRepository for finding a document by its ID.
    @Override
    Optional<Assembly> findById(String id);

    // Custom query to find components by their parentPcuBO.
    List<Assembly> findByParentPcuBO(String parentPcuBO);

    Optional<Assembly> findByPcuBO(String pcuBO);

    // Assuming the ancestry field is a list of objects that contain a pcuBO field,
    // this custom query finds documents where any ancestry object's pcuBO field matches the provided value.
    @Query("{'ancestry.pcuBO': ?0}")
    List<Assembly> findByAncestryPcuBO(String pcuBO);

    // If needed, you can also add methods for checking component existence based on multiple criteria (step 3)
    // Example for a method that might be used to find a component based on several fields:
    @Query("{'componentList.sequence': ?0, 'componentList.component': ?1, 'componentList.assembledBy': ?2, 'componentList.operation': ?3, 'componentList.resourceBO': ?4}")
    List<Assembly> findByComponentCriteria(String sequence, String component, String assembledBy, String operation, String resourceBO);
}
