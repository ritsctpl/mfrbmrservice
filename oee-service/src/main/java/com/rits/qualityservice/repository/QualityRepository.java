package com.rits.qualityservice.repository;


import com.rits.qualityservice.model.Quality;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QualityRepository extends MongoRepository<Quality, String> {
    List<Quality> findBySiteAndActiveAndProcessed(String site, int i, boolean b);
}
