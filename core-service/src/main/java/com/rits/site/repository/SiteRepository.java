package com.rits.site.repository;

import com.rits.site.dto.RetrieveTop50;
import com.rits.site.model.Site;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SiteRepository extends MongoRepository<Site,String> {
  Site findBySite(String site);
  Boolean existsBySite(String site);
  List<RetrieveTop50>  findTop50ByOrderByCreatedDateTimeDesc();
}
