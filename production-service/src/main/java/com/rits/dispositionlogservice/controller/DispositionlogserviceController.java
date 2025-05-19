package com.rits.dispositionlogservice.controller;

import com.rits.dispositionlogservice.dto.DispositionLogRequest;
import com.rits.dispositionlogservice.exception.DispositionlogserviceException;
import com.rits.dispositionlogservice.model.DispositionLog;
import com.rits.dispositionlogservice.model.MessageModel;
import com.rits.dispositionlogservice.service.Dispositionlogservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/dispositionlog-service")
public class DispositionlogserviceController {

    private final Dispositionlogservice dispositionlogservice;

    @PostMapping("create")
    public ResponseEntity<MessageModel> createDispositionLog(@RequestBody DispositionLogRequest dispositionLogRequest) throws Exception {
       MessageModel dispositionlog;

        try {
            dispositionlog = dispositionlogservice.createDispositionLog(dispositionLogRequest);
        return ResponseEntity.ok( MessageModel.builder().message_details(dispositionlog.getMessage_details()).response(dispositionlog.getResponse()).build());


        } catch (DispositionlogserviceException dispositionlogserviceException) {
            throw  dispositionlogserviceException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("findActiveRec")
    public DispositionLog getActiveRec(@RequestBody DispositionLogRequest dispositionLogRequest){
       DispositionLog dispositionLog= dispositionlogservice.getActiveRecord(dispositionLogRequest);
       return dispositionLog;
    }


}
