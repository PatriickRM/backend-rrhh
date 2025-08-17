package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.UserMapper;
import com.rrhh.backend.application.service.UserService;
import com.rrhh.backend.domain.model.Role;
import com.rrhh.backend.domain.model.User;
import com.rrhh.backend.domain.repository.RoleRepository;
import com.rrhh.backend.domain.repository.UserRepository;
import com.rrhh.backend.web.dto.UserRequestDTO;
import com.rrhh.backend.web.dto.UserResponseDTO;
import com.rrhh.backend.web.dto.UserStatusUpdateDTO;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ErrorSistema("El nombre de usuario ya está en uso.");
        }
        if(dto.getPassword().length() <= 8){
            throw new ErrorSistema("La contraseña debe tener al menos 8 caracteres por seguridad!");
        }
        Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(() -> new ErrorSistema("Rol no encontrado."));
        String passwordEncode = passwordEncoder.encode(dto.getPassword());
        User entity = userMapper.toEntity(dto, role);
        entity.setPassword(passwordEncode);
        return userMapper.toDto(userRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ErrorSistema("Usuario no encontrado"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserStatus(Long id, UserStatusUpdateDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ErrorSistema("Usuario no encontrado"));
        user.setEnabled(dto.getEnabled());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDTO disableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ErrorSistema("Usuario no encontrado"));
        if(Boolean.FALSE.equals(user.getEnabled())){
            return userMapper.toDto(user);
        }
        user.setEnabled(false);
        return userMapper.toDto(userRepository.save(user));
    }
}
