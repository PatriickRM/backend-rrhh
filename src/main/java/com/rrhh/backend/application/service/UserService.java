package com.rrhh.backend.application.service;

import com.rrhh.backend.web.dto.UserRequestDTO;
import com.rrhh.backend.web.dto.UserResponseDTO;
import com.rrhh.backend.web.dto.UserStatusUpdateDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO dto);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO updateUserStatus(Long id, UserStatusUpdateDTO dto);
    UserResponseDTO disableUser(Long id);
}
