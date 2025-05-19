package com.rits.oeeservice.service;

import com.rits.oeeservice.dto.ParameterMetaDto;
import com.rits.oeeservice.model.ApiConfiguration;
import com.rits.oeeservice.service.StoredProcedureService;
import com.rits.oeeservice.util.ParameterConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.Array;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class StoredProcedureServiceImpl implements StoredProcedureService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> executeStoredProcedure(ApiConfiguration apiConfig, Map<String, Object> parameters) {
        List<Map<String, Object>> result;

        // Check if input parameters are provided
        if (parameters == null || parameters.isEmpty()) {
            // No input parameters: build simple SQL call
            String sql = "SELECT * FROM " + apiConfig.getStoredProcedure() + "()";
            result = jdbcTemplate.queryForList(sql);
        } else {
            // There are input parameters: build the function call dynamically
            List<ParameterMetaDto> paramList = ParameterConverter.convertJsonToParameterList(apiConfig.getInputParameters());
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(apiConfig.getStoredProcedure()).append("(");
            Object[] args = new Object[paramList.size()];
            for (int i = 0; i < paramList.size(); i++) {
                if (i > 0) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("?");
                String paramName = paramList.get(i).getName();
                Object value = parameters.get(paramName);

                if ("ARRAY".equalsIgnoreCase(paramList.get(i).getType())) {
                    // If the value is a List, convert it to a proper SQL Array.
                    if (value instanceof List) {
                        List<?> list = (List<?>) value;
                        Object[] arrayObject = list.toArray();
                        Connection connection = null;
                        try {
                            connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
                            // Use configurable array element type; default to "integer" if not provided
                            String sqlType = paramList.get(i).getElementType();
                            if (sqlType == null || sqlType.trim().isEmpty()) {
                                sqlType = "integer";
                            }
                            Array sqlArray = connection.createArrayOf(sqlType, arrayObject);
                            args[i] = sqlArray;
                        } catch (SQLException e) {
                            throw new RuntimeException("Error creating SQL Array", e);
                        } finally {
                            if (connection != null) {
                                DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
                            }
                        }
                    } else {
                        args[i] = value;
                    }
                } else {
                    args[i] = value;
                }
            }
            sqlBuilder.append(")");
            String sql = sqlBuilder.toString();
            result = jdbcTemplate.queryForList(sql, args);
        }
        return Map.of("data", result);
    }
}
