package com.barbershop.features.service.exception;

import com.barbershop.common.exception.BusinessLogicException;

public class ServiceHasActiveRecordsException extends BusinessLogicException {
    
    public ServiceHasActiveRecordsException(String serviceId) {
        super("No se puede eliminar el servicio con ID " + serviceId + " porque tiene registros activos asociados (citas programadas, confirmadas o en progreso)");
    }
    
    public ServiceHasActiveRecordsException(String serviceId, String details) {
        super("No se puede eliminar el servicio con ID " + serviceId + " porque tiene registros activos asociados: " + details);
    }
}