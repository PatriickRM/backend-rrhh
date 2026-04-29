package com.rrhh.backend.application.email;

import com.rrhh.backend.domain.model.LeaveRequest;
import com.rrhh.backend.domain.model.LeaveStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Respuesta del Jefe ─────────────────────────────────────────────────

    @Async
    @Override
    public void sendHeadResponseNotification(LeaveRequest request) {
        String employeeEmail = request.getEmployee().getEmail();
        String employeeName  = request.getEmployee().getFullName();
        boolean approved     = request.getStatus() == LeaveStatus.PENDIENTE_RRHH;

        String subject = approved
                ? "✅ Tu solicitud de permiso fue aprobada por tu jefe"
                : "❌ Tu solicitud de permiso fue rechazada";

        String body = buildHeadResponseBody(request, employeeName, approved);

        sendEmail(employeeEmail, subject, body);
    }

    // ── Respuesta de RRHH ──────────────────────────────────────────────────

    @Async
    @Override
    public void sendHRResponseNotification(LeaveRequest request) {
        String employeeEmail = request.getEmployee().getEmail();
        String employeeName  = request.getEmployee().getFullName();
        boolean approved     = request.getStatus() == LeaveStatus.APROBADO;

        String subject = approved
                ? "🎉 Tu permiso ha sido APROBADO por RRHH"
                : "❌ Tu solicitud de permiso fue rechazada por RRHH";

        String body = buildHRResponseBody(request, employeeName, approved);

        sendEmail(employeeEmail, subject, body);
    }

    // ── Recordatorio de solicitudes pendientes ────────────────────────────

    @Async
    @Override
    public void sendPendingRequestReminderToHead(
            String headEmail, String headName, int pendingCount) {

        String subject = String.format(
                "📋 Tienes %d solicitud(es) de permiso pendiente(s) de revisión", pendingCount);

        String body = String.format("""
                Hola %s,

                Tienes %d solicitud(es) de permiso de tu equipo esperando tu revisión.

                Por favor, ingresa al sistema y revisa las solicitudes pendientes
                para no demorar el proceso de aprobación.

                ──────────────────────────────────
                Sistema de RRHH
                """, headName, pendingCount);

        sendEmail(headEmail, subject, body);
    }

    // ── Métodos de construcción de cuerpo ────────────────────────────────

    private String buildHeadResponseBody(
            LeaveRequest request, String employeeName, boolean approved) {

        String leaveType  = getLeaveTypeDisplayName(request.getType().name());
        String startDate  = request.getStartDate().format(DATE_FORMATTER);
        String endDate    = request.getEndDate().format(DATE_FORMATTER);
        String headName   = request.getReviewedByHeadName() != null
                ? request.getReviewedByHeadName() : "Tu jefe";
        String comment    = request.getHeadComment() != null
                ? request.getHeadComment() : "(Sin comentarios adicionales)";

        if (approved) {
            return String.format("""
                    Hola %s,

                    Tu solicitud de permiso por %s ha sido aprobada por tu jefe de departamento.

                    📅 Período: %s al %s
                    👤 Revisada por: %s
                    💬 Comentario: %s

                    Tu solicitud ahora pasa a revisión de RRHH para su aprobación final.
                    Te notificaremos cuando RRHH tome la decisión.

                    ──────────────────────────────────
                    Sistema de RRHH
                    """, employeeName, leaveType, startDate, endDate, headName, comment);
        } else {
            return String.format("""
                    Hola %s,

                    Lamentamos informarte que tu solicitud de permiso por %s
                    ha sido rechazada por tu jefe de departamento.

                    📅 Período solicitado: %s al %s
                    👤 Revisada por: %s
                    💬 Motivo: %s

                    Si tienes dudas sobre esta decisión, puedes comunicarte
                    directamente con tu jefe de departamento.

                    ──────────────────────────────────
                    Sistema de RRHH
                    """, employeeName, leaveType, startDate, endDate, headName, comment);
        }
    }

    private String buildHRResponseBody(
            LeaveRequest request, String employeeName, boolean approved) {

        String leaveType = getLeaveTypeDisplayName(request.getType().name());
        String startDate = request.getStartDate().format(DATE_FORMATTER);
        String endDate   = request.getEndDate().format(DATE_FORMATTER);
        String comment   = request.getHrComment() != null
                ? request.getHrComment() : "(Sin comentarios adicionales)";

        if (approved) {
            return String.format("""
                    Hola %s,

                    ¡Buenas noticias! Tu solicitud de permiso ha sido APROBADA oficialmente por RRHH.

                    📋 Tipo de permiso: %s
                    📅 Fechas aprobadas: %s al %s
                    💬 Comentario de RRHH: %s

                    Tu permiso está confirmado. Recuerda coordinar con tu equipo
                    antes de ausentarte.

                    ──────────────────────────────────
                    Sistema de RRHH
                    """, employeeName, leaveType, startDate, endDate, comment);
        } else {
            return String.format("""
                    Hola %s,

                    Lamentamos informarte que tu solicitud de permiso por %s
                    ha sido rechazada por el área de RRHH.

                    📅 Período solicitado: %s al %s
                    💬 Motivo: %s

                    Si tienes consultas adicionales, comunícate con el área de RRHH.

                    ──────────────────────────────────
                    Sistema de RRHH
                    """, employeeName, leaveType, startDate, endDate, comment);
        }
    }

    // ── Envío real con manejo de error no bloqueante ───────────────────────

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email enviado a: {} | Asunto: {}", to, subject);
        } catch (MailException e) {
            // No relanzar — el email es secundario al flujo principal
            log.error("Error al enviar email a '{}': {}", to, e.getMessage());
        }
    }

    private String getLeaveTypeDisplayName(String type) {
        return switch (type) {
            case "VACACIONES"           -> "Vacaciones";
            case "ENFERMEDAD"           -> "Enfermedad";
            case "MATRIMONIO"           -> "Matrimonio";
            case "FALLECIMIENTO_FAMILIAR" -> "Fallecimiento Familiar";
            case "NACIMIENTO_HIJO"      -> "Nacimiento de Hijo";
            case "MUDANZA"              -> "Mudanza";
            case "CITA_MEDICA"          -> "Cita Médica";
            default                     -> type;
        };
    }
}
