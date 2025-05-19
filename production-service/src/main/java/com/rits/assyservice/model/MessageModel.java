
package com.rits.assyservice.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MessageModel {
    private String id;
    private List<MessageDetails> messageDetails;
}