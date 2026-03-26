package com.disramfor.api.dto;

import com.disramfor.api.entity.Cliente;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
public interface IClienteMapper {

    @Mapping(source = "asesor.id", target = "asesorId")
    @Mapping(source = "asesor.username", target = "asesorNombre")
    ClienteResponseDTO toResponse(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asesor", ignore = true)
    Cliente toEntity(ClienteRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "asesor", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(ClienteRequestDTO dto, @MappingTarget Cliente entity);
}