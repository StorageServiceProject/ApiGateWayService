package com.brodep.apigatewayservice.controller;

import com.brodep.apigatewayservice.dto.response.UserResponse;
import com.brodep.apigatewayservice.mapper.UserMapper;
import com.brodep.apigatewayservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
@Tag(name = "Пользователь")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;

    @Operation(summary = "Текущий пользователь")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("me")
    public UserResponse getCurrentUser() {
        return userMapper.userToUserResponse(userService.getCurrentUser());
    }

}
