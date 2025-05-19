package com.rits.usergroupservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserGroupMessageModel {
    private UserGroup response;
    private MessageDetails message_details;
}
