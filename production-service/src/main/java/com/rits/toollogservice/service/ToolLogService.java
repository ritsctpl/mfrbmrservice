package com.rits.toollogservice.service;

import com.rits.toollogservice.dto.ToolLogRequest;
import com.rits.toollogservice.model.ToolLog;
import com.rits.toollogservice.model.ToolLogMessageModel;

import java.util.List;

public interface ToolLogService {
    ToolLogMessageModel logTool(ToolLogRequest toolLogRequest) throws Exception;

    List<ToolLog> retrieveBySiteAndPcu(String site, String pcu) throws Exception;
}
