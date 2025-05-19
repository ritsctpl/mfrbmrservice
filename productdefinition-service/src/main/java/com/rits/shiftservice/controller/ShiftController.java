package com.rits.shiftservice.controller;

import com.rits.shiftservice.dto.*;
import com.rits.shiftservice.exception.ShiftException;
import com.rits.shiftservice.model.Break;
import com.rits.shiftservice.model.Shift;
import com.rits.shiftservice.model.ShiftMessageModel;
import com.rits.shiftservice.model.ShiftMongo;
import com.rits.shiftservice.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/shift-service")
public class ShiftController
{
    private final ShiftService shiftService;

    @PostMapping("/create")
    public ResponseEntity<?> createShift(@RequestBody ShiftRequest shiftRequest) throws Exception
    {


        ShiftMessageModel createShift;
        try
        {
            createShift = shiftService.createShift(shiftRequest);
            return ResponseEntity.ok(ShiftMessageModel.builder().message_details(createShift.getMessage_details()).response(createShift.getResponse()).build());
        } catch (ShiftException shiftException)
        {
            throw shiftException;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/update")
    public ResponseEntity<ShiftMessageModel> updateShift(@RequestBody ShiftRequest shiftRequest) throws Exception {
        ShiftMessageModel updateShift;

        try {
            updateShift = shiftService.updateShift(shiftRequest);
            return ResponseEntity.ok(ShiftMessageModel.builder().message_details(updateShift.getMessage_details()).response(updateShift.getResponse()).build());

        } catch (ShiftException shiftException) {
            throw shiftException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ShiftMessageModel> deleteShift(@RequestBody ShiftRequest shiftRequest) throws Exception {
        ShiftMessageModel deleteResponse;
        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                deleteResponse = shiftService.deleteShift(shiftRequest);
                return ResponseEntity.ok(deleteResponse);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }

    @PostMapping("/retrieve")
    public ResponseEntity<ShiftResponse> retrieveRouting(@RequestBody ShiftRequest shiftRequest) throws Exception {
        ShiftResponse retrieveShift;

        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                retrieveShift = shiftService.retrieveShift(shiftRequest);
                return ResponseEntity.ok(retrieveShift);

            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }


    @PostMapping("/retrieveWithVersion")
    public ResponseEntity<List<Shift>> retrieveShiftWithVersion(@RequestBody ShiftRequest shiftRequest) throws Exception {

        List<Shift> retrievedShift;

        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {
            try {
                retrievedShift = shiftService.retrieveShiftByVersion(
                        shiftRequest.getSite(),
                        shiftRequest.getShiftId(),
                        shiftRequest.getVersion()
                );
                if (retrievedShift == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                return ResponseEntity.ok(retrievedShift);

            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }

    @PostMapping("/retrieveAll")
    public ResponseEntity<ShiftResponseList> getShiftList(@RequestBody ShiftRequest shiftRequest) {
        ShiftResponseList shiftResponseList;
        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                shiftResponseList = shiftService.retrieveAll(shiftRequest);
                return ResponseEntity.ok(shiftResponseList);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }



    @PostMapping("/retrieveTop50")
    public ResponseEntity<ShiftResponseList> retrieveTop50(@RequestBody ShiftRequest shiftRequest) {
        ShiftResponseList shiftResponseList;

        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                shiftResponseList = shiftService.retrieveTop50(shiftRequest.getSite());
                return ResponseEntity.ok(shiftResponseList);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);

    }

    @PostMapping("/getBreakHours")
    public ResponseEntity<BreakMinutesList> getBreakHours(@RequestBody ShiftRequest shiftRequest) {
        BreakMinutesList shiftResponseList;
        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                shiftResponseList = shiftService.getBreakHours(shiftRequest.getSite());
                return ResponseEntity.ok(shiftResponseList);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }

    @PostMapping("/getBreakHoursTillNow")
    public ResponseEntity<BreakMinutesList> getBreakHoursTillNow(@RequestBody ShiftRequest shiftRequest) {
        BreakMinutesList shiftResponseList;
        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                shiftResponseList = shiftService.getBreakHoursTillNow(shiftRequest.getSite());
                return ResponseEntity.ok(shiftResponseList);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }
    @PostMapping("/getBreakHoursTillNowByType")
    public ResponseEntity<BreakMinutes> getBreakHoursTillNowByType(@RequestBody ShiftRequest shiftRequest) {
        BreakMinutes shiftResponseList;
        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                shiftResponseList = shiftService.getBreakHoursTillNowByType(
                        shiftRequest.getSite(),
                        shiftRequest.getShiftType(),
                        shiftRequest.getResourceId(),
                        shiftRequest.getWorkCenterId(),
                        shiftRequest.getLocalDateTime());
                return ResponseEntity.ok(shiftResponseList);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }


    @PostMapping("/getPlannedTime")
    public ResponseEntity<MinutesList> getPlannedTime(@RequestBody ShiftRequest shiftRequest) {
        MinutesList minutesList;
        if(shiftRequest.getSite()!=null && !shiftRequest.getSite().isEmpty()){
            try{
                minutesList =shiftService.getPlannedtime(shiftRequest.getSite());
                return ResponseEntity.ok(minutesList);
            }catch (ShiftException shiftException){
                throw shiftException;
            }catch (Exception e){
                throw new RuntimeException(e);

            }
        }
        throw new ShiftException(1);
    }


    @PostMapping("/getPlannedTimeTillNow")
    public ResponseEntity<MinutesList> getPlannedTimeTillNow(@RequestBody ShiftRequest shiftRequest) {
        MinutesList minutesList;
        if(shiftRequest.getSite()!=null && !shiftRequest.getSite().isEmpty()){
            try{
                minutesList =shiftService.getPlannedtimeTillNow(shiftRequest.getSite());
                return ResponseEntity.ok(minutesList);
            }catch (ShiftException shiftException){
                throw shiftException;
            }catch (Exception e){
                throw new RuntimeException(e);

            }
        }
        throw new ShiftException(1);

    }

    @PostMapping("/getPlannedTimeTillNowByType")
    public ResponseEntity<PlannedMinutes> getPlannedTimeTillNowByType(@RequestBody ShiftRequest shiftRequest) {
        PlannedMinutes shiftResponseList;
        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                shiftResponseList = shiftService.getPlannedTimeTillNowByType(
                        shiftRequest.getSite(),
                        shiftRequest.getShiftType(),
                        shiftRequest.getResourceId(),
                        shiftRequest.getWorkCenterId());
                return ResponseEntity.ok(shiftResponseList);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }
    @PostMapping("/getShiftByDates")
    public ResponseEntity<ShiftMongo> getShiftByDates(@RequestBody ShiftRequest shiftRequest) {
        ShiftMongo shiftResponse;
        if (shiftRequest.getLocalDateTime() != null) {
            try {
                shiftResponse = shiftService.getShiftByDates(shiftRequest.getLocalDateTime());
                if (shiftResponse != null) {
                    return ResponseEntity.ok(shiftResponse);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }

    @PostMapping("/getShiftBetweenDates")
    public List<ShiftIntervalWithDate> getShiftBetweenDates(@RequestBody ShiftBtwnDatesRequest shiftBtwnDatesRequest) {

        if (shiftBtwnDatesRequest.getSite() != null && !shiftBtwnDatesRequest.getSite().isEmpty()) {

            try {
                List<ShiftIntervalWithDate> shiftWithDates = shiftService.getShiftsWithDatesInRange(
                        shiftBtwnDatesRequest.getSite(),
                        shiftBtwnDatesRequest.getShiftType(),
                        shiftBtwnDatesRequest.getResourceId(),
                        shiftBtwnDatesRequest.getWorkCenterId(),
                        shiftBtwnDatesRequest.getDateStart(),
                        shiftBtwnDatesRequest.getDateEnd());
                return shiftWithDates;
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }


    @PostMapping("/getShiftIntervalsBetweenDates")
    public ResponseEntity<List<ShiftIntervalWithDate> > getShiftIntervalsBetweenDates(@RequestBody ShiftBtwnDatesRequest shiftBtwnDatesRequest) {

        if (shiftBtwnDatesRequest.getSite() != null && !shiftBtwnDatesRequest.getSite().isEmpty()) {

            try {
                List<ShiftIntervalWithDate> shiftWithDates = shiftService.getShiftsWithDatesInRanges(
                        shiftBtwnDatesRequest.getSite(),
                        shiftBtwnDatesRequest.getShiftType(),
                        shiftBtwnDatesRequest.getResourceId(),
                        shiftBtwnDatesRequest.getWorkCenterId(),
                        shiftBtwnDatesRequest.getDateStart(),
                        shiftBtwnDatesRequest.getDateEnd());
                return ResponseEntity.ok(shiftWithDates);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }

    @PostMapping("/shiftPlannedOperatingTime")
    public Long getShiftPlannedOperatingTime(@RequestBody DowntimRequestForShift request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {

            try {
                if (request.getShiftId() == null || request.getShiftId().isEmpty()) {
                    throw new ShiftException(1001);
                }
                return shiftService.getShiftPlannedOperatingTime(request.getShiftId(),request.getSite());
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }

    @PostMapping("/CurrentCompleteShiftDetails")
    public CurrentShiftDetails getCurrentShiftDetails(@RequestBody DowntimRequestForShift request){
        CurrentShiftDetails shift=new CurrentShiftDetails();
        if(request.getSite()!=null||request.getSite()!="") {
            ShiftDetailsDTO shiftdetails = shiftService.getCurrentShiftAndBreak(request.getSite());
            shift.setShiftId(shiftdetails.getShiftId());
            shift.setShiftCreatedDatetime(shiftdetails.getShiftCreatedDatetime());
            shift.setBreakStartTime(shiftdetails.getBreakStartTime());
            if(shiftdetails.getShiftId()!=null||shiftdetails.getShiftId()!=""){
                shift.setPlannedOperatingTime(shiftService.getShiftPlannedOperatingTime(shiftdetails.getHandle(),request.getSite()));
                shift.setShiftRef(shiftdetails.getHandle());
                if(request.getDowntimeEnd()!=null&&request.getDowntimeStart()!=null) {
                    shift.setBreaktime(shiftService.getBreakDuration(request.getDowntimeEnd(), request.getDowntimeStart(), List.of(shiftdetails.getShiftId()),request.getSite()));
                }
                if(request.getDate()!=null){
                    shift.setNonproduction(shiftService.getNonProduction(request.getDate()));
                }
            }
            else{
                throw new ShiftException(1001);
            }
        }
        else{
            throw new ShiftException(1);
        }
        return shift;

    }

    @PostMapping("/getShiftDetailBetweenTime")
    public List<ShiftOutput> getShifts(@RequestBody ShiftInput input) {
        return shiftService.getShifts(input);
    }

    @PostMapping("/nonProduction")
    public Long getNonProduction(@RequestBody DowntimRequestForShift request) {
        try {
            if (request.getDate()== null) {
                throw new ShiftException(1110);
            }
            return shiftService.getNonProduction(request.getDate());
        } catch (ShiftException shiftException) {
            throw shiftException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/breakDuration")
    public Long getBreakDuration(@RequestBody DowntimRequestForShift request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {

            try {
                if (request.getDowntimeEnd() == null) {
                    throw new ShiftException(1004);
                }
                if (request.getDowntimeStart() == null) {
                    throw new ShiftException(1003);
                }
                /*if (request.getShiftIds() == null || request.getShiftIds().isEmpty()) {
                    throw new ShiftException(1008);
                }*/
                return shiftService.getBreakDuration(request.getDowntimeEnd(),request.getDowntimeStart(),request.getShiftIds(),request.getSite());
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }
    @PostMapping("/breakDurationList")
    public List<Break> getBreakDurationList(@RequestBody DowntimRequestForShift request) {
        if (request.getSite() != null && !request.getSite().isEmpty()) {

            try {
                if (request.getDowntimeEnd() == null) {
                    throw new ShiftException(1004);
                }
                if (request.getDowntimeStart() == null) {
                    throw new ShiftException(1003);
                }
                /*if (request.getShiftIds() == null || request.getShiftIds().isEmpty()) {
                    throw new ShiftException(1008);
                }*/
                return shiftService.getBreakDurationList(request.getDowntimeEnd(),request.getDowntimeStart(),request.getShiftIds(),request.getSite());
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }


    @PostMapping("/availabilityPlannedOperatingTime")
    public List<AvailabilityPlannedOperatingTimeResponse> getAvailabilityPlannedOperatingTime(@RequestBody AvailabilityRequestForShift request) {
        try {
            return shiftService.getAvailabilityPlannedOperatingTime(request.getSite(),request.getShiftIds(),request.getStartDateTime(),request.getEndDateTime(),request.getDynamicBreak());
        } catch (ShiftException shiftException) {
            throw shiftException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getCurrentShiftAndBreak")
    public ShiftDetailsDTO getCurrentShiftAndBreak(@RequestBody DowntimRequestForShift request) {
        if (StringUtils.isEmpty(request.getSite()))
            throw new ShiftException(1);

        try {
            return shiftService.getCurrentShiftAndBreak(request.getSite());
        } catch (ShiftException shiftException) {
            throw shiftException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getShiftDetailsByShiftType")
    public ResponseEntity<BreakMinutes> getShiftDetailsByShiftType(@RequestBody ShiftRequest shiftRequest) {
        BreakMinutes shiftResponse;
        if (shiftRequest.getSite() != null && !shiftRequest.getSite().isEmpty()) {

            try {
                shiftResponse = shiftService.getShiftDetailsByShiftType(shiftRequest.getSite(), shiftRequest.getResourceId(), shiftRequest.getWorkCenterId());
                return ResponseEntity.ok(shiftResponse);
            } catch (ShiftException shiftException) {
                throw shiftException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ShiftException(1);
    }
    @PostMapping("/getShiftData")
    public List<Shift> getShiftData(@RequestBody ShiftBtwnDatesRequest request) {
        return shiftService.findShiftWithIntervalsAndBreaks(request.getShiftRef(),request.getSite(),request.getDateStart(),request
                .getDateEnd());
    }
    @PostMapping("/getRuntime")
    public ShiftDataResponse calculateShiftData(@RequestBody ShiftBtwnDatesRequest request) {
        return shiftService.calculateShiftData(request.getShiftRef(),request.getSite(),request.getDateStart(),request
                .getDateEnd());
    }
    @PostMapping("/getTotalPlannedProductionTime")
    public ResponseEntity<Long> getTotalPlannedProductionTime(@RequestBody ShiftBtwnDatesRequest request) {
        if (request.getSite() == null || request.getSite().isEmpty()) {
            throw new ShiftException(1);
        }

        try {
            // Step 1: Get the list of shifts in the given interval
            List<Shift> shifts = shiftService.findShiftWithIntervalsAndBreaks(
                    request.getShiftRef(),
                    request.getSite(),
                    request.getDateStart(),
                    request.getDateEnd()
            );

            long totalPlannedProductionTime = 0;

            // Step 2: Filter shifts by resource or work center if provided
            if (request.getResourceId() != null || request.getWorkCenterId() != null) {
                List<Shift> filteredShifts = shifts.stream()
                        .filter(shift ->
                                (request.getResourceId() != null && request.getResourceId().equals(shift.getResourceId())) ||
                                        (request.getWorkCenterId() != null && request.getWorkCenterId().equals(shift.getWorkCenterId()))
                        )
                        .collect(Collectors.toList());

                // If no specific type found, use general type
                if (filteredShifts.isEmpty()) {
                    filteredShifts = shifts.stream()
                            .filter(shift -> shift.getShiftType().equalsIgnoreCase("GENERAL"))
                            .collect(Collectors.toList());
                }
                shifts = filteredShifts;
            }

            // Step 3: Calculate total planned production time without breaks
            for (Shift shift : shifts) {
                long plannedOperatingTime = shiftService.getShiftPlannedOperatingTime(shift.getShiftId(), request.getSite());
                long breakDuration = shiftService.getBreakDuration(request.getDateEnd(), request.getDateStart(), List.of(shift.getShiftId()), request.getSite());

                long effectivePlannedTime = plannedOperatingTime - breakDuration;
                if (effectivePlannedTime > 0) {
                    totalPlannedProductionTime += effectivePlannedTime;
                }
            }

            return ResponseEntity.ok(totalPlannedProductionTime);
        } catch (ShiftException shiftException) {
            throw shiftException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/plannedProductionTimes")
    public ResponseEntity<Map<String, Long>> getPlannedProductionTimes(@RequestBody List<String> shiftHandles) {
        Map<String, Long> plannedProductionTimes = shiftService.getPlannedProductionTimes(shiftHandles);
        return ResponseEntity.ok(plannedProductionTimes);
    }



}
