package com.rits.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.kafkaevent.Message;
import com.rits.kafkapojo.ProductionLogPlacedEvent;
import com.rits.kafkapojo.ProductionLogRequest;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;


@Lazy
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationHandler {
    private final ObservationRegistry observationRegistry;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
   // private final Tracer tracer; // Assuming Tracer is also needed here

    @Value("${downtime-service.url}/logDowntime")
    private String  getDownTimeServiceUrl;

    @Value("${downtime-service.url}/logDowntimeByJson")
    private String  getDownTimeServiceUrlbyjson;

    @Value("${machinestatus-service.url}/logmachineStatusByJson")
    private String  getMcStatusServiceUrlbyjson;
    @Value("${performance-service.url}/calculatePerformanceByJson")
    private String  calculatePerformancebyjson;
    @Value("${quality-service.url}/calculateQualityByJson")
    private String  calculateQualitybyjson;

    @Value("${oee-service.url}/calculateOeeByJson")
    private String  calculateOeebyjson;
/*

    public NotificationHandler(ObservationRegistry observationRegistry,ObjectMapper objectMapper) {
        this.observationRegistry = observationRegistry;
        this.objectMapper = objectMapper;
     //   this.tracer = tracer;
    }*/

    public void handle(ProductionLogPlacedEvent event) {

        Observation.createNotStarted("on-message", observationRegistry).observe(() -> {
            log.info("Got message <{}>", event);
            log.info("TraceId- {}, Received Notification for Order - {}", "Test",
                    event.getProductionLogType());
        });
    }
    public void handle(Message<?> message) {
        try {
            String eventType = message.getEventType();
            String eventJson = objectMapper.writeValueAsString(message.getObject());
            if(eventType.equalsIgnoreCase("MC_UP")||eventType.equalsIgnoreCase("MC_DOWN")){
                webClientBuilder.build()
                        .post()

                        .uri(getMcStatusServiceUrlbyjson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(eventJson)
                        .retrieve()
                        .onStatus(HttpStatus::isError, clientResponse -> {
                            // Handle different HTTP errors. You can log them or transform them into exceptions.
                            return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                System.err.println("Error response body: " + errorBody);
                                return Mono.error(new RuntimeException("API call failed with error: " + errorBody));
                            });
                        })
                        .bodyToMono(Object.class)
                        //            .doOnNext(item -> System.out.println("Item received: " + item))     // we can use this blocks if we make async and after receiving the value to exectue
                        //            .doOnError(error -> System.err.println("Error: " + error.getMessage())) // async with error handling
                        //             .doOnTerminate(() -> System.out.println("Stream completed")) //asyn on completion
                        .subscribe(
                                result -> {
                                    // Handle successful response here if needed
                                    System.out.println("Success: " + result);
                                },
                                error -> {
                                    // Handle error
                                    System.err.println("Error: " + error.getMessage());
                                },
                                () -> {
                                    // Handle completion signal if needed (optional)
                                    System.out.println("Completed");
                                }
                        );

               /* webClientBuilder.build()
                        .post()
                        .uri(getDownTimeServiceUrlbyjson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(eventJson)
                        .retrieve()
                        .onStatus(HttpStatus::isError, clientResponse -> {
                            // Handle different HTTP errors. You can log them or transform them into exceptions.
                            return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                System.err.println("Error response body: " + errorBody);
                                return Mono.error(new RuntimeException("API call failed with error: " + errorBody));
                            });
                        })
                        .bodyToMono(Object.class)
            //            .doOnNext(item -> System.out.println("Item received: " + item))     // we can use this blocks if we make async and after receiving the value to exectue
            //            .doOnError(error -> System.err.println("Error: " + error.getMessage())) // async with error handling
           //             .doOnTerminate(() -> System.out.println("Stream completed")) //asyn on completion
                        .subscribe(
                        result -> {
                            // Handle successful response here if needed
                            System.out.println("Success: " + result);
                        },
                        error -> {
                            // Handle error
                            System.err.println("Error: " + error.getMessage());
                        },
                        () -> {
                            // Handle completion signal if needed (optional)
                            System.out.println("Completed");
                        }
                );*/
                        //.block();
            }else if(eventType.equalsIgnoreCase("OeePerformance")) {
                webClientBuilder.build()
                        .post()

                        .uri(calculatePerformancebyjson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(eventJson)
                        .retrieve()
                        .onStatus(HttpStatus::isError, clientResponse -> {
                            // Handle different HTTP errors. You can log them or transform them into exceptions.
                            return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                System.err.println("Error response body: " + errorBody);
                                return Mono.error(new RuntimeException("API call failed with error: " + errorBody));
                            });
                        })
                        .bodyToMono(Object.class)
                        //            .doOnNext(item -> System.out.println("Item received: " + item))     // we can use this blocks if we make async and after receiving the value to exectue
                        //            .doOnError(error -> System.err.println("Error: " + error.getMessage())) // async with error handling
                        //             .doOnTerminate(() -> System.out.println("Stream completed")) //asyn on completion
                        .subscribe(
                                result -> {
                                    // Handle successful response here if needed
                                    System.out.println("Success: " + result);
                                },
                                error -> {
                                    // Handle error
                                    System.err.println("Error: " + error.getMessage());
                                },
                                () -> {
                                    // Handle completion signal if needed (optional)
                                    System.out.println("Completed");
                                }
                        );
            }else if(eventType.equalsIgnoreCase("OeeQuality")) {
                webClientBuilder.build()
                        .post()

                        .uri(calculateQualitybyjson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(eventJson)
                        .retrieve()
                        .onStatus(HttpStatus::isError, clientResponse -> {
                            // Handle different HTTP errors. You can log them or transform them into exceptions.
                            return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                System.err.println("Error response body: " + errorBody);
                                return Mono.error(new RuntimeException("API call failed with error: " + errorBody));
                            });
                        })
                        .bodyToMono(Object.class)
                        //            .doOnNext(item -> System.out.println("Item received: " + item))     // we can use this blocks if we make async and after receiving the value to exectue
                        //            .doOnError(error -> System.err.println("Error: " + error.getMessage())) // async with error handling
                        //             .doOnTerminate(() -> System.out.println("Stream completed")) //asyn on completion
                        .subscribe(
                                result -> {
                                    // Handle successful response here if needed
                                    System.out.println("Success: " +  result + ", "+ eventType);
                                },
                                error -> {
                                    // Handle error
                                    System.err.println("Error: " + error.getMessage());
                                },
                                () -> {
                                    // Handle completion signal if needed (optional)
                                    System.out.println("Completed");
                                }
                        );
            }else if(eventType.equalsIgnoreCase("OeeCalculation")) {
                webClientBuilder.build()
                        .post()

                        .uri(calculateOeebyjson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(eventJson)
                        .retrieve()
                        .onStatus(HttpStatus::isError, clientResponse -> {
                            // Handle different HTTP errors. You can log them or transform them into exceptions.
                            return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                System.err.println("Error response body: " + errorBody);
                                return Mono.error(new RuntimeException("API call failed with error: " + errorBody));
                            });
                        })
                        .bodyToMono(Object.class)
                        //            .doOnNext(item -> System.out.println("Item received: " + item))     // we can use this blocks if we make async and after receiving the value to exectue
                        //            .doOnError(error -> System.err.println("Error: " + error.getMessage())) // async with error handling
                        //             .doOnTerminate(() -> System.out.println("Stream completed")) //asyn on completion
                        .subscribe(
                                result -> {
                                    // Handle successful response here if needed
                                    System.out.println("Success: " +  result + ", "+ eventType);
                                },
                                error -> {
                                    // Handle error
                                    System.err.println("Error: " + error.getMessage());
                                },
                                () -> {
                                    // Handle completion signal if needed (optional)
                                    System.out.println("Completed");
                                }
                        );
            }else{
                Observation.createNotStarted("on-message", observationRegistry).observe(() -> {
                    log.info("Received message with event type: {}", eventType);
                    log.info("TraceId- {}, Received Notification for Order - {}", "Test", eventJson);
                });
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void handle(ProductionLogRequest event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            Observation.createNotStarted("on-message", observationRegistry).observe(() -> {
                log.info("Got message <{}>", event);
                log.info("TraceId- {}, Received Notification for Order - {}", "Test",
                        eventJson);
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

}
