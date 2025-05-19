package com.rits.machinestatusservice.repository;
import com.rits.machinestatusservice.model.MachineStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.crypto.Mac;
import java.util.List;

public interface MachineStatusRepository extends MongoRepository<MachineStatus,String> {
    List<MachineStatus> findByEventAndResourceAndActive(String event, String resource, int active);

    List<MachineStatus> findBySiteAndActive(String site,int active);

    List<MachineStatus> findBySiteAndEventAndResourceAndActive(String site, String mcDown, String resource, int i);

    List<MachineStatus> findBySiteAndEventAndResourceAndActiveAndShiftStartTime(String site, String mcDown, String resource, int i, String shiftStartTime);

    List<MachineStatus> findBySiteAndEventAndResourceAndActiveAndShiftStartTimeAndCreatedDate(String site, String mcDown, String resource, int i, String shiftStartTime, String createdDate);
    MachineStatus findTop1BySiteAndActiveAndResourceAndEventOrderByCreatedDateTime(String site,int active,String resource,String event);
}
