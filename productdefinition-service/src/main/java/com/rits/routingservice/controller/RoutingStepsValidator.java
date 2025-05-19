package com.rits.routingservice.controller;
import com.rits.routingservice.dto.RoutingRequest;
import com.rits.routingservice.model.MessageDetails;
import com.rits.routingservice.model.RoutingMessageModel;
import com.rits.routingservice.model.RoutingStep;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class RoutingStepsValidator {
    RoutingMessageModel updateRouting = null;
    public ResponseEntity<RoutingMessageModel> validateOperation(RoutingRequest routingRequest) {
        List<String> operationValidationReport = new ArrayList<>();

        Set<String> uniqueOperations = new HashSet<>();

        for (RoutingStep step : routingRequest.getRoutingStepList()) {
            String operation = step.getOperation();

            if (operation == null) {
                operationValidationReport.add("Operation cannot be null.");
                continue;
            }

            if (!uniqueOperations.add(operation)) {
                operationValidationReport.add("Operation " + operation + "is already in use.");
            }
        }

        if (!operationValidationReport.isEmpty()) {
            String str = String.join(", ",operationValidationReport);
            return ResponseEntity.ok(
                    RoutingMessageModel.builder()
                            .message_details(new MessageDetails(str,"error"))
                            .build()
            );
        }

        return ResponseEntity.ok(
                RoutingMessageModel.builder()
//                        .message_details(new MessageDetails("Operation validated successfully."))
                        .build()
        );
    }



    //entryStepValidation
    public ResponseEntity<RoutingMessageModel> validateEntryStep(RoutingRequest routingRequest) {
        List<String> entryStepValidationReport = new ArrayList<>();
        boolean hasEntryStep = false;
        int entryStepCount = 0;
        int lastReportingStepCount = 0;
        int totalSteps = routingRequest.getRoutingStepList().size();

        for (RoutingStep step : routingRequest.getRoutingStepList()) {
            boolean entryStep = step.isEntryStep();
            boolean lastReportingStep = step.isLastReportingStep();//

            if (entryStep) {
                entryStepCount++;
                hasEntryStep = true;
            }
            if (lastReportingStep) {
                lastReportingStepCount++;
            }
        }

        switch (routingRequest.getSubType()) {
            case "Sequential":
                if (entryStepCount != 1)
                    entryStepValidationReport.add("There must be exactly one entryStep.");
                if (lastReportingStepCount != 1)
                    entryStepValidationReport.add("There must be exactly one lastReportingStep.");
                break;

            case "Simultaneous":
                if (!hasEntryStep) entryStepValidationReport.add("Atleast one entryStep must be true.");
                if (lastReportingStepCount != 1)
                    entryStepValidationReport.add("There must be exactly one last reporting step.");
                break;

            case "AnyOrder":
                if (entryStepCount != totalSteps)
                    entryStepValidationReport.add("All entryStep must be true.");
                if (lastReportingStepCount != 1)
                    entryStepValidationReport.add("There must be exactly one lastReportingStep.");
                break;

            default:
                entryStepValidationReport.add("Invalid subType: " + routingRequest.getSubType());
                break;
        }

        if (!entryStepValidationReport.isEmpty()) {
            return ResponseEntity.ok(
                    RoutingMessageModel.builder()
                            .message_details(new MessageDetails(String.join(", ", entryStepValidationReport),"e"))
                            .build()
            );
        }
        return ResponseEntity.ok(
                RoutingMessageModel.builder()
//                          .message_details(new MessageDetails("Validation successful!!","yes"))
//                          .response(updateRouting.getResponse())
                          .build()
        );
    }
}
