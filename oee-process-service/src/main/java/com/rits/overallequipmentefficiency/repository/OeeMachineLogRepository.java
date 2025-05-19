package com.rits.overallequipmentefficiency.repository;


import com.rits.overallequipmentefficiency.model.MachineLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OeeMachineLogRepository extends JpaRepository<MachineLogModel,Long> {
    @Transactional
    @Modifying
    @Query("UPDATE MachineLogModel m SET m.active = 0 WHERE m.resourceId = :resourceId AND m.active = 1")
    void deactivateOldRecords(@Param("resourceId") String resourceId);

}
