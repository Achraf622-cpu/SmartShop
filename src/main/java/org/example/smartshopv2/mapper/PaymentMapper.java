package org.example.smartshopv2.mapper;

import org.example.smartshopv2.dto.PaymentResponse;
import org.example.smartshopv2.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toResponse(Payment payment);
}
