package com.rits.batchnoinwork.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private BatchNoInWork response;
    private MessageDetails message_details;
}
