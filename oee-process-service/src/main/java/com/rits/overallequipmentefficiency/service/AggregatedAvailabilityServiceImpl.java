package com.rits.overallequipmentefficiency.service;

import com.rits.overallequipmentefficiency.dto.AggregatedAvailabilityResponse;
import com.rits.overallequipmentefficiency.model.AggregatedAvailabilityEntity;
import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import com.rits.overallequipmentefficiency.repository.AggregatedAvailabilityRepository;
import com.rits.overallequipmentefficiency.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AggregatedAvailabilityServiceImpl implements AggregatedAvailabilityService {

   /* private final AggregatedAvailabilityRepository aggregatedAvailabilityRepository;
    private final AvailabilityRepository availabilityRepository;

    @Override
    @Transactional
    public void aggregateAvailability(String site, String shiftId, LocalDate shiftDate, String resourceId, String workcenterId) {
        boolean isValidResource = resourceId != null && !resourceId.trim().isEmpty();
        boolean isValidWorkcenter = workcenterId != null && !workcenterId.trim().isEmpty();

        if (isValidResource) {
            aggregateByCategory(site, shiftId, shiftDate, resourceId, null, "RESOURCE");
        }
        if (isValidWorkcenter) {
            aggregateByCategory(site, shiftId, shiftDate, null, workcenterId, "WORKCENTER");
        }

        aggregateByCategory(site, shiftId, shiftDate, null, null, "SHIFT");
    }

    private void aggregateByCategory(String site, String shiftId, LocalDate shiftDate, String resourceId, String workcenterId, String category) {
        List<AvailabilityEntity> availabilityRecords;

        switch (category) {
            case "SHIFT":
                availabilityRecords = availabilityRepository.findBySiteAndShiftIdAndAvailabilityDate(site, shiftId, shiftDate);
                break;
            case "RESOURCE":
                availabilityRecords = availabilityRepository.findBySiteAndShiftIdAndResourceIdAndAvailabilityDate(site, shiftId, resourceId, shiftDate);
                break;
            case "WORKCENTER":
                availabilityRecords = availabilityRepository.findBySiteAndShiftIdAndWorkcenterIdAndAvailabilityDate(site, shiftId, workcenterId, shiftDate);
                break;
            default:
                return;
        }

        if (availabilityRecords.isEmpty()) {
            return;
        }

        double totalPlannedOperatingTime = 0;
        double totalActualAvailableTime = 0;
        double totalRuntime = 0;
        double totalDowntime = 0;
        double totalShiftBreakDuration = 0;
        double totalNonProductionDuration = 0;

        for (AvailabilityEntity record : availabilityRecords) {
            totalPlannedOperatingTime += record.getPlannedOperatingTime();
            totalActualAvailableTime += record.getActualAvailableTime();
            totalRuntime += record.getRuntime();
            totalDowntime += record.getDowntime();
            totalShiftBreakDuration += record.getShiftBreakDuration();
            totalNonProductionDuration += record.getNonProductionDuration();
        }

        double averageAvailabilityPercentage = (totalActualAvailableTime / totalPlannedOperatingTime) * 100;

        Optional<AggregatedAvailabilityEntity> existingRecordOpt =
                aggregatedAvailabilityRepository.findBySiteAndShiftIdAndAggregationLevel(
                        site, shiftId, category);

        AggregatedAvailabilityEntity aggregatedAvailability;

        if (existingRecordOpt.isPresent()) {
            aggregatedAvailability = existingRecordOpt.get();
        } else {
            aggregatedAvailability = new AggregatedAvailabilityEntity();
        }

        aggregatedAvailability.setSite(site);
        aggregatedAvailability.setShiftId(shiftId);
        aggregatedAvailability.setResourceId(resourceId);
        aggregatedAvailability.setWorkcenterId(workcenterId);
        aggregatedAvailability.setAggregationLevel(category);
        aggregatedAvailability.setShiftDate(shiftDate);
        aggregatedAvailability.setTotalPlannedOperatingTime(totalPlannedOperatingTime);
        aggregatedAvailability.setTotalActualAvailableTime(totalActualAvailableTime);
        aggregatedAvailability.setTotalRuntime(totalRuntime);
        aggregatedAvailability.setTotalDowntime(totalDowntime);
        aggregatedAvailability.setTotalShiftBreakDuration(totalShiftBreakDuration);
        aggregatedAvailability.setTotalNonProductionDuration(totalNonProductionDuration);
        aggregatedAvailability.setAverageAvailabilityPercentage(averageAvailabilityPercentage);
        aggregatedAvailability.setUpdatedDatetime(LocalDateTime.now());
        aggregatedAvailability.setActive(true);

        aggregatedAvailabilityRepository.save(aggregatedAvailability);
    }

    @Override
    public AggregatedAvailabilityResponse getAggregatedAvailability(String site, String shiftId, String resourceId, String workcenterId) {
        Optional<AggregatedAvailabilityEntity> entityOpt = aggregatedAvailabilityRepository.findBySiteAndShiftIdAndResourceIdAndWorkcenterId(
                site, shiftId, resourceId, workcenterId);

        return entityOpt.map(entity -> new AggregatedAvailabilityResponse(
                entity.getSite(), entity.getShiftId(), entity.getResourceId(), entity.getWorkcenterId(), entity.getShiftDate(),
                entity.getAggregationLevel(), entity.getTotalPlannedOperatingTime(), entity.getTotalActualAvailableTime(),
                entity.getTotalRuntime(), entity.getTotalDowntime(), entity.getTotalShiftBreakDuration(),
                entity.getTotalNonProductionDuration(), entity.getAverageAvailabilityPercentage()
        )).orElse(null);
    }*/
}
