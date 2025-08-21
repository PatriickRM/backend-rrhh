package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.service.UserService;
import com.rrhh.backend.web.dto.user.UserRequestDTO;
import com.rrhh.backend.web.dto.user.UserResponseDTO;
import com.rrhh.backend.web.dto.user.UserStatusUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto){
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponseDTO> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody UserStatusUpdateDTO statusUpdateDTO){
        return ResponseEntity.ok(userService.updateUserStatus(id,statusUpdateDTO));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<UserResponseDTO> disable(@PathVariable Long id){
        return ResponseEntity.ok(userService.disableUser(id));
    }
}
