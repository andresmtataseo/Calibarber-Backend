package com.barbershop.features.payment.mapper;

import com.barbershop.features.appointment.mapper.AppointmentMapper;
import com.barbershop.features.payment.dto.PaymentResponseDto;
import com.barbershop.features.payment.dto.request.CreatePaymentRequestDto;
import com.barbershop.features.payment.model.Payment;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {AppointmentMapper.class}
)
public interface PaymentMapper {

    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    Payment toEntity(CreatePaymentRequestDto dto);

    @Mapping(target = "appointment", source = "appointment")
    PaymentResponseDto toResponseDto(Payment entity);

    @IterableMapping(elementTargetType = PaymentResponseDto.class)
    java.util.List<PaymentResponseDto> toResponseDtoList(java.util.List<Payment> entities);
}