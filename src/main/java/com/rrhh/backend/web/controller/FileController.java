package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.utils.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/evidence/{fileName}")
    public ResponseEntity<Resource> getEvidenceFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFile(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}