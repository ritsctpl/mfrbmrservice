package com.rits.availability.controller;

import com.rits.availability.dto.*;
import com.rits.availability.exception.AvailabilityException;
import com.rits.availability.model.OeeAvailabilityEntity;
import com.rits.common.dto.OeeFilterRequest;
import com.rits.oeeservice.dto.OeeByMachineResponse;
import com.rits.oeeservice.dto.OeeByShiftResponse;
import com.rits.oeeservice.dto.OeeByTimeResponse;
import com.rits.oeeservice.dto.OverallOeeResponse;
import com.rits.oeeservice.exception.OeeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rits.availability.service.AvailabilityService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/availability-service")
public class AvailabilityController {


    private final AvailabilityService availabilityService;


    @PostMapping("/logAvailability1")
    public ResponseEntity<AvailabilityResponse> getlogAvailability(@RequestBody AvailabilityRequest availabilityRequest){
        if(availabilityRequest.getSite() != null || !availabilityRequest.getSite().isEmpty() ) {
            try {
                AvailabilityResponse response = availabilityService.getLogAvailability(availabilityRequest);
                return ResponseEntity.ok(response);
            } catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }
    @PostMapping("/logAvailability")
    public Boolean logAvailability(@RequestBody AvailabilityRequest availabilityRequest){
        if(availabilityRequest.getSite() != null || !availabilityRequest.getSite().isEmpty() ) {
            try {
                Boolean response = availabilityService.logAvailability(availabilityRequest);
                return response;
            } catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }

    @PostMapping("/calculateAvailability")
    public ResponseEntity<CalculateAvailabilityResponse> calculateAvailability(@RequestBody AvailabilityRequest availabilityRequest){
        if(availabilityRequest.getSite() != null || !availabilityRequest.getSite().isEmpty() ) {
            try {
                CalculateAvailabilityResponse response = availabilityService.calculateAvailability(availabilityRequest);
                return ResponseEntity.ok(response);
            } catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }
    @PostMapping("/deleteAvailability")
    public ResponseEntity<AvailabilityResponse> deleteAvailability(@RequestBody AvailabilityRequest request){
        if(request.getSite() != null || !request.getSite().isEmpty() ) {
            try {
                if(request.getResourceId() == null || request.getResourceId().isEmpty()){
                    throw new AvailabilityException(1007);
                }
                if(request.getWorkcenterId() == null || request.getWorkcenterId().isEmpty()){
                    throw new AvailabilityException(1006);
                }
                AvailabilityResponse response = availabilityService.deleteAvailability(request);
                return ResponseEntity.ok(response);
            } catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }
    @PostMapping("/overallAvailability")
    public ResponseEntity<OverallAvailabilityResponse> getOverallAvailability(@RequestBody OeeFilterRequest oeeFilterRequest) {
            if (oeeFilterRequest.getSite() != null && !oeeFilterRequest.getSite().isEmpty()) {
                try {
                    OverallAvailabilityResponse response = availabilityService.getOverallAvailability(oeeFilterRequest);
                    return ResponseEntity.ok(response);
                } catch (AvailabilityException availabilityException) {
                    throw availabilityException;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            throw new AvailabilityException(1001);
        }


    @PostMapping("/availabilityByTime")
    public ResponseEntity<AvailabilityByTimeResponse> getAvailabilityByTime(@RequestBody OeeFilterRequest oeeFilterRequest) {
        if (oeeFilterRequest.getSite() != null && !oeeFilterRequest.getSite().isEmpty()) {
            try {
                AvailabilityByTimeResponse response = availabilityService.getAvailabilityByTime(oeeFilterRequest);
                return ResponseEntity.ok(response);
            } catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }


    @PostMapping("/availabilityByShift")
    public ResponseEntity<AvailabilityByShiftResponse> getAvailabilityByShift(@RequestBody OeeFilterRequest oeeFilterRequest) {
        if (oeeFilterRequest.getSite() != null && !oeeFilterRequest.getSite().isEmpty()) {
            try {
                AvailabilityByShiftResponse response = availabilityService.getAvailabilityByShift(oeeFilterRequest);
                return ResponseEntity.ok(response);
            } catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }

    @PostMapping("/availabilityByMachine")
    public ResponseEntity<AvailabilityByMachineResponse> getAvailabilityByMachine(@RequestBody OeeFilterRequest oeeFilterRequest) {
        if (oeeFilterRequest.getSite() != null && !oeeFilterRequest.getSite().isEmpty()) {
            try {
                AvailabilityByMachineResponse response = availabilityService.getAvailabilityByMachine(oeeFilterRequest);
                return ResponseEntity.ok(response);
            } catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }
    @PostMapping("/availabilityByWorkcenter")
    public ResponseEntity<AvailabilityByWorkcenterResponse> getAvailabilityByWorkcenter(@RequestBody OeeFilterRequest oeeFilterRequest) {
        if (oeeFilterRequest.getSite() != null && !oeeFilterRequest.getSite().isEmpty()) {
            try {
                AvailabilityByWorkcenterResponse response = availabilityService.getAvailabilityByWorkcenter(oeeFilterRequest);
                return ResponseEntity.ok(response);
            } catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }

    @PostMapping("/resources/daterange")
    public ResponseEntity<AvailabilityByMachineAndDateRangeResponse> getAvailabilityByResourceAndDateRange(@RequestBody OeeFilterRequest oeeFilterRequest) {
        if (oeeFilterRequest.getSite() != null && !oeeFilterRequest.getSite().isEmpty()) {
            try {
        AvailabilityByMachineAndDateRangeResponse availabilityList = availabilityService.getAvailabilityByResourceAndDateRange(oeeFilterRequest);
        return ResponseEntity.ok(availabilityList);
    }
            catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }

    @PostMapping("/workcenters/daterange")
    public ResponseEntity<AvailabilityByWorkcenterAndDateRangeResponse> getAvailabilityByWorkcenterAndDateRange(@RequestBody OeeFilterRequest oeeFilterRequest) {
        if (oeeFilterRequest.getSite() != null && !oeeFilterRequest.getSite().isEmpty()) {
            try {
                AvailabilityByWorkcenterAndDateRangeResponse availabilityList = availabilityService.getAvailabilityByWorkcenterAndDateRange(oeeFilterRequest);
                return ResponseEntity.ok(availabilityList);
            }
            catch (AvailabilityException availabilityException) {
                throw availabilityException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new AvailabilityException(1001);
    }



    @PostMapping("/availabilityByDowntime")
    public AvailabilityByDownTimeResponse getAvailabilityByDownTime(@RequestBody OeeFilterRequest request) {
        return availabilityService.getAvailabilityByDownTime(request);
    }

    @PostMapping("/downtimeHeatMap")
    public DownTimeHeatMapResponse getDownTimeHeatMap(@RequestBody OeeFilterRequest request) {
        return availabilityService.getDownTimeHeatMap(request);
    }

    @PostMapping("/getActualAvailableTime")
    public List<AggregatedAvailabilityDTO> getGroupedAvailability(@RequestBody List<CombinationRequest> combinations) {
        return availabilityService.getGroupedAvailability(combinations);
    }

    @PostMapping("/getAvailabilityRec")
    public List<OverallAvailabilityResponse> findAvailability(@RequestBody AvailabilityRequest availabilityRequest) {
        return availabilityService.findAvailability(availabilityRequest);
    }



}
