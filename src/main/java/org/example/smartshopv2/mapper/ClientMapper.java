package org.example.smartshopv2.mapper;

import org.example.smartshopv2.dto.ClientResponse;
import org.example.smartshopv2.entity.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse toResponse(Client client);
}
