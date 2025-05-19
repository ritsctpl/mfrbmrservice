package com.rits.worklistservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.worklistservice.dto.WorkListRequest;
import com.rits.worklistservice.dto.WorkListRequestNoBO;
import com.rits.worklistservice.dto.WorkListResponse;
import com.rits.worklistservice.exception.WorkListException;
import com.rits.worklistservice.model.WorkList;
import com.rits.worklistservice.service.WorkListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/worklist-service")
public class WorkListController {
    private final WorkListService workListService;
    private final ObjectMapper objectMapper;

    @PostMapping("getWorkList")
    public ResponseEntity<List<WorkListResponse>> getWorkList(@RequestBody WorkListRequestNoBO workListRequestNoBO) {
        WorkListRequest workListRequest = workListService.convertToWorkListRequest(workListRequestNoBO);
        List<WorkListResponse> getWorkList;
        if (workListRequest.getSite() != null && !workListRequest.getSite().isEmpty()) {

            try {
                getWorkList = workListService.getWorkList(workListRequest);
                return ResponseEntity.ok(getWorkList);
            } catch (WorkListException workListException) {
                throw workListException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new WorkListException(1);
    }


    @PostMapping("getFieldNameByCategory")

    public ResponseEntity<List<WorkList>> getFieldNameByCategory(@RequestBody WorkListRequest workListRequest) {

        List<WorkList> getFieldNameByCategory;


        try {

            getFieldNameByCategory = workListService.getFieldNameByCategory(workListRequest.getCategory());

            return ResponseEntity.ok(getFieldNameByCategory);

        } catch (WorkListException workListException) {

            throw workListException;

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }

    @PostMapping("dummy")

    public ResponseEntity<Boolean> dummy(@RequestBody WorkListRequest workListRequest) {

        boolean getFieldNameByCategory;
        try {

            getFieldNameByCategory = workListService.dummyWebCLient(workListRequest.getSite());

            return ResponseEntity.ok(getFieldNameByCategory);

        } catch (WorkListException workListException) {

            throw workListException;

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }
}
