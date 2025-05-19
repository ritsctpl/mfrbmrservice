package com.rits.scheduleservice.service;

import com.rits.scheduleservice.model.EventScheduleConfig;
import com.rits.scheduleservice.model.EventScheduleOutput;
import com.rits.scheduleservice.repository.EventScheduleConfigRepository;
import com.rits.scheduleservice.repository.EventScheduleOutputRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.definition.CronDefinitionBuilder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class SchedulingServiceImpl implements SchedulingService {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private EventScheduleOutputRepository outputRepository;

    @Autowired
    private EventScheduleConfigRepository configRepository;
    private final WebClient.Builder webClientBuilder;

    public SchedulingServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public void schedule(EventScheduleConfig config) throws Exception {
        String routeId = "route-" + config.getId();

        // Remove the route if it already exists (for rescheduling or updates)
        if (camelContext.getRoute(routeId) != null) {
            camelContext.getRouteController().stopRoute(routeId);
            camelContext.removeRoute(routeId);
        }

        // Determine the URI for Quartz based on available scheduling parameters
        String quartzUri;
        if (config.getCronExpression() != null && !config.getCronExpression().isEmpty()) {
            // Use cron expression if provided
            quartzUri = "quartz://" + routeId + "?cron=" + config.getCronExpression();
        } else if (config.getEventIntervalSeconds() > 0) {
            // Use interval scheduling if cron expression is not provided
            quartzUri = "quartz://" + routeId + "?trigger.repeatInterval=" + config.getEventIntervalSeconds() * 1000 + "&trigger.repeatCount=-1";
        } else {
            // If neither cron nor a positive interval is provided, throw an exception
            throw new IllegalArgumentException("Either a valid cron expression or a positive event interval in seconds must be provided.");
        }

        // Create a new route with the specified scheduling URI
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(quartzUri)
                        .routeId(routeId)
                        .log("Running API call for: " + config.getEntityName() + " based on schedule")
                        .bean(SchedulingServiceImpl.this, "triggerApi(" + config.getId() + ")");
            }
        });

        camelContext.getRouteController().startRoute(routeId);  // Start the newly created route
    }


   /* @Override
    public void schedule(EventScheduleConfig config) throws Exception {
        String routeId = "route-" + config.getId();

        // Remove the route if it already exists (for rescheduling or updates)
        if (camelContext.getRoute(routeId) != null) {
            camelContext.getRouteController().stopRoute(routeId);
            camelContext.removeRoute(routeId);
        }

        // Dynamically create a new route for this schedule
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("quartz://" + routeId + "?trigger.repeatInterval=" + config.getEventIntervalSeconds() * 1000 + "&trigger.repeatCount=-1")
                        .routeId(routeId)
                        .log("Running API call for: " + config.getEntityName() + " at interval: " + config.getEventIntervalSeconds() + " seconds")
                        .bean(SchedulingServiceImpl.this, "triggerApi(" + config.getId() + ")");
            }
        });

        camelContext.getRouteController().startRoute(routeId);  // Start the newly created route
    }*/

    @Override
    public void updateSchedule(EventScheduleConfig config) throws Exception {
        cancelSchedule("route-" + config.getId());
        schedule(config);
    }

    @Override
    public void cancelSchedule(String routeId) throws Exception {
        if (camelContext.getRoute(routeId) != null) {
            camelContext.getRouteController().stopRoute(routeId);  // Use getRouteController to stop the route
            camelContext.removeRoute(routeId);
        }
    }

    // Logic for processing the task (triggering the API and storing output)
    /*public void triggerApi(String configId) {
        // Fetch the configuration by its ID
        Optional<EventScheduleConfig> optionalConfig = configRepository.findById(configId);
        if (optionalConfig.isPresent()) {
            EventScheduleConfig config = optionalConfig.get();

            // Simulate an API call here (using the config.getApiInput() as input to the API)
            System.out.println("Triggering API for " + config.getEntityName() + " with input: " + config.getApiInput() + " at endpoint: " + config.getApiEndpoint());

            // Example of processing API input and simulating an API call
            String apiInput = config.getApiInput();
            String apiOutput = "";// = simulateApiCall(apiInput,config.getApiEndpoint());  // Simulate the API output
            boolean isError = false;
            try {
                // Update status to active before calling the API
                config.setStatus("active");
                configRepository.save(config);

                apiOutput = callApi(config.getApiEndpoint(), config.getApiInput());
            } catch (RuntimeException e) {
                apiOutput = "Error calling API: " + e.getMessage();
                isError = true;
                config.setStatus("failed");
            }

            // Store the output in MongoDB after job execution
            EventScheduleOutput output = new EventScheduleOutput();
            output.setScheduleId(config.getId());
            output.setApiInput(apiInput);
            output.setExecutionTime(LocalDateTime.now());
            output.setApiOutput(apiOutput);  // Store the actual API output
            output.setError(isError);
            outputRepository.save(output);  // Save the output to the MongoDB collection
            // Update nextRunTime for the next execution if the task executed successfully
            if (!isError) {
                config.setNextRunTime(calculateNextRunTime(config));
                config.setStatus("completed");
                configRepository.save(config);  // Save the updated nextRunTime to MongoDB
            }
        } else {
            System.err.println("No configuration found for ID: " + configId);
        }
    }

    private LocalDateTime calculateNextRunTime(EventScheduleConfig config) {
        if (config.getCronExpression() != null && !config.getCronExpression().isEmpty()) {
            // Use a library like cron-utils or custom parsing to calculate the next time based on cron
            return calculateCronNextRunTime(config.getCronExpression());
        } else if (config.getEventIntervalSeconds() > 0) {
            // Calculate next run time based on the interval
            return LocalDateTime.now().plusSeconds(config.getEventIntervalSeconds());
        } else {
            throw new IllegalArgumentException("Either a valid cron expression or a positive event interval in seconds must be provided.");
        }
    }*/

   /* public void triggerApi(String configId) {
        Optional<EventScheduleConfig> optionalConfig = configRepository.findById(configId);
        if (optionalConfig.isPresent()) {
            EventScheduleConfig config = optionalConfig.get();

            LocalDateTime currentRunTime = LocalDateTime.now();
            LocalDateTime lastRunTime = config.getLastRunTime();

            // Initialize lastRunTime if null (e.g., first run)
            if (lastRunTime == null) {
                lastRunTime = currentRunTime.minusSeconds(config.getEventIntervalSeconds());
            }



            // Prepare the API input
            String apiInput = config.getApiInput();

            if (config.isIncludeRunTime()) {
                // Replace placeholders with actual runtime values
                apiInput = apiInput
                        .replace("$eventStartDateTime", lastRunTime.toString())
                        .replace("$eventEndDateTime", currentRunTime.toString())
                        .replace("$eventInterval", String.valueOf(config.getEventIntervalSeconds()));
            }

            System.out.println("Triggering API for " + config.getEntityName() + " with input: " + apiInput + " at endpoint: " + config.getApiEndpoint());

            String apiOutput = "";
            boolean isError = false;
            try {
                config.setStatus("active");
                configRepository.save(config);

                apiOutput = callApi(config.getApiEndpoint(), apiInput);
            } catch (RuntimeException e) {
                apiOutput = "Error calling API: " + e.getMessage();
                isError = true;
                config.setStatus("failed");
            }

            // Store the output in MongoDB
            EventScheduleOutput output = new EventScheduleOutput();
            output.setScheduleId(config.getId());
            output.setApiInput(apiInput);
            output.setExecutionTime(currentRunTime);
            output.setApiOutput(apiOutput);
            output.setError(isError);
            outputRepository.save(output);

            // Update lastRunTime and nextRunTime for successful executions
            if (!isError) {
                config.setLastRunTime(currentRunTime);
                config.setNextRunTime(calculateNextRunTime(config));
                config.setStatus("completed");
                configRepository.save(config);
            }
        } else {
            System.err.println("No configuration found for ID: " + configId);
        }
    }
*/

    public void triggerApi(String configId) {
        Optional<EventScheduleConfig> optionalConfig = configRepository.findById(configId);
        if (optionalConfig.isPresent()) {
            EventScheduleConfig config = optionalConfig.get();

            LocalDateTime currentRunTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime lastRunTime = config.getLastRunTime();

            // Determine event interval dynamically
            long elapsedTimeSeconds;
            if (config.getCronExpression() != null && !config.getCronExpression().isEmpty()) {
                // Calculate elapsed time based on Cron schedule
                lastRunTime = calculateLastRunTimeFromCron(config.getCronExpression(), currentRunTime);
                elapsedTimeSeconds = ChronoUnit.SECONDS.between(lastRunTime, currentRunTime);
            } else {
                // Default to fixed interval in seconds
                if (lastRunTime == null) {
                    lastRunTime = currentRunTime.minusSeconds(config.getEventIntervalSeconds());
                }
                elapsedTimeSeconds = config.getEventIntervalSeconds();
            }

            // Prepare API input
            String apiInput = config.getApiInput();

            if (config.isIncludeRunTime()) {
                apiInput = apiInput
                        .replace("$eventStartDateTime", lastRunTime.toString())
                        .replace("$eventEndDateTime", currentRunTime.toString())
                        .replace("$eventInterval", String.valueOf(elapsedTimeSeconds));
            }

            System.out.println("Triggering API for " + config.getEntityName() + " with input: " + apiInput + " at endpoint: " + config.getApiEndpoint());

            String apiOutput = "";
            boolean isError = false;
            try {
                config.setStatus("active");
                configRepository.save(config);

                apiOutput = callApi(config.getApiEndpoint(), apiInput);
            } catch (RuntimeException e) {
                apiOutput = "Error calling API: " + e.getMessage();
                isError = true;
                config.setStatus("failed");
            }

            // Store output in MongoDB
            EventScheduleOutput output = new EventScheduleOutput();
            output.setScheduleId(config.getId());
            output.setApiInput(apiInput);
            output.setExecutionTime(currentRunTime);
            output.setApiOutput(apiOutput);
            output.setError(isError);
            outputRepository.save(output);

            // Update lastRunTime and nextRunTime for successful executions
            if (!isError) {
                config.setLastRunTime(currentRunTime);
                config.setNextRunTime(calculateNextRunTime(config));
                config.setStatus("completed");
                configRepository.save(config);
            }
        } else {
            System.err.println("No configuration found for ID: " + configId);
        }
    }

    private LocalDateTime calculateNextRunTime(EventScheduleConfig config) {
        if (config.getCronExpression() != null && !config.getCronExpression().isEmpty()) {
            // Calculate the next execution time based on the cron expression
            return calculateCronNextRunTime(config.getCronExpression());
        } else if (config.getEventIntervalSeconds() > 0) {
            // Calculate the next execution time based on the interval
            return LocalDateTime.now().plusSeconds(config.getEventIntervalSeconds());
        } else {
            throw new IllegalArgumentException(
                    "Invalid configuration: Either a valid cron expression or a positive event interval in seconds must be provided."
            );
        }
    }

    private LocalDateTime calculateLastRunTimeFromCron(String cronExpression, LocalDateTime currentRunTime) {
        try {
            // Use Quartz cron definition for proper parsing
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            Cron cron = parser.parse(cronExpression);
            cron.validate(); // Validate the cron format to ensure it's correct

            ExecutionTime executionTime = ExecutionTime.forCron(cron);

            // Step 1: Calculate the last execution time (this run)
            Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(ZonedDateTime.of(currentRunTime, ZoneId.systemDefault()));

            // Step 2: Calculate the execution before the last one (previous run)
            Optional<ZonedDateTime> previousExecution = lastExecution.flatMap(executionTime::lastExecution);


            return lastExecution.get().toLocalDateTime();
            /*// Use the previous execution if available, otherwise fallback to last execution
            if (previousExecution.isPresent()) {
                return previousExecution.get().toLocalDateTime();
            } else if (lastExecution.isPresent()) {
                return lastExecution.get().toLocalDateTime();
            } else {
                // Fallback: Go back one interval manually
                return currentRunTime.minusMinutes(5); // Adjust based on your default interval
            }*/
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cron expression or unable to determine last execution time", e);
        }
    }



    // Helper method to calculate the next run time for cron-based schedules
    private LocalDateTime calculateCronNextRunTime(String cronExpression) {
        try {

            // Use Quartz definition to parse the cron expression
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            Cron cron = parser.parse(cronExpression);
            cron.validate(); // Validate the cron format to ensure it's correct

            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(ZonedDateTime.now());
            return nextExecution.map(ZonedDateTime::toLocalDateTime)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid cron expression or unable to determine next execution time"));

        }
        catch (IllegalArgumentException e){
        throw e;
        }

        // Convert ZonedDateTime to LocalDateTime if next execution is available
         }

    // Simulate API call (You can replace this with actual API call logic)
    private String simulateApiCall(String apiInput, String apiEndpoint) {
        // Here, you would actually call the API using RestTemplate, WebClient, etc.
        // For the sake of this example, we'll just simulate a response based on input.
        String apiOutput = "";
        apiOutput = callApi(apiEndpoint,apiInput);
        return "Processed Output: " + apiOutput;
    }

    public String callApi(String url, String requestBody) {
        WebClient webClient = webClientBuilder.build();
      //  try {
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
      //  } catch (Exception e) {
      //      throw new RuntimeException("Error calling API at " + url, e);
      //  }
    }

}
