package com.rits.queryBuilder.repository;

import com.rits.queryBuilder.model.DataSourceData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DataSourceRepository extends MongoRepository<DataSourceData, String> {
    List<DataSourceData> findBySiteAndActive(String site, int active);
    List<DataSourceData> findByStatus(boolean status);
    Optional<DataSourceData> findBySiteAndHandleAndActive(String site, String handle, int active);
    DataSourceData findBySiteAndDataSourceIdAndActive(String site, String dataSourceId, int active);

}
