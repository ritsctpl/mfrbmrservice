package com.rits.pslqconnectionservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/plsqlcon-service")
public class PsqlController {

//    @PostMapping("/create")
//    public String create(UserRequest userRequest){
//        UserOperationImpl userOperation = new UserOperationImpl();
//        return userOperation.insertUser(userRequest);
//    }
//
//    //repository
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/insertUser")
//    public String insertUser(UserRequest userRequest) {
//        return userService.insertUser(userRequest);
//    }
}
