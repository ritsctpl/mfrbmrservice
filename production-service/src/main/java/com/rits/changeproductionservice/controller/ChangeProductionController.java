package com.rits.changeproductionservice.controller;

import com.rits.changeproductionservice.service.ChangeProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/changeproduction-service")
public class ChangeProductionController {
    private final ChangeProductionService changeProductionService;
}
