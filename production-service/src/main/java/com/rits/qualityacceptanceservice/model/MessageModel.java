package com.rits.qualityacceptanceservice.model;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private QualityAcceptance qualityAcceptance;
    private MessageDetails message_details;
}
