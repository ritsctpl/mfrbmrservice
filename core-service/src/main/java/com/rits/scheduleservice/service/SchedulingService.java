package com.rits.scheduleservice.service;
import com.rits.scheduleservice.model.EventScheduleConfig;

public interface SchedulingService {

    /**
     * Schedules a new task based on the provided configuration.
     *
     * @param config The configuration for the event, including interval, API endpoint, and input.
     * @throws Exception if an error occurs during scheduling.
     */
    void schedule(EventScheduleConfig config) throws Exception;

    /**
     * Updates an existing schedule based on the updated configuration.
     *
     * @param config The updated configuration for the event.
     * @throws Exception if an error occurs during rescheduling.
     */
    void updateSchedule(EventScheduleConfig config) throws Exception;

    /**
     * Cancels an existing schedule based on the route ID.
     *
     * @param routeId The route ID associated with the schedule to be canceled.
     * @throws Exception if an error occurs while canceling the schedule.
     */
    void cancelSchedule(String routeId) throws Exception;

    /**
     * Triggers an API call based on the event configuration and stores the output.
     *
     * @param configId The ID of the event configuration used to fetch the event details and API input.
     */
    void triggerApi(String configId);
}
