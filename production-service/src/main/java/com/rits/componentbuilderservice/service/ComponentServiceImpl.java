package com.rits.componentbuilderservice.service;

import com.rits.componentbuilderservice.dto.ComponentRequest;
import com.rits.componentbuilderservice.dto.ComponentResponse;
import com.rits.componentbuilderservice.model.Component;
import com.rits.componentbuilderservice.model.MessageDetails;
import com.rits.componentbuilderservice.model.MessageModel;
import com.rits.componentbuilderservice.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComponentServiceImpl implements ComponentService {


    private final ComponentRepository componentRepository;

    public Component createUpdateComponentBuilder(Component component, ComponentRequest componentRequest, Boolean componentExists) {
        if (componentExists) {
            component.setSite(component.getSite());
            component.setHandle(component.getHandle());
            component.setComponentLabel(component.getComponentLabel());
            component.setDataType(componentRequest.getDataType());
            component.setUnit(componentRequest.getUnit());
            component.setDefaultValue(componentRequest.getDefaultValue());
            component.setRequired(componentRequest.getRequired());
            component.setValidation(componentRequest.getValidation());
            component.setUserId(componentRequest.getUserId());
            component.setActive(component.getActive());
            component.setCreatedDateTime(component.getCreatedDateTime());
            component.setModifiedDateTime(LocalDateTime.now());
        } else {
            component = Component.builder()
                    .site(componentRequest.getSite())
                    .handle("ComponentBO:"+ componentRequest.getSite() + "," + componentRequest.getComponentLabel())
                    .componentLabel(componentRequest.getComponentLabel())
                    .dataType(componentRequest.getDataType())
                    .unit(componentRequest.getUnit())
                    .defaultValue(componentRequest.getDefaultValue())
                    .required(componentRequest.getRequired())
                    .validation(componentRequest.getValidation())
                    .userId(componentRequest.getUserId())
                    .active(1)
                    .createdDateTime(LocalDateTime.now())
                    .build();
        }
        return component;
    }

    @Override
    public MessageModel createComponent (ComponentRequest componentRequest) throws Exception
    {
        Boolean componentExists = componentRepository.existsByHandleAndSiteAndActiveEquals("ComponentBO:"+ componentRequest.getSite() + "," + componentRequest.getComponentLabel(), componentRequest.getSite(), 1);
        if(componentExists)
        {
            throw new Exception("Component with this label already exists");
        }
        else
        {
            Component component = createUpdateComponentBuilder(null, componentRequest, false);
            return MessageModel.builder().message_details(new MessageDetails(componentRequest.getComponentLabel() + " Created SuccessFully", "S")).response(componentRepository.save(component)).build();
        }
    }

    @Override
    public MessageModel updateComponent (ComponentRequest componentRequest) throws Exception
    {
        Boolean componentExists = componentRepository.existsByHandleAndSiteAndActiveEquals("ComponentBO:"+ componentRequest.getSite() + "," + componentRequest.getComponentLabel(), componentRequest.getSite(), 1);
        if(componentExists)
        {
            Component retrievedComponent = componentRepository.findByHandleAndSiteAndActiveEquals("ComponentBO:"+ componentRequest.getSite() + "," + componentRequest.getComponentLabel(), componentRequest.getSite(), 1);
            Component component = createUpdateComponentBuilder(retrievedComponent, componentRequest, true);
            return MessageModel.builder().message_details(new MessageDetails(componentRequest.getComponentLabel() + " Updated SuccessFully", "S")).response(componentRepository.save(component)).build();
        }
        else
        {
            throw new Exception("Component with this label does not exists");
        }
    }

    @Override
    public MessageModel deleteComponent (ComponentRequest componentRequest) throws Exception
    {
        Boolean componentExists = componentRepository.existsByHandleAndSiteAndActiveEquals("ComponentBO:"+ componentRequest.getSite() + "," + componentRequest.getComponentLabel(), componentRequest.getSite(), 1);
        if(componentExists)
        {
            Component retrievedComponent = componentRepository.findByHandleAndSiteAndActiveEquals("ComponentBO:"+ componentRequest.getSite() + "," + componentRequest.getComponentLabel(), componentRequest.getSite(), 1);
            retrievedComponent.setActive(0);
            return MessageModel.builder().message_details(new MessageDetails(componentRequest.getComponentLabel() + " Deleted SuccessFully", "S")).response(componentRepository.save(retrievedComponent)).build();
        }
        else
        {
            throw new Exception("Component with this label does not exists");
        }
    }

    @Override
    public Component retrieveComponent(ComponentRequest componentRequest) throws Exception {
        Component component = componentRepository.findByHandleAndSiteAndActiveEquals("ComponentBO:"+ componentRequest.getSite() + "," + componentRequest.getComponentLabel(), componentRequest.getSite(), 1);
        if(component != null && component.getHandle() != null)
        {
            return component;
        }
        else
        {
            throw new Exception("Component with this label does not exists");
        }
    }

    @Override
    public List<ComponentResponse> retrieveAllComponent(ComponentRequest componentRequest) throws Exception
    {
        return componentRepository.findBySiteAndComponentLabelContainingIgnoreCaseAndActiveEquals(componentRequest.getSite(), componentRequest.getComponentLabel(), 1);
    }

    @Override
    public List<ComponentResponse> retrieveTop50Component(ComponentRequest componentRequest) throws Exception {
        return componentRepository.findTop50BySiteAndActiveOrderByCreatedDateTimeDesc(componentRequest.getSite(), 1);
    }


}


