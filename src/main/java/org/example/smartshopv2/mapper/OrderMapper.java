package org.example.smartshopv2.mapper;

import org.example.smartshopv2.dto.OrderItemResponse;
import org.example.smartshopv2.dto.OrderResponse;
import org.example.smartshopv2.dto.PaymentResponse;
import org.example.smartshopv2.entity.Order;
import org.example.smartshopv2.entity.OrderItem;
import org.example.smartshopv2.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.companyName", target = "clientName")
    OrderResponse toResponse(Order order);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponse toItemResponse(OrderItem item);

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toPaymentResponse(Payment payment);
}
