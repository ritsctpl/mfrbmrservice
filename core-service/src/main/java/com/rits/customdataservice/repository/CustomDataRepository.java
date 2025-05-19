package com.rits.customdataservice.repository;

import com.rits.customdataservice.dto.Category;
import com.rits.customdataservice.dto.CategoryList;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.rits.customdataservice.model.CustomData;

import java.util.List;

public interface CustomDataRepository extends MongoRepository<CustomData, String> {

    public CustomData findBySiteAndCategoryAndActiveEquals(String site, String category, int active);

    boolean existsBySiteAndCategoryAndActiveEquals(String site, String category, int active);

    List<Category> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);
}
