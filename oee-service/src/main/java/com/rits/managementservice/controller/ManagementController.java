package com.rits.managementservice.controller;

import com.rits.managementservice.dto.ManageRequest;
import com.rits.managementservice.dto.MessageModel;
import com.rits.managementservice.exception.ManageException;
import com.rits.managementservice.service.ManagementService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/management-service")
public class ManagementController {
    private final ManagementService managementService;

    @PostMapping("create")
    public MessageModel create(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new ManageException(5);

        try {
            return managementService.create(manageRequest);
        } catch (ManageException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("update")
    public MessageModel update(@RequestBody ManageRequest managementRequest)
    {
        if(StringUtils.isBlank(managementRequest.getSite()))
            throw new ManageException(5);

        try {
            return managementService.update(managementRequest);
        } catch (ManageException managementException) {
            throw managementException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("delete")
    public MessageModel delete(@RequestBody ManageRequest managementRequest)
    {
        if(StringUtils.isBlank(managementRequest.getSite()))
            throw new ManageException(5);

        try {
            return managementService.delete(managementRequest);
        } catch (ManageException managementException) {
            throw managementException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveForColorScheme")
    public MessageModel retrieveForColorScheme(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new ManageException(5);

        try {
            return managementService.retrieveForColorScheme(manageRequest);
        } catch (ManageException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveForFilter")
    public MessageModel retrieveForFilter(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new ManageException(5);

        try {
            return managementService.retrieveForFilter(manageRequest);
        } catch (ManageException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    public MessageModel retrieve(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new ManageException(5);

        try {
            return managementService.retrieve(manageRequest);
        } catch (ManageException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAll")
    public MessageModel retrieveAll(@RequestBody ManageRequest manageRequest)
    {
        if(StringUtils.isBlank(manageRequest.getSite()))
            throw new ManageException(5);

        try {
            return managementService.retrieveAll(manageRequest);
        } catch (ManageException manageException) {
            throw manageException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
