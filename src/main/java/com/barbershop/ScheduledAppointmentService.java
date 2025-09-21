package com.barbershop;

import com.barbershop.features.appointment.model.Appointment;
import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import com.barbershop.features.appointment.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio programado para gestionar automáticamente el estado de las citas.
 * Se ejecuta cada 30 minutos para marcar como NO_SHOW las citas que han pasado
 * su hora de finalización y estaban en estado SCHEDULED o CONFIRMED.
 */
@Service
public class ScheduledAppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledAppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Método programado que se ejecuta cada 30 minutos para verificar y actualizar
     * el estado de las citas que deberían marcarse como NO_SHOW.
     * 
     * Se ejecuta a los 0 y 30 minutos de cada hora.
     */
    @Scheduled(cron = "0 0,30 * * * *")
    @Transactional
    public void markMissedAppointmentsAsNoShow() {
        logger.info("Iniciando proceso de verificación de citas perdidas - {}", LocalDateTime.now());
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Buscar todas las citas que están SCHEDULED o CONFIRMED y cuya hora de finalización ya pasó
            List<Appointment> missedAppointments = appointmentRepository.findMissedAppointments(now);
            
            if (missedAppointments.isEmpty()) {
                logger.info("No se encontraron citas perdidas para marcar como NO_SHOW");
                return;
            }
            
            logger.info("Se encontraron {} citas perdidas para marcar como NO_SHOW", missedAppointments.size());
            
            int updatedCount = 0;
            for (Appointment appointment : missedAppointments) {
                try {
                    // Actualizar el estado a NO_SHOW
                    appointment.setStatus(AppointmentStatus.NO_SHOW);
                    appointment.setUpdatedAt(now);
                    
                    appointmentRepository.save(appointment);
                    updatedCount++;
                    
                    logger.debug("Cita {} marcada como NO_SHOW - Cliente: {}, Barbero: {}, Fecha: {}", 
                        appointment.getAppointmentId(),
                        appointment.getClientId(),
                        appointment.getBarberId(),
                        appointment.getAppointmentDatetimeStart());
                        
                } catch (Exception e) {
                    logger.error("Error al actualizar la cita {} a NO_SHOW: {}", 
                        appointment.getAppointmentId(), e.getMessage(), e);
                }
            }
            
            logger.info("Proceso completado exitosamente. {} de {} citas actualizadas a NO_SHOW", 
                updatedCount, missedAppointments.size());
                
        } catch (Exception e) {
            logger.error("Error durante el proceso de verificación de citas perdidas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Método para obtener estadísticas del último procesamiento.
     * Útil para monitoreo y debugging.
     */
    public void logAppointmentStatistics() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            long todayScheduled = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.SCHEDULED, startOfDay, endOfDay);
            long todayConfirmed = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.CONFIRMED, startOfDay, endOfDay);
            long todayNoShow = appointmentRepository.countByStatusAndDateRange(
                AppointmentStatus.NO_SHOW, startOfDay, endOfDay);
            
            logger.info("Estadísticas del día - Programadas: {}, Confirmadas: {}, No Show: {}", 
                todayScheduled, todayConfirmed, todayNoShow);
                
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de citas: {}", e.getMessage(), e);
        }
    }
}