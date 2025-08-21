package com.rrhh.backend.application.mapper;

import com.rrhh.backend.domain.model.Role;
import com.rrhh.backend.domain.model.User;
import com.rrhh.backend.web.dto.user.UserRequestDTO;
import com.rrhh.backend.web.dto.user.UserResponseDTO;
import com.rrhh.backend.web.dto.user.UserStatusUpdateDTO;

public interface UserMapper {
    User toEntity(UserRequestDTO dto, Role role);
    UserResponseDTO toDto(User entity);
    void updateStatusFromDto(User user, UserStatusUpdateDTO dto);
}
