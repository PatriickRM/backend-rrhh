package com.rrhh.backend.application.report;

import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.EmployeeStatus;
import com.rrhh.backend.domain.model.LeaveRequest;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.domain.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Reporte de solicitudes de permiso ─────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] generateLeaveRequestsExcel(LocalDate from, LocalDate to) throws IOException {
        List<LeaveRequest> requests = leaveRequestRepository
                .findApprovedByEmployeeAndDateRange(null, from, to);

        // Si el método anterior no filtra bien para el reporte total, usar findAll con filtro
        List<LeaveRequest> allRequests = leaveRequestRepository.findAllOrderByRequestDateDesc()
                .stream()
                .filter(r -> !r.getStartDate().isBefore(from) && !r.getStartDate().isAfter(to))
                .toList();

        log.info("Generando reporte Excel: {} solicitudes entre {} y {}", allRequests.size(), from, to);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Solicitudes de Permiso");

            // Estilos
            CellStyle titleStyle    = createTitleStyle(workbook);
            CellStyle headerStyle   = createHeaderStyle(workbook);
            CellStyle dateStyle     = createDateStyle(workbook);
            CellStyle approvedStyle = createStatusStyle(workbook, IndexedColors.LIGHT_GREEN);
            CellStyle rejectedStyle = createStatusStyle(workbook, IndexedColors.ROSE);
            CellStyle pendingStyle  = createStatusStyle(workbook, IndexedColors.LIGHT_YELLOW);
            CellStyle defaultStyle  = createDefaultStyle(workbook);

            // ── Fila de título ──────────────────────────────────────────────
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(String.format(
                    "Reporte de Solicitudes de Permiso — %s al %s",
                    from.format(FORMATTER), to.format(FORMATTER)));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
            titleRow.setHeightInPoints(28);

            // ── Fila de subtítulo con metadatos ─────────────────────────────
            Row metaRow = sheet.createRow(1);
            Cell metaCell = metaRow.createCell(0);
            metaCell.setCellValue(String.format(
                    "Generado el %s  |  Total de registros: %d",
                    LocalDate.now().format(FORMATTER), allRequests.size()));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

            // ── Fila vacía de separación ─────────────────────────────────────
            sheet.createRow(2);

            // ── Headers ──────────────────────────────────────────────────────
            String[] headers = {
                "ID", "Empleado", "Departamento", "Tipo de Permiso",
                "Fecha Inicio", "Fecha Fin", "Días Solicitados",
                "Estado", "Jefe Revisor", "Comentario Jefe", "Comentario RRHH"
            };

            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Filas de datos ────────────────────────────────────────────────
            int rowNum = 4;
            for (LeaveRequest req : allRequests) {
                Row row = sheet.createRow(rowNum++);

                setCellValue(row, 0, req.getId().toString(), defaultStyle);
                setCellValue(row, 1, req.getEmployee().getFullName(), defaultStyle);
                setCellValue(row, 2, req.getEmployee().getDepartment().getName(), defaultStyle);
                setCellValue(row, 3, getLeaveTypeDisplayName(req.getType().name()), defaultStyle);

                // Fechas con formato
                Cell startCell = row.createCell(4);
                startCell.setCellValue(req.getStartDate().format(FORMATTER));
                startCell.setCellStyle(dateStyle);

                Cell endCell = row.createCell(5);
                endCell.setCellValue(req.getEndDate().format(FORMATTER));
                endCell.setCellStyle(dateStyle);

                // Días (calculado)
                long days = req.getStartDate().datesUntil(req.getEndDate().plusDays(1)).count();
                setCellValue(row, 6, String.valueOf(days), defaultStyle);

                // Estado con color
                Cell statusCell = row.createCell(7);
                statusCell.setCellValue(getStatusDisplayName(req.getStatus().name()));
                statusCell.setCellStyle(switch (req.getStatus().name()) {
                    case "APROBADO"        -> approvedStyle;
                    case "RECHAZADO"       -> rejectedStyle;
                    default                -> pendingStyle;
                });

                setCellValue(row, 8,  req.getReviewedByHeadName() != null ? req.getReviewedByHeadName() : "", defaultStyle);
                setCellValue(row, 9,  req.getHeadComment() != null ? req.getHeadComment() : "", defaultStyle);
                setCellValue(row, 10, req.getHrComment()   != null ? req.getHrComment()   : "", defaultStyle);
            }

            // ── Auto-ajuste de columnas ───────────────────────────────────────
            int[] columnWidths = {8, 25, 20, 20, 13, 13, 10, 15, 20, 30, 30};
            for (int i = 0; i < columnWidths.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i] * 256);
            }

            // ── Freeze pane: headers fijos al hacer scroll ────────────────────
            sheet.createFreezePane(0, 4);

            // ── Auto filter en headers ────────────────────────────────────────
            sheet.setAutoFilter(new CellRangeAddress(3, 3, 0, headers.length - 1));

            return toBytes(workbook);
        }
    }

    // ── Reporte de empleados ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] generateEmployeesExcel() throws IOException {
        List<Employee> employees = employeeRepository.findByStatus(EmployeeStatus.CONTRATADO);

        log.info("Generando reporte Excel de empleados: {} registros", employees.size());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Empleados Activos");

            CellStyle titleStyle   = createTitleStyle(workbook);
            CellStyle headerStyle  = createHeaderStyle(workbook);
            CellStyle defaultStyle = createDefaultStyle(workbook);

            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Listado de Empleados Activos — " + LocalDate.now().format(FORMATTER));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            titleRow.setHeightInPoints(28);
            sheet.createRow(1);

            // Headers
            String[] headers = {
                "ID", "Nombre Completo", "DNI", "Email", "Teléfono",
                "Departamento", "Posición", "Fecha Ingreso", "Fin Contrato", "Salario"
            };
            Row headerRow = sheet.createRow(2);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 3;
            for (Employee emp : employees) {
                Row row = sheet.createRow(rowNum++);
                setCellValue(row, 0, emp.getId().toString(), defaultStyle);
                setCellValue(row, 1, emp.getFullName(), defaultStyle);
                setCellValue(row, 2, emp.getDni(), defaultStyle);
                setCellValue(row, 3, emp.getEmail(), defaultStyle);
                setCellValue(row, 4, emp.getPhone(), defaultStyle);
                setCellValue(row, 5, emp.getDepartment().getName(), defaultStyle);
                setCellValue(row, 6, emp.getPosition().getTitle(), defaultStyle);
                setCellValue(row, 7, emp.getHireDate().format(FORMATTER), defaultStyle);
                setCellValue(row, 8, emp.getContractEndDate().format(FORMATTER), defaultStyle);

                // Salario como número
                Cell salaryCell = row.createCell(9);
                salaryCell.setCellValue(emp.getSalary());
                CellStyle salaryStyle = workbook.createCellStyle();
                salaryStyle.cloneStyleFrom(defaultStyle);
                DataFormat format = workbook.createDataFormat();
                salaryStyle.setDataFormat(format.getFormat("#,##0.00"));
                salaryCell.setCellStyle(salaryStyle);
            }

            int[] widths = {8, 25, 10, 25, 13, 20, 20, 13, 13, 12};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);
            sheet.createFreezePane(0, 3);
            sheet.setAutoFilter(new CellRangeAddress(2, 2, 0, headers.length - 1));

            return toBytes(workbook);
        }
    }

    // ── Estilos ───────────────────────────────────────────────────────────

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createDateStyle(Workbook wb) {
        CellStyle style = createDefaultStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createStatusStyle(Workbook wb, IndexedColors color) {
        CellStyle style = createDefaultStyle(wb);
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createDefaultStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    // ── Utilidades ────────────────────────────────────────────────────────

    private void setCellValue(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private byte[] toBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    private String getLeaveTypeDisplayName(String type) {
        return switch (type) {
            case "VACACIONES"             -> "Vacaciones";
            case "ENFERMEDAD"             -> "Enfermedad";
            case "MATRIMONIO"             -> "Matrimonio";
            case "FALLECIMIENTO_FAMILIAR" -> "Fallecimiento Familiar";
            case "NACIMIENTO_HIJO"        -> "Nacimiento de Hijo";
            case "MUDANZA"                -> "Mudanza";
            case "CITA_MEDICA"            -> "Cita Médica";
            default                       -> type;
        };
    }

    private String getStatusDisplayName(String status) {
        return switch (status) {
            case "PENDIENTE_JEFE" -> "Pendiente Jefe";
            case "PENDIENTE_RRHH" -> "Pendiente RRHH";
            case "APROBADO"       -> "Aprobado";
            case "RECHAZADO"      -> "Rechazado";
            default               -> status;
        };
    }
}
