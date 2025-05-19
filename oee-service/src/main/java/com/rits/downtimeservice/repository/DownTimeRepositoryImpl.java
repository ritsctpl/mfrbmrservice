package com.rits.downtimeservice.repository;

import com.rits.downtimeservice.model.AggregatedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class DownTimeRepositoryImpl implements DownTimeRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DownTimeRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<AggregatedResult> aggregateByDateRange(String shiftStartDate, String shiftEndDate) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("shiftStartDate").is(shiftStartDate)
                        .and("shiftEndDate").is(shiftEndDate)),
                Aggregation.group("resourceId")
                        .sum("plannedProductionTime").as("totalPlannedProductionTime")
                        .sum("totalDowntime").as("totalDowntime")
                        .sum("operatingTime").as("totalOperatingTime")
                        .sum("mcBreakDownHours").as("totalMcBreakDownHours")
        );

        AggregationResults<AggregatedResult> result =
                mongoTemplate.aggregate(aggregation, "DOWNTIME_AVAILABILITY", AggregatedResult.class);

        return result.getMappedResults();
    }
    @Override
    public List<AggregatedResult> customAggregationForToday() {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String todayStartString = todayStart.format(formatter);
        String todayEndString = todayEnd.format(formatter);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("createdDateTime").gte(todayStartString).lte(todayEndString)
                ),
                Aggregation.group()
                        .sum("plannedProductionTime").as("totalPlannedProductionTime")
                        .sum("totalDowntime").as("totalDowntime")
                        .sum("operatingTime").as("totalOperatingTime")
                        .sum("mcBreakDownHours").as("totalMcBreakDownHours")
        );

        AggregationResults<AggregatedResult> result =
                mongoTemplate.aggregate(aggregation, "DOWNTIME_AVAILABILITY", AggregatedResult.class);

        return result.getMappedResults();
    }
}
