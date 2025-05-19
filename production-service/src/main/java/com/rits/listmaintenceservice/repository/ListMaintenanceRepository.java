package com.rits.listmaintenceservice.repository;

import com.rits.listmaintenceservice.dto.ListMaintenanceResponse;
import com.rits.listmaintenceservice.model.ListMaintenance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ListMaintenanceRepository extends MongoRepository<ListMaintenance,String> {
    public boolean existsByActiveAndSiteAndList(int active, String site, String list);

    public ListMaintenance findByActiveAndSiteAndList(int active, String site, String list);

    public List<ListMaintenanceResponse> findByActiveAndSiteAndListContainingIgnoreCase(int active, String site, String list);

    public List<ListMaintenanceResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<ListMaintenanceResponse> findByActiveAndSiteAndCategory(int active, String site, String category);

    boolean existsByActiveAndSiteAndListAndCategory(int i, String site, String list, String category);

    ListMaintenance findByActiveAndSiteAndListAndCategory(int i, String site, String list, String category);
}
