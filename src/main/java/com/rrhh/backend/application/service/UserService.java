package com.rrhh.backend.application.service;

import com.rrhh.backend.domain.model.Position;
import com.rrhh.backend.domain.model.User;
import com.rrhh.backend.web.dto.user.UserRequestDTO;
import com.rrhh.backend.web.dto.user.UserResponseDTO;
import com.rrhh.backend.web.dto.user.UserStatusUpdateDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO dto);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO updateUserStatus(Long id, UserStatusUpdateDTO dto);
    UserResponseDTO disableUser(Long id);
    void updateUserRoleBasedOnPosition(User user, Position position);
}
