package com.rits.oeeservice.service;

import com.rits.oeeservice.model.ApiConfiguration;
import java.util.Map;

public interface StoredProcedureService {
    Map<String, Object> executeStoredProcedure(ApiConfiguration apiConfig, Map<String, Object> parameters);
}
