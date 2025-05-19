package com.rits.nonconformanceservice.controller;
import com.rits.nonconformanceservice.dto.DispositionRequest;
import com.rits.nonconformanceservice.dto.DispositionRoutings;
import com.rits.nonconformanceservice.dto.NcRequest;
import com.rits.nonconformanceservice.model.MessageDetails;
import com.rits.nonconformanceservice.model.MessageModel;
import com.rits.nonconformanceservice.model.NcData;
import com.rits.nonconformanceservice.service.NonConformanceservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/nonconformance-service")
public class NonConformanceserviceController {
    private final NonConformanceservice nonConformanceservice;
    /**
     * Endpoint to log a Non-Conformance
     * @param ncRequest Request body containing Non-Conformance details
     * @return ResponseEntity with a message indicating success or failure
     */
    @PostMapping("lognc")
    public ResponseEntity<MessageModel> logNC(@RequestBody NcRequest ncRequest) {
        MessageModel messageModel = new MessageModel();
        try {
            if (ncRequest != null) {
                MessageDetails message = new MessageDetails();
                nonConformanceservice.logNc(ncRequest);
                message.setMsg("Non Conformance Raised Successfully for the PCU");
                message.setMsg_type("Success");
                messageModel.setMessage_details(message);
            }
        } catch (Exception e) {
            // Handle exception, log or return error response
            e.printStackTrace();
        }
        return ResponseEntity.ok(messageModel);
    }

    /**
     * Retrieve Non-Conformance data based on specific parameters
     * @param ncRequest Request body containing parameters for fetching Non-Conformance data
     * @return List of Non-Conformance data
     */
    @PostMapping("getNcData")
    public List<NcData> getNcData(@RequestBody NcRequest ncRequest) {
        List<NcData> ncDataList = new ArrayList<>();
        try {
            if (ncRequest != null) {
                ncDataList = nonConformanceservice.getNcData(ncRequest.getPcuBO(), ncRequest.getOperationBO(), ncRequest.getResourceBo());
            }
        } catch (Exception e) {
            // Handle exception, log or return error response
            e.printStackTrace();
        }
        return ncDataList;
    }

    /**
     * Endpoint to close a Non-Conformance
     * @return ResponseEntity with a message indicating success or failure
     */
    @PostMapping("closeNc")
    public ResponseEntity<MessageModel> closeNc() {
        MessageModel messageModel = new MessageModel();
        try {
            // Logic to close Non-Conformance
            // Implement your closeNc functionality here

        } catch (Exception e) {
            // Handle exception, log or return error response
            e.printStackTrace();
        }
        return ResponseEntity.ok(messageModel);
    }

    /**
     * Retrieve all Non-Conformance data for a specific PCU
     * @param ncRequest Request body containing PCU details
     * @return List of Non-Conformance data for the specified PCU
     */
    @PostMapping("getAllNcByPCU")
    public List<NcData> getAllNcByPCU(@RequestBody NcRequest ncRequest) {
        List<NcData> ncDatalist = new ArrayList<>();
        try {
            if (ncRequest != null) {
                ncDatalist = nonConformanceservice.getAllNcByPCU(ncRequest.getPcuBO());
            }
        } catch (Exception e) {
            // Handle exception, log or return error response
            e.printStackTrace();
        }
        return ncDatalist;
    }

    /**
     * Update Non-Conformance status to 'Done'
     * @param dispositionRequest Request body containing disposition details
     * @return ResponseEntity with a message indicating success or failure
     */
    @PostMapping("Done")
    public ResponseEntity<MessageModel> donePcu(@RequestBody DispositionRequest dispositionRequest) {
        MessageModel messageModel = new MessageModel();
        MessageDetails message = new MessageDetails();
        try {
            if (nonConformanceservice.donePCU(dispositionRequest)) {
                message.setMsg("Disposition Successful");
            } else {
                message.setMsg("Disposition Unsuccessful");
            }
            message.setMsg_type("Success");
            messageModel.setMessage_details(message);
        } catch (Exception e) {
            // Handle exception, log or return error response
            e.printStackTrace();
        }
        return ResponseEntity.ok(messageModel);
    }

    /**
     * Retrieve Disposition Routings for provided Non-Conformance codes
     * @param ncCodeRequest List of Non-Conformance data for getting disposition routings
     * @return List of Disposition Routings
     */
    @PostMapping("getDispositionRouting")
    public List<DispositionRoutings> getDispositionRouting(@RequestBody List<NcData> ncCodeRequest) {
        List<DispositionRoutings> dispositionRoutingList = new ArrayList<>();
        try {
            dispositionRoutingList = nonConformanceservice.getDispositionRouting(ncCodeRequest);
        } catch (Exception e) {
            // Handle exception, log or return error response
            e.printStackTrace();
        }
        return dispositionRoutingList;
    }

    @PostMapping("retrieveByPcuForLogNc")
    public List<NcData> retrieveBySiteAndPcu(@RequestBody NcRequest ncRequest) {
        List<NcData> ncDataList = new ArrayList<>();
        try {
            ncDataList = nonConformanceservice.retrieveBySiteAndPcu(ncRequest.getSite(),ncRequest.getPcuBO());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ncDataList;
    }
}
