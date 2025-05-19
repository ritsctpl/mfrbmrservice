package com.rits.availability.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityByDownTimeResponse {
    /*private String startTime;
    private String endTime;
    private String machine;*/
    private List<DownTimeReason> downtimeReasons;

    // Getters and Setters
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownTimeReason {
        private String reason;
        private String resourceId;
        private double downtimeDuration;
        private double availabilityPercentage;

        // Getters and Setters
    }
}
