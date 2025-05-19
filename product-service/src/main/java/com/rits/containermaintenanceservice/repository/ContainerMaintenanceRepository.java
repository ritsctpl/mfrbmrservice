package com.rits.containermaintenanceservice.repository;

import com.rits.containermaintenanceservice.dto.Container;
import com.rits.containermaintenanceservice.model.ContainerMaintenance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContainerMaintenanceRepository extends MongoRepository<ContainerMaintenance, String > {
    Boolean existsByContainerAndSiteAndActiveEquals(String container, String site, int active);

    ContainerMaintenance findByContainerAndSiteAndActiveEquals(String container, String site, int active);

    List<Container>  findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(String site, int active);
}
