package com.barbershop.features.user.exception;

import com.barbershop.common.exception.BusinessLogicException;

/**
 * Excepción lanzada cuando se intenta eliminar un usuario que tiene registros activos
 */
public class UserHasActiveRecordsException extends BusinessLogicException {
    
    public UserHasActiveRecordsException(String userId, String recordType) {
        super(String.format("No se puede eliminar el usuario con ID '%s' porque tiene %s activos", userId, recordType));
    }
    
    public UserHasActiveRecordsException(String userId, String recordType, long count) {
        super(String.format("No se puede eliminar el usuario con ID '%s' porque tiene %d %s activos", userId, count, recordType));
    }
}