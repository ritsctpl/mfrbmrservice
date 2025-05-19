package com.rits.oeeservice.controller;

import com.rits.oeeservice.dto.ApiResponseDto;
import com.rits.oeeservice.dto.ParameterMetaDto;
import com.rits.oeeservice.model.ApiConfiguration;
import com.rits.oeeservice.service.ApiRegistryService;
import com.rits.oeeservice.service.StoredProcedureService;
import com.rits.oeeservice.util.ParameterConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/app/v1/oee-service/apiregistry")
public class GenericApiController {

    @Autowired
    private ApiRegistryService apiRegistryService;

    @Autowired
    private StoredProcedureService storedProcedureService;

    @RequestMapping(value = "/{apiName}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResponseDto handleApiRequest(@PathVariable String apiName,
                                           @RequestBody(required = false) Map<String, Object> requestBody,
                                           HttpServletRequest request) {
        try {
            ApiConfiguration apiConfig = apiRegistryService.getApiConfiguration(apiName);
            if (apiConfig == null) {
                return new ApiResponseDto("error", null, "API not registered");
            }
            // Validate that the HTTP method of the request matches what is configured
            if (!request.getMethod().equalsIgnoreCase(apiConfig.getHttpMethod())) {
                return new ApiResponseDto("error", null, "HTTP method not allowed for this API");
            }

            Map<String, Object> parameters;
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                // For GET requests, extract parameters from the query string
                parameters = request.getParameterMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue()[0] // assuming single-valued parameters
                        ));
            } else {
                // For POST requests, use the JSON body (which might be null if not provided)
                parameters = requestBody != null ? requestBody : new HashMap<>();
            }

            // Convert JSON metadata into a list of parameter definitions
            List<ParameterMetaDto> inputParams = ParameterConverter.convertJsonToParameterList(apiConfig.getInputParameters());
            // Validate and convert parameters
            Map<String, Object> processedParams = ParameterConverter.validateAndConvertParameters(inputParams, parameters);
            // Execute the stored procedure / function
            Map<String, Object> result = storedProcedureService.executeStoredProcedure(apiConfig, processedParams);
            return new ApiResponseDto("success", result, "Procedure executed successfully");
        } catch (Exception ex) {
            return new ApiResponseDto("error", null, ex.getMessage());
        }
    }
}
