package com.rits.shiftservice.repository;

import com.rits.shiftservice.dto.ShiftResponse;
import com.rits.shiftservice.model.Shift;
import org.springframework.data.mongodb.core.aggregation.DocumentOperators;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift,Integer>{
//    List<ShiftResponse> findBySiteAndActiveAndShiftNameContainingIgnoreCase(String site, int i, String shiftName);
//
//    List<ShiftResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int i, String site);
//
//    Shift findBySiteAndShiftNameAndActive(String site, String shiftName,  int i);
//
//    List<Shift> findBySiteAndActiveAndShiftType(String site, int i, String shiftType);
//
//    List<Shift> findBySiteAndActiveAndShiftTypeAndResource(String site, int i, String shiftType, String resource);
//
//    List<Shift> findBySiteAndActiveAndShiftTypeAndWorkCenter(String site, int i, String shiftType, String workCenter);
//
//    List<Shift> findBySiteAndActive(String site, int i);
//
//    boolean existsBySiteAndShiftNameAndActive(String site, String shiftName, int i);
}

