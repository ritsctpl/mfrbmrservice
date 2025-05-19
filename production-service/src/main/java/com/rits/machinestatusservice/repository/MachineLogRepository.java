package com.rits.machinestatusservice.repository;

import com.rits.machinestatusservice.model.MachineLog;
import com.rits.machinestatusservice.model.MachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MachineLogRepository extends JpaRepository<MachineLog, Integer> {
    List<MachineLog> findBySiteIdAndLogEventAndResourceIdAndActiveAndShiftStartTimeAndCreatedDate(
            String siteId, String mcDown, String resourceId, int i, String shiftStartTime, String createdDate);
}
