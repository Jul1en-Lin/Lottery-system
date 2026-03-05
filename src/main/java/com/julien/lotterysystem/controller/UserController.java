package com.julien.lotterysystem.controller;

import com.julien.lotterysystem.common.constants.ControllerErrorConstants;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.entity.errorcode.ErrorCode;
import com.julien.lotterysystem.entity.request.UserRequest;
import com.julien.lotterysystem.entity.response.UserResponse;
import com.julien.lotterysystem.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        UserResponse userResponse = userService.register(request);

    }
}
