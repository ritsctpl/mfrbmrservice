package com.rits.mfrservice.model;
import com.rits.mfrservice.model.Mfr;
import com.rits.mfrservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private Mfr response;
    private MessageDetails message_details;
}


