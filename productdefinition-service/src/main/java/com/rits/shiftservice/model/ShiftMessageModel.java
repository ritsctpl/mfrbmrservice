package com.rits.shiftservice.model;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class ShiftMessageModel {
    private Shift response;
    private MessageDetails message_details;



    public ShiftMessageModel(Shift newShift ,MessageDetails message_details) {
        this.response = newShift;
        this.message_details=message_details;
    }
}
