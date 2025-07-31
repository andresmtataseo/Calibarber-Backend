package com.barbershop.features.appointment.mapper;

import com.barbershop.features.appointment.dto.AppointmentResponseDto;
import com.barbershop.features.appointment.dto.request.CreateAppointmentRequestDto;
import com.barbershop.features.appointment.dto.request.UpdateAppointmentRequestDto;
import com.barbershop.features.appointment.model.Appointment;
import com.barbershop.features.barber.mapper.BarberMapper;
import com.barbershop.features.service.mapper.ServiceMapper;
import com.barbershop.features.user.mapper.UserMapper;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {BarberMapper.class, UserMapper.class, ServiceMapper.class}
)
public interface AppointmentMapper {

    @Mapping(target = "appointmentId", ignore = true)
    @Mapping(target = "clientId", source = "userId")
    @Mapping(target = "appointmentDatetimeStart", source = "appointmentDateTime")
    @Mapping(target = "appointmentDatetimeEnd", ignore = true) // Se calculará en el servicio
    @Mapping(target = "priceAtBooking", source = "price")
    @Mapping(target = "barbershopId", ignore = true) // Se establecerá en el servicio
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "barber", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "payments", ignore = true)
    Appointment toEntity(CreateAppointmentRequestDto dto);

    @Mapping(target = "appointmentId", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "barberId", ignore = true)
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "barbershopId", ignore = true)
    @Mapping(target = "appointmentDatetimeStart", source = "appointmentDateTime")
    @Mapping(target = "appointmentDatetimeEnd", ignore = true) // Se calculará en el servicio si cambia la duración
    @Mapping(target = "priceAtBooking", source = "price")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "barber", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "payments", ignore = true)
    void updateEntity(@MappingTarget Appointment entity, UpdateAppointmentRequestDto dto);

    @Mapping(target = "userId", source = "clientId")
    @Mapping(target = "appointmentDateTime", source = "appointmentDatetimeStart")
    @Mapping(target = "price", source = "priceAtBooking")
    @Mapping(target = "durationMinutes", ignore = true) // Se calculará desde appointmentDatetimeEnd - appointmentDatetimeStart
    @Mapping(target = "user", source = "client")
    @Mapping(target = "barber", source = "barber")
    @Mapping(target = "service", source = "service")
    AppointmentResponseDto toResponseDto(Appointment entity);

    @IterableMapping(elementTargetType = AppointmentResponseDto.class)
    java.util.List<AppointmentResponseDto> toResponseDtoList(java.util.List<Appointment> entities);
}