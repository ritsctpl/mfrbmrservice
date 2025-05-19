package com.rits.sectionbuilderservice.model;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private SectionBuilder sectionBuilder;
    private MessageDetails message_details;
}
