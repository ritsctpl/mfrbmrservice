package com.rits.dispositionlogservice.service;
import com.rits.dispositionlogservice.dto.DispositionLogRequest;
import com.rits.dispositionlogservice.model.DispositionLog;
import com.rits.dispositionlogservice.model.MessageModel;


public interface Dispositionlogservice {
    public MessageModel createDispositionLog(DispositionLogRequest dispositionLogRequest) throws  Exception;
    public DispositionLog getActiveRecord(DispositionLogRequest dispositionLogRequest);
}
