package com.julien.lotterysystem.service;


import com.julien.lotterysystem.entity.request.UserRequest;
import com.julien.lotterysystem.entity.response.UserResponse;
import jakarta.validation.Valid;

public interface UserService {
    UserResponse register(@Valid UserRequest request);
}
