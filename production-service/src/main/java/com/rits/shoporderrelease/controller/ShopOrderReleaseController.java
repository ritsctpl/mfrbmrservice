package com.rits.shoporderrelease.controller;

import com.rits.shoporderrelease.dto.ReleaseRequestList;
import com.rits.shoporderrelease.exception.ShopOrderReleaseException;
import com.rits.shoporderrelease.model.SOReleaseMessageModel;
import com.rits.shoporderrelease.service.ShopOrderReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("app/v1/shoporderrelease-service")
public class ShopOrderReleaseController {
    private final ShopOrderReleaseService shopOrderReleaseService;
    private final ApplicationContext context;
    @PostMapping("/shutdown")
    public void shutdown() {
        System.out.println("Shutting down...");
        SpringApplication.exit(context, () -> 1);
    }
    @PostMapping("release")
    public SOReleaseMessageModel release(@RequestBody ReleaseRequestList releaseRequest)
    {
        try {
            return shopOrderReleaseService.multiRelease(releaseRequest.getReleaseRequest());
        } catch (ShopOrderReleaseException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

