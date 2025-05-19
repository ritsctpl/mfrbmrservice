package com.rits.performance.controller;

import com.rits.common.dto.OeeFilterRequest;
import com.rits.performance.dto.*;
import com.rits.performance.exception.PerformanceException;
import com.rits.performance.model.OeePerformanceEntity;
import com.rits.performance.service.PerformanceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/v1/performance-service")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    // Endpoint for overall performance
    @PostMapping("/overall")
    public ResponseEntity<OverallPerformanceResponse> getOverallPerformance(@RequestBody OeeFilterRequest request) throws Exception{
        return ResponseEntity.ok(performanceService.getOverallPerformance(request));
    }

    // Endpoint for performance by time
    @PostMapping("/performanceByTime")
    public ResponseEntity<PerformanceByTimeResponse> getPerformanceByTime(@RequestBody OeeFilterRequest request) throws Exception{
        return ResponseEntity.ok(performanceService.getPerformanceByTime(request));
    }

    // Endpoint for performance by shift
    @PostMapping("/performanceByShift")
    public ResponseEntity<PerformanceByShiftResponse> getPerformanceByShift(@RequestBody OeeFilterRequest request) throws Exception {
        return ResponseEntity.ok(performanceService.getPerformanceByShift(request));
    }

    // Endpoint for performance by machine
    @PostMapping("/performanceByMachine")
    public ResponseEntity<PerformanceByMachineResponse> getPerformanceByMachine(@RequestBody OeeFilterRequest request) throws Exception{
        return ResponseEntity.ok(performanceService.getPerformanceByMachine(request));
    }

    // Endpoint for performance by production line
    @PostMapping("/performanceByProductionLine")
    public ResponseEntity<PerformanceByProductionLineResponse> getPerformanceByProductionLine(@RequestBody OeeFilterRequest request) throws Exception{
        return ResponseEntity.ok(performanceService.getPerformanceByProductionLine(request));
    }

    // Endpoint for performance by reason
    @PostMapping("/performanceByReason")
    public ResponseEntity<PerformanceByReasonResponse> getPerformanceByReason(@RequestBody OeeFilterRequest request) {
        return ResponseEntity.ok(performanceService.getPerformanceByReason(request));
    }

    // Endpoint for performance heatmap
    @PostMapping("/performanceHeatMap")
    public ResponseEntity<PerformanceHeatMapResponse> getPerformanceHeatMap(@RequestBody OeeFilterRequest request) {
        return ResponseEntity.ok(performanceService.getPerformanceHeatMap(request));
    }

    // Endpoint for performance by downtime analysis
    @PostMapping("/performanceByDowntime")
    public ResponseEntity<PerformanceByDowntimeResponse> getPerformanceByDowntime(@RequestBody OeeFilterRequest request) {
        return ResponseEntity.ok(performanceService.getPerformanceByDowntime(request));
    }

    // Endpoint for performance by operator
    @PostMapping("/performanceByOperator")
    public ResponseEntity<PerformanceByOperatorResponse> getPerformanceByOperator(@RequestBody OeeFilterRequest request) {
        return ResponseEntity.ok(performanceService.getPerformanceByOperator(request));
    }

    // Endpoint for performance comparison
    /*@PostMapping("/performanceComparison")
    public ResponseEntity<PerformanceComparisonResponse> getPerformanceComparison(@RequestBody OeeFilterRequest request) {
        return ResponseEntity.ok(performanceService.getPerformanceComparison(request));
    }*/

    @PostMapping("/calculatePerformance")
    public Boolean calculatePerformance(@RequestBody PerformanceRequest performanceRequest) {

        if(StringUtils.isEmpty(performanceRequest.getSite()))
            throw new PerformanceException(1001);
        try{
            return performanceService.calculatePerformance(performanceRequest);
        }catch (PerformanceException performanceException){
            throw performanceException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/performanceUniqueComb")
    public ResponseEntity<List<PerformanceResponseDto>> performanceUniqueComb(@RequestBody OeeFilterRequest request){
        if(StringUtils.isEmpty(request.getSite()))
            throw new PerformanceException(1001);

        try{
            return ResponseEntity.ok(performanceService.performanceUniqueComb(request));

        }catch (PerformanceException performanceException){
            throw performanceException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getPerfromanceByDateRange")
    public ResponseEntity<List<OeePerformanceEntity>> performanceByDateRange(@RequestBody OeeFilterRequest request){
        if(StringUtils.isEmpty(request.getSite()))
            throw new PerformanceException(1001);

        try{
            return ResponseEntity.ok(performanceService.performanceByDateRange(request));

        }catch (PerformanceException performanceException){
            throw performanceException;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/performanceByEfficiencyOfProduct")
    public ResponseEntity<PerformanceByEfficiencyOfProduct> getPerformanceByEfficiencyOfProduct(@RequestBody OeeFilterRequest request) throws Exception{
        return ResponseEntity.ok(performanceService.getPerformanceByEfficiencyOfProduct(request));
    }

    @PostMapping("/performanceLossReasons")
    public ResponseEntity<PerformanceLossReasonsResponse> getPerformanceLossReasons(@RequestBody OeeFilterRequest request) throws Exception{
        return ResponseEntity.ok(performanceService.getPerformanceLossReasons(request));
    }
}
