package com.rrhh.backend.application.mapper.impl;

import com.rrhh.backend.application.mapper.UserMapper;
import com.rrhh.backend.domain.model.Role;
import com.rrhh.backend.domain.model.User;
import com.rrhh.backend.web.dto.user.UserRequestDTO;
import com.rrhh.backend.web.dto.user.UserResponseDTO;
import com.rrhh.backend.web.dto.user.UserStatusUpdateDTO;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserRequestDTO dto, Role role) {
        return User.builder().username(dto.getUsername())
                .fullName(dto.getFullName())
                .password(dto.getPassword())
                .roles(Set.of(role)).enabled(true)
                .build();
    }

    @Override
    public UserResponseDTO toDto(User entity) {
        return UserResponseDTO.builder().id(entity.getId())
                .userName(entity.getUsername())
                .fullName(entity.getFullName())
                .enabled(entity.getEnabled())
                .roles(entity.getRoles().stream()//Convierte a Stream<Role>
                        .map(Role::getName) // Transforma Cada Role a String
                        .collect(Collectors.toSet())) // Junta todos a un Set<String>
                .build();
    }

    @Override
    public void updateStatusFromDto(User user, UserStatusUpdateDTO dto) {
        user.setEnabled(dto.getEnabled());
    }
}
