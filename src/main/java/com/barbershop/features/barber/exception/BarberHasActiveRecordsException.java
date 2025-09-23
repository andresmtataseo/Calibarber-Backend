package com.barbershop.features.barber.exception;

import com.barbershop.common.exception.BusinessLogicException;

/**
 * Excepción lanzada cuando se intenta eliminar un barbero que tiene registros activos
 * como citas programadas o disponibilidades configuradas.
 */
public class BarberHasActiveRecordsException extends BusinessLogicException {

    public BarberHasActiveRecordsException(String message) {
        super(message);
    }

    public BarberHasActiveRecordsException(String message, Throwable cause) {
        super(message, cause);
    }

    public BarberHasActiveRecordsException(String barberId, String recordType, long count) {
        super(String.format("No se puede eliminar el barbero con ID '%s' porque tiene %d %s activos", 
              barberId, count, recordType));
    }

    public BarberHasActiveRecordsException(String barberId, String recordType) {
        super(String.format("No se puede eliminar el barbero con ID '%s' porque tiene %s activos", 
              barberId, recordType));
    }
}