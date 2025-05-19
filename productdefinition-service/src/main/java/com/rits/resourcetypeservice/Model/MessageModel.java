package com.rits.resourcetypeservice.Model;

import com.rits.resourcetypeservice.dto.ResourceListResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private ResourceType response;
    private MessageDetails message_details;
    private List<ResourceListResponse> availableResources;
}
