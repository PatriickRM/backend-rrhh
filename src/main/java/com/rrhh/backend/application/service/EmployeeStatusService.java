package com.rrhh.backend.application.service;

import com.rrhh.backend.domain.model.Employee;

public interface EmployeeStatusService {
    void actualizarEstadoSiContratoVencido(Employee employee);
}
