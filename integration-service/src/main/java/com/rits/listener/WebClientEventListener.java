package com.rits.listener;

import com.rits.event.WebClientCallEvent;
import com.rits.kafkapojo.ProductionLogRequest;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebClientEventListener {
    @Value("${productionLog-service.uri}/save")
    private String productionLogURL;
    private final WebClient.Builder webClientBuilder;
private String  resourceBO;
private String  operation_bo;
private String  workCenterBO;
private String  shopOrderBO;
private String  pcuBO;
    @EventListener
    public void handleWebClientCallEvent(WebClientCallEvent event) {
        String payload = event.getRequestPayload().toString();
        JSONObject fixedPayload = convertPayloadToJson(payload);
        Boolean created = mapToProductionLogRequest(fixedPayload);

    }
    public Boolean mapToProductionLogRequest(JSONObject fixedPayload) {
        // Extract top-level fields
        String eventType="";
        String activityId = fixedPayload.optString("activityId", "");
        String site = fixedPayload.optString("site", "");
        String eventId = fixedPayload.optString("extensionPointId", "");
        String eventData = "fixedPayload.toString()"; // Use full payload as event data if needed
        if(activityId.equalsIgnoreCase("SU580")){
            eventType="ScrapSFC";
        }else {
            eventType = fixedPayload.optString("identifier", "").replaceAll("}", "");
        }
        LocalDateTime eventDatetime = LocalDateTime.now();
        String userId = fixedPayload.optString("user", "");
        String topic = fixedPayload.optString("topic", "");

        // Extract from "result" array
        JSONArray resultArray = fixedPayload.optJSONArray("result");
        JSONArray inputArray = fixedPayload.optJSONObject("input") != null ? fixedPayload.optJSONObject("input").optJSONArray("input") : null;
// Assuming fixedPayload is the JSONObject you've already parsed.
        JSONObject inputObject = fixedPayload.optJSONObject("input");



        List<ProductionLogRequest> productionLogRequests = new ArrayList<>();

        // Process each record in the "result" array
        if (resultArray != null && resultArray.length() > 0) {
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject result = resultArray.getJSONObject(i);

                // Find the corresponding input record (if any)
                JSONObject input = null;
                if (inputArray != null && inputArray.length() > i) {
                    input = inputArray.getJSONObject(i);
                }
                else{
                    JSONObject inputObj = fixedPayload.optJSONObject("input");
                     operation_bo = inputObject.optString("operationRef");
                     pcuBO = inputObject.optString("sfcRef");
                     resourceBO = inputObject.optString("resourceRef");
                     workCenterBO = inputObject.optString("workCenterRef");
                     shopOrderBO = inputObject.optString("shopOrderRef");

                }


                // Use the result data, but fall back to input data if result is empty
                productionLogRequests.add(createProductionLogRequest(fixedPayload, result, input, eventId, eventData, eventDatetime, userId, topic));
            }
        }

        // Send each ProductionLogRequest to the external service
        for (ProductionLogRequest productionLogReq : productionLogRequests) {
            Boolean retrievedRecord = webClientBuilder.build()
                    .post()
                    .uri(productionLogURL)
                    .bodyValue(productionLogReq)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (!retrievedRecord) {
                // Handle failure for this request (if needed)
                return false;
            }
        }

        return true;
    }

    private ProductionLogRequest createProductionLogRequest(JSONObject fixedPayload, JSONObject result, JSONObject input,
                                                            String eventId, String eventData,
                                                            LocalDateTime eventDatetime, String userId,
                                                            String topic) {
        // Initialize variables for nested fields, with priority given to `result`, then `input`
        if (shopOrderBO == null || shopOrderBO.isEmpty()) {
            shopOrderBO = getValue(result, "shopOrderRef", input, "shopOrderRef");
        }
        if (operation_bo == null || operation_bo.isEmpty()) {
            operation_bo = getValue(result, "operationRef", input, "operationRef");
        }
        if (workCenterBO == null || workCenterBO.isEmpty()) {
            workCenterBO = ""; // Not available in result
        }
        if (resourceBO == null || resourceBO.isEmpty()) {
            resourceBO = getValue(result, "resourceRef", input, "resourceRef");
        }
        String routerBO = getValue(result, "routerRef", input, "routerRef");
        String itemBO = getValue(result, "itemRef", input, "itemRef");
        if (pcuBO == null || pcuBO.isEmpty()) {
            pcuBO = getValue(result, "sfcRef", input, "sfcRef");
        }
        String qty = getValue(result, "quantity", input, "quantity", "1");
        String nc = getValue(result, "rework", input, "rework", "false");
        String comments = getValue(input, "comments", ""); // Comments may only exist in input
        String reasonCode = getValue(input, "reasonCode", ""); // ReasonCode may only exist in input

        // Shift-related fields
        LocalDateTime shiftCreatedDatetime = getDateValue(result, "shiftCreatedDatetime", input, "shiftCreatedDatetime");
        LocalDateTime shiftStartTime = getDateValue(result, "shiftStartTime", input, "shiftStartTime");
        LocalDateTime shiftEndTime = getDateValue(result, "shiftEndTime", input, "shiftEndTime");
        Integer shiftAvailableTime = getIntValue(result, "shiftAvailableTime", input, "shiftAvailableTime", 0);
        Integer totalBreakHours = getIntValue(result, "totalBreakHours", input, "totalBreakHours", 0);

        // Cycle time and manufacturing fields
        Double plannedCycleTime = getDoubleValue(result, "plannedCycleTime", input, "plannedCycleTime", 0.0);
        Double actualCycleTime = getDoubleValue(result, "actualCycleTime", input, "actualCycleTime", 0.0);
        Double manufactureTime = getDoubleValue(result, "manufactureTime", input, "manufactureTime", 0.0);

        // Quantity fields
        Integer quantityStarted = getIntValue(result, "quantityStarted", input, "quantityStarted", 0);
        Integer quantityCompleted = getIntValue(result, "quantityCompleted", input, "quantityCompleted", 0);
        Integer quantityScrapped = getIntValue(result, "quantityScrapped", input, "quantityScrapped", 0);
        Integer quantityRework = getIntValue(result, "quantityRework", input, "quantityRework", 0);



        // Construct the ProductionLogRequest object
        return ProductionLogRequest.builder()
                .site(fixedPayload.optString("site", ""))
                .eventId(eventId)
                .eventData(eventData)
                .eventType(fixedPayload.optString("identifier", "").replaceAll("}", ""))
                .eventDatetime(eventDatetime)
                .userId(userId)
                .pcu(pcuBO)
                .shopOrderBO(shopOrderBO)
                .operation_bo(operation_bo)
                .workcenterId(workCenterBO)
                .resourceId(resourceBO)
                .routerBO(routerBO)
                .itemBO(itemBO)
                .dcGrp("") // Not available, default empty
                .dataField("") // Not available, default empty
                .dataValue("") // Not available, default empty
                .component("") // Not available, default empty
                .nc(nc)
                .comments(comments)
                .reasonCode(reasonCode)
                .shiftId("") // Default empty
                .shiftCreatedDatetime(shiftCreatedDatetime)
                .shiftStartTime(shiftStartTime)
                .shiftEndTime(shiftEndTime)
                .shiftAvailableTime(shiftAvailableTime)
                .totalBreakHours(totalBreakHours)
                .plannedCycleTime(plannedCycleTime)
                .actualCycleTime(actualCycleTime)
                .manufactureTime(manufactureTime)
                .quantityStarted(quantityStarted)
                .qty(qty)
                .quantityCompleted(quantityCompleted)
                .quantityScrapped(quantityScrapped)
                .quantityRework(quantityRework)
                .status("") // Default empty
                .isQualityImpact(false) // Default false
                .isPerformanceImpact(false) // Default false
                .entryTime(LocalDateTime.now()) // Current timestamp
                .instructionType("") // Default empty
                .signoffUser(userId) // Default to current user
                .Active(1) // Default to active
                .createdDatetime(LocalDateTime.now()) // Current timestamp
                .updatedDatetime(LocalDateTime.now()) // Current timestamp
                .topic(topic)
                .startDateTime(LocalDateTime.now()) // Default current timestamp
                .build();
    }

