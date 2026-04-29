package com.rrhh.backend.application.report;

import java.io.IOException;
import java.time.LocalDate;


public interface ReportService {

    /**
     * Genera un reporte Excel de todas las solicitudes de permiso
     * dentro del rango de fechas especificado.
     *
     * @param from  fecha de inicio del filtro (inclusive)
     * @param to    fecha de fin del filtro (inclusive)
     * @return bytes del archivo .xlsx listo para enviar como respuesta HTTP
     */
    byte[] generateLeaveRequestsExcel(LocalDate from, LocalDate to) throws IOException;

    /**
     * Genera un reporte Excel del listado de empleados activos.
     *
     * @return bytes del archivo .xlsx
     */
    byte[] generateEmployeesExcel() throws IOException;
}
