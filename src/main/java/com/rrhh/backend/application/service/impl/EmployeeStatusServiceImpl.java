package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.service.EmployeeStatusService;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.EmployeeStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmployeeStatusServiceImpl implements EmployeeStatusService {

    @Override
    public void actualizarEstadoSiContratoVencido(Employee employee) {
        if(employee.getContractEndDate() != null && employee.getContractEndDate().isBefore(LocalDate.now())
                && employee.getStatus() == EmployeeStatus.CONTRATADO){
                employee.setStatus(EmployeeStatus.FINALIZADO);
        }
    }
}