// Helper methods to handle fallbacks between `result` and `input`:

    private String getValue(JSONObject result, String resultKey, JSONObject input, String inputKey) {
        // If result and input are both null, return an empty string
        if (result == null && input == null) {
            return "";
        }

        // Try to get the value from result first; if it's null or empty, fall back to input
        String resultValue = result != null ? result.optString(resultKey, "") : "";

        // If resultValue is empty, get the value from input (if available)
        if (resultValue.isEmpty() && input != null) {
            resultValue = input.optString(inputKey, "");
        }

        return resultValue;
    }


    // For cases where only the "input" object is being checked (no fallback needed)
    private String getValue(JSONObject input, String inputKey, String defaultValue) {
        return input != null ? input.optString(inputKey, defaultValue) : defaultValue; // Return defaultValue if input is null
    }

    private String getValue(JSONObject result, String resultKey, JSONObject input, String inputKey, String defaultValue) {
        // Check if both result and input are null or empty, then return default value
        if ((result == null || result.optString(resultKey, "").isEmpty()) &&
                (input == null || input.optString(inputKey, "").isEmpty())) {
            return defaultValue; // Return default value if both are null or empty
        }

        // First try to get the value from result, if not found try from input, if both are missing return default value
        return result != null && !result.optString(resultKey, "").isEmpty()
                ? result.optString(resultKey)
                : (input != null && !input.optString(inputKey, "").isEmpty()
                ? input.optString(inputKey)
                : defaultValue);
    }


    private Integer getIntValue(JSONObject result, String resultKey, JSONObject input, String inputKey, Integer defaultValue) {
        // Check if both result and input are null or empty, return default value if so
        if ((result == null || result.optString(resultKey, "").isEmpty()) &&
                (input == null || input.optString(inputKey, "").isEmpty())) {
            return defaultValue; // Return default value if both are null or empty
        }

        // First try to get the value from result, if not found try from input, if both are missing return the default value
        return result != null && !result.optString(resultKey, "").isEmpty()
                ? result.optInt(resultKey)
                : (input != null && !input.optString(inputKey, "").isEmpty()
                ? input.optInt(inputKey)
                : defaultValue);
    }


    private Double getDoubleValue(JSONObject result, String resultKey, JSONObject input, String inputKey, Double defaultValue) {
        // Check if both result and input are null or empty, return default value if so
        if ((result == null || result.optString(resultKey, "").isEmpty()) &&
                (input == null || input.optString(inputKey, "").isEmpty())) {
            return defaultValue; // Return default value if both are null or empty
        }

        // First try to get the value from result, if not found try from input, if both are missing return the default value
        return result != null && !result.optString(resultKey, "").isEmpty()
                ? result.optDouble(resultKey)
                : (input != null && !input.optString(inputKey, "").isEmpty()
                ? input.optDouble(inputKey)
                : defaultValue);
    }


    private LocalDateTime getDateValue(JSONObject result, String resultKey, JSONObject input, String inputKey) {
        // Check if both result and input are null or empty, return null if so
        if ((result == null || result.optString(resultKey, "").isEmpty()) &&
                (input == null || input.optString(inputKey, "").isEmpty())) {
            return null; // Return null if both are null or empty
        }

        // First try to get the value from result, if not found try from input
        String resultDateStr = result != null ? result.optString(resultKey, "") : "";
        String inputDateStr = input != null ? input.optString(inputKey, "") : "";
        String dateStr = !resultDateStr.isEmpty() ? resultDateStr : inputDateStr;

        return !dateStr.isEmpty() ? LocalDateTime.parse(dateStr) : null;
    }





    // Helper method to fix the incorrect input format
    public static JSONObject convertPayloadToJson1(String inputPayload) {
        JSONObject resultJson = new JSONObject();

        // Extracting values correctly for each key
        String[] keys = {
                "activityId", "originalTransactionId", "sfcRef", "operationRef", "resourceRef",
                "itemRef", "routerRef","workCenterRef" ,"stepId", "stepSequence", "quantity", "timesProcessed",
                "rework", "user", "site", "identifier", "extensionPointId","shopOrderRef"
        };

        // Add each key-value pair to the JSONObject
        for (String key : keys) {
            String value = extractValue(inputPayload, key);
            resultJson.put(key, value);
        }

        // Manually constructing dateTime and input fields
        JSONObject dateTime = new JSONObject();
        dateTime.put("firstDayOfWeek", 1);
        dateTime.put("minimalDaysInFirstWeek", 1);
        dateTime.put("weekendOnset", 7);
        dateTime.put("weekendOnsetMillis", 0);
        dateTime.put("weekendCease", 1);
        dateTime.put("weekendCeaseMillis", 86400000);
        dateTime.put("repeatedWallTime", 0);
        dateTime.put("skippedWallTime", 0);

        JSONObject timeInstance = new JSONObject();
        timeInstance.put("gregorianCutover", -12219292800000L);
        timeInstance.put("time", 1734497331497L);
        timeInstance.put("lenient", true);

        JSONObject zone = new JSONObject();
        zone.put("transitionCount", 0);
        zone.put("typeCount", 1);
        zone.put("typeOffsets", new int[]{0, 0});
        zone.put("ID", "UTC");

        timeInstance.put("zone", zone);
        dateTime.put("timeInstance", timeInstance);
        resultJson.put("dateTime", dateTime);

        // Constructing the input array
        JSONArray inputArray = new JSONArray();
        JSONObject inputObject = new JSONObject();
        inputObject.put("sfcRef", extractValue(inputPayload, "sfcRef"));
        inputObject.put("operationRef", extractValue(inputPayload, "operationRef"));
        inputObject.put("resourceRef", extractValue(inputPayload, "resourceRef"));
        inputObject.put("itemRef", extractValue(inputPayload, "itemRef"));
        inputObject.put("shopOrderRef", extractValue(inputPayload, "shopOrderRef"));
        inputObject.put("workCenterRef", extractValue(inputPayload, "workCenterRef"));

        inputArray.put(inputObject);
        resultJson.put("input", inputArray);

        resultJson.put("extended", true);

        return resultJson;
    }

    private static String extractValue(String input, String key) {

        String pattern;

        // For itemRef, routerRef, operationRef (capture up to 3 commas)
        if (key.equals("itemRef") || key.equals("routerRef") || key.equals("operationRef") ) {
            pattern = key + "=([^,]+(?:,[^,]+){0,2})";  // Capture up to 3 comma-separated parts
        }
        // For sfcRef, resourceRef, user (capture up to 2 commas)
        else if (key.equals("sfcRef") || key.equals("resourceRef") || key.equals("user") || key.equals("shopOrderRef") || key.equals("workCenterRef")) {
            pattern = key + "=([^,]+(?:,[^,]+){0,1})";  // Capture up to 2 comma-separated parts
        }
        // For all other keys, capture only the first part
        else {
            pattern = key + "=([^,]+)";  // Capture only the first value
        }

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }
    public static JSONObject convertPayloadToJson(String inputPayload) {
        JSONObject resultJson = new JSONObject();

        // Keys to extract from the payload
        String[] keys = {
                "activityId", "retryAttempts", "originalTransactionId", "extensionPointId",
                "identifier", "user", "site"
        };

        // Extract and add key-value pairs to resultJson
        for (String key : keys) {
            String value = extractValue(inputPayload, key);
            resultJson.put(key, value);
        }

        // Process "result" array
        String resultArrayString = extractBlock(inputPayload, "result=[", "]");
        JSONArray resultArray = new JSONArray();
        if (resultArrayString == null|| resultArrayString.equalsIgnoreCase(null)) {
            resultArrayString = extractBlock(inputPayload, "result={", "}");
        }
        if (!resultArrayString.isEmpty()) {
            String[] resultEntries = splitEntries(resultArrayString);
            for (String entry : resultEntries) {
                JSONObject resultObject = new JSONObject();

                String[] subKeys = {
                        "operationRef", "resourceRef", "sfcRef", "itemRef", "shopOrderRef",
                        "routerRef", "stepId", "stepSequence", "quantity", "timesProcessed", "rework"
                };

                for (String subKey : subKeys) {
                    String subValue = extractValue(entry, subKey);
                    resultObject.put(subKey, subValue);
                }

                // Process nested "dateTime", "dateStarted", and "dateQueued" blocks
                resultObject.put("dateTime", extractNestedObject(entry, "dateTime={", "}"));
                resultObject.put("dateStarted", extractNestedObject(entry, "dateStarted={", "}"));
                resultObject.put("dateQueued", extractNestedObject(entry, "dateQueued={", "}"));

                resultArray.put(resultObject);
            }
        }
        resultJson.put("result", resultArray);

        // Process "input" object (updated to handle both array and object formats)
        JSONObject inputObject = new JSONObject();
        String inputBlock = extractBlock(inputPayload, "input={", "}");  // Adjusted to handle end properly.

        if (!inputBlock.isEmpty()) {
            // Check if "input" is an array or object by inspecting its format
            if (inputBlock.startsWith("[{")) {
                // Handle "input" as an array
                String[] inputEntries = splitEntries(inputBlock);
                JSONArray inputArray = new JSONArray();

                for (String entry : inputEntries) {
                    JSONObject entryObject = new JSONObject();
                    String[] inputKeys = {
                            "sfcRef", "operationRef", "resourceRef", "confirmed",
                            "checkWorkCenterAssigment", "workStationRef"
                    };

                    for (String inputKey : inputKeys) {
                        String inputValue = extractValue(entry, inputKey);
                        entryObject.put(inputKey, inputValue);
                    }

                    inputArray.put(entryObject);
                }
                inputObject.put("input", inputArray);
            } else {
                // Handle "input" as an object (existing logic)
                String[] inputKeys = {
                        "workstationRef", "transferLaboredUsers", "bypassLaboredOffUsers",
                        "disableProductionLogAndActivityLog", "decrementTimesProcessed",
                        "allowSignoffForNonOwner", "checkForAlreadyLaboredOn",
                        "placeInQueue", "doNotLaborOff", "administrativeSignoff","sfcRef", "operationRef", "resourceRef", "confirmed",
                        "checkWorkCenterAssigment", "workStationRef"
                };

                for (String inputKey : inputKeys) {
                    String inputValue = extractValue(inputBlock, inputKey);
                    inputObject.put(inputKey, inputValue);
                }

                // Process "sfcData" array within "input"
                String sfcDataString = extractBlock(inputBlock, "sfcData=[", "]");
                JSONArray sfcDataArray = new JSONArray();
                if (!sfcDataString.isEmpty()) {
                    String[] sfcDataEntries = splitEntries(sfcDataString);
                    for (String sfcEntry : sfcDataEntries) {
                        JSONObject sfcObject = new JSONObject();
                        String[] sfcKeys = {"sfcRef", "operationRef", "resourceRef"};
                        for (String sfcKey : sfcKeys) {
                            sfcObject.put(sfcKey, extractValue(sfcEntry, sfcKey));
                        }
                        sfcDataArray.put(sfcObject);
                    }
                }
                inputObject.put("sfcData", sfcDataArray);
            }
        }
        resultJson.put("input", inputObject);

        // Add the extended flag
        resultJson.put("extended", true);

        return resultJson;
    }
    // Extract the value of a key from the payload


    // Extract a block of text enclosed by delimiters
    private static String extractBlock(String inputPayload, String startDelimiter, String endDelimiter) {
        int start = inputPayload.indexOf(startDelimiter);
        if (start == -1) return "";  // If start delimiter is not found, return empty string.

        int bracketLevel = 0;
        int end = -1;
        boolean isInsideBlock = false;

        for (int i = start + startDelimiter.length(); i < inputPayload.length(); i++) {
            char currentChar = inputPayload.charAt(i);

            if (currentChar == '{') {
                bracketLevel++;  // Increment bracket level for every '{'
            } else if (currentChar == '}') {
                bracketLevel--;  // Decrement bracket level for every '}'
            }

            // Start capturing data when we enter the block.
            if (bracketLevel == 0 && currentChar == ']') {
                end = i;
                break;
            }
        }

        // If no closing bracket was found or end is not properly set, return empty.
        if (end == -1) return "";

        // Extract the block between the start and end delimiters.
        return inputPayload.substring(start + startDelimiter.length(), end + 1).trim();
    }



    // Extract nested objects within a block
    private static JSONObject extractNestedObject(String inputBlock, String startDelimiter, String endDelimiter) {
        String nestedBlock = extractBlock(inputBlock, startDelimiter, endDelimiter);
        if (!nestedBlock.isEmpty()) {
            return new JSONObject("{" + nestedBlock + "}");
        }
        return new JSONObject();
    }

    // Split entries by a delimiter
    private static String[] splitEntries(String block) {
        return block.split("},\\s*\\{");
    }

    // Utility to match patterns
    private static String matchPattern(String input, String pattern) {
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(input);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

  /*  public static void main(String[] args) {
        String inputPayload = "{activityId=PR520, retryAttempts=0, originalTransactionId=136736569867472f737be:193f720487b:-7d2d, result=[{operationRef=OperationBO:RITS,MOULDING,#, resourceRef=ResourceBO:RITS,DEFAULT, sfcRef=SFCBO:RITS,RITS417, itemRef=ItemBO:RITS,110200049003,A, shopOrderRef=ShopOrderBO:RITS,805009629_123, routerRef=RouterBO:RITS,MOULDING,U,A, stepId=010, stepSequence=1, quantity=1, timesProcessed=1, rework=false, dateTime={timeInstance={gregorianCutover=-12219292800000, time=1735021045782, lenient=true, zone={ID=Asia/Kolkata}}}, dateStarted={timeInstance={time=1735020777000, zone={ID=UTC}}}, dateQueued={timeInstance={time=1735020715000, zone={ID=UTC}}}}], extended=true, extensionPointId=com.sap.me.production$SignoffService#signoffSfc, input={sfcData=[{sfcRef=SFCBO:RITS,RITS417, operationRef=OperationBO:RITS,MOULDING,A, resourceRef=ResourceBO:RITS,DEFAULT}], workstationRef=WorkstationBO:RITS,O,OPERATION_DEF, transferLaboredUsers=false, bypassLaboredOffUsers=false, disableProductionLogAndActivityLog=false, decrementTimesProcessed=true, allowSignoffForNonOwner=false, checkForAlreadyLaboredOn=false, placeInQueue=true, doNotLaborOff=true, administrativeSignoff=false}, user=UserBO:RITS,SENTHIL, site=RITS, identifier=signoffSfc}";

        JSONObject jsonResult = convertPayloadToJson(inputPayload);
        System.out.println(jsonResult.toString(4));
    }*/
}
