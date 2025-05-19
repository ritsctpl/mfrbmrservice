package com.rits.podservice.repository;

import com.rits.podservice.dto.PodResponse;
import com.rits.podservice.model.Pod;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PodRepository extends MongoRepository<Pod,String> {
    long countByPodNameAndSiteAndActive(String podName,String site, int active);


    boolean existsByPodNameAndSiteAndActive(String podName, String site, int active);

    Pod findByPodNameAndSiteAndActive(String podName, String site, int active);

    List<PodResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);


    List<PodResponse> findByPodNameContainingIgnoreCaseAndSiteAndActive(String podName, String site, int active);
}
