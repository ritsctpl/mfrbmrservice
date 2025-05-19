package com.rits.dhrservice.service;

import com.rits.dhrservice.dto.*;

import java.util.List;

public interface DhrService {
    List<ParametricMeasures> retrieveFromDataCollection(String site, String pcu) throws Exception;

    List<ToolLog> retrieveFromToolLog(String site, String pcu)throws Exception;

    List<NcData> retrieveFromLoggedNc(String site, String pcu)throws Exception;

    List<Component> retrieveFromAssembly(String site, String pcu) throws Exception;

    List<ProductionLogMongo> retrieveForWorkInstruction(String site, String pcu);

    Assembly retrieveAssemblyByPcy(String site, String pcu);
}
