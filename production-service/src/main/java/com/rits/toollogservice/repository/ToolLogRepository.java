package com.rits.toollogservice.repository;

import com.rits.toollogservice.dto.ToolNumber;
import com.rits.toollogservice.model.ToolLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ToolLogRepository extends MongoRepository<ToolLog,String> {
    ToolLog findByActiveAndSiteAndToolNumberBOAndAttachment(int i, String site, String toolNumberBO, String attachment);

    List<ToolLog> findByActiveAndSiteAndPcuBO(int i, String site, String s);
}
