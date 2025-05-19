package com.rits.componentbuilderservice.service;

import com.rits.componentbuilderservice.dto.ComponentRequest;
import com.rits.componentbuilderservice.dto.ComponentResponse;
import com.rits.componentbuilderservice.model.Component;
import com.rits.componentbuilderservice.model.MessageModel;

import java.util.List;

public interface ComponentService {
 MessageModel createComponent(ComponentRequest component) throws Exception;

 MessageModel updateComponent(ComponentRequest component) throws Exception;

 MessageModel deleteComponent(ComponentRequest component) throws Exception;

 Component retrieveComponent(ComponentRequest componentRequest) throws Exception;

 List<ComponentResponse> retrieveAllComponent(ComponentRequest componentRequest) throws Exception;

 List<ComponentResponse> retrieveTop50Component(ComponentRequest componentRequest) throws Exception;


}
