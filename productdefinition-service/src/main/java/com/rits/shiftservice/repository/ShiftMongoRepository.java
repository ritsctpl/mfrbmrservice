package com.rits.shiftservice.repository;

import com.rits.shiftservice.dto.ShiftResponse;
import com.rits.shiftservice.model.Shift;
import com.rits.shiftservice.model.ShiftMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftMongoRepository extends MongoRepository<ShiftMongo, String> {
    List<ShiftMongo> findBySiteAndShiftIdAndActive(String site, String shiftId, int active);
    List<ShiftMongo> findTopBySiteOrderByCreatedDateTimeDesc(String site);
    List<ShiftResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active,String site);
    List<ShiftResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active,String site);
    List<ShiftMongo> findByVersion(String version);


}