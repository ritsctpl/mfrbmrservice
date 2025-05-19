package com.rits.scrapservice.repository;

import com.rits.scrapservice.model.Scrap;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScrapRepository extends MongoRepository<Scrap,String> {
    Scrap findByActiveAndSiteAndPcuBO(int i, String site, String pcu);

    List<Scrap> findByActiveAndSite(int i, String site);
}
