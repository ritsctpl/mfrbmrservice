package com.rits.routingservice.repository;

import com.rits.routingservice.dto.RoutingResponse;
import com.rits.routingservice.model.Routing;
import com.rits.routingservice.model.RoutingType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoutingRepository extends MongoRepository<Routing, String> {
    boolean existsByRoutingAndVersionAndSiteAndActiveEquals(String routing, String version, String site, int active);

    List<Routing> findByRoutingAndSiteAndActive(String routing, String site, int active);

    Routing findByRoutingAndVersionAndSiteAndActive(String routing, String version, String site, int active);

    List<RoutingResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<RoutingResponse> findByActiveAndSiteAndRoutingContainingIgnoreCase(int i, String site, String routing);
    List<RoutingType> findByActiveAndSiteAndRoutingTypeContainingIgnoreCase(int i, String site, String routingType);

    Routing findByRoutingAndCurrentVersionAndSiteAndActive(String routing, boolean currentVersion, String site, int active);

    List<RoutingResponse> findByActiveAndSite(int active, String site);

    Routing findByActiveAndSiteAndHandle(int active, String site, String routerBO);

    boolean existsByRoutingAndCurrentVersionAndSiteAndActive(String routing, boolean b, String site, int i);
}
