package com.rrhh.backend.application.email;

import com.rrhh.backend.domain.model.LeaveRequest;

/**
 * Servicio de notificaciones por email.
 * Se dispara automáticamente cuando el jefe o RRHH responde una solicitud.
 */
public interface EmailService {

    /**
     * Notifica al empleado que el jefe de departamento respondió su solicitud.
     * Si fue aprobada, pasa a RRHH. Si fue rechazada, el flujo termina aquí.
     */
    void sendHeadResponseNotification(LeaveRequest request);

    /**
     * Notifica al empleado que RRHH tomó la decisión final sobre su solicitud.
     * Este es el correo de resolución definitiva.
     */
    void sendHRResponseNotification(LeaveRequest request);

    /**
     * Notifica al jefe de departamento que tiene solicitudes pendientes de revisión.
     * Útil para recordatorios programados (scheduler).
     */
    void sendPendingRequestReminderToHead(String headEmail, String headName, int pendingCount);
}
