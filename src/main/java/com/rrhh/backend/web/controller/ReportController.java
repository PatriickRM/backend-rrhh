package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Tag(name = "Reportes", description = "Exportación de datos en formato Excel (.xlsx) — solo CHRO")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    @Operation(
        summary = "Exportar solicitudes de permiso a Excel",
        description = """
            Genera un archivo .xlsx con todas las solicitudes de permiso
            dentro del rango de fechas. El archivo incluye:
            - Encabezados con formato y colores
            - Estado coloreado (verde=aprobado, rojo=rechazado, amarillo=pendiente)
            - Filtros automáticos en cada columna
            - Panel de encabezados fijo al hacer scroll
            """
    )
    @ApiResponse(responseCode = "200", description = "Archivo Excel generado exitosamente")
    @GetMapping("/leave-requests/export")
    public ResponseEntity<byte[]> exportLeaveRequests(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Fecha fin (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to

    ) throws IOException {

        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Generando reporte de permisos: {} → {}", from, to);

        byte[] excelBytes = reportService.generateLeaveRequestsExcel(from, to);

        String filename = String.format("permisos_%s_%s.xlsx",
                from.format(FILE_DATE_FORMAT), to.format(FILE_DATE_FORMAT));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(excelBytes.length))
                .body(excelBytes);
    }

    @Operation(
        summary = "Exportar listado de empleados activos a Excel",
        description = "Genera un archivo .xlsx con todos los empleados en estado CONTRATADO."
    )
    @GetMapping("/employees/export")
    public ResponseEntity<byte[]> exportEmployees() throws IOException {

        log.info("Generando reporte de empleados activos");

        byte[] excelBytes = reportService.generateEmployeesExcel();

        String filename = "empleados_" + LocalDate.now().format(FILE_DATE_FORMAT) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(excelBytes.length))
                .body(excelBytes);
    }
}
