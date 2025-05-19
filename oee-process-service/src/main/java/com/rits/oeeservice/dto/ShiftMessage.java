package com.rits.oeeservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ShiftMessage {
     private String resourceId;
     private String message;
}
