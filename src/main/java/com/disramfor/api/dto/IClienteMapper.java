package com.disramfor.api.dto;
import com.disramfor.api.entity.Cliente;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
public interface IClienteMapper {

    // De entidad a DTO de respuesta
    ClienteResponseDTO toResponse(Cliente cliente);

    // De DTO de petición a entidad para creación
    @Mapping(target = "id", ignore = true)  // dejamos que DB genere el id
    Cliente toEntity(ClienteRequestDTO dto);

    // Para actualización, toma datos del DTO y los copia a la entidad existente
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ClienteRequestDTO dto, @MappingTarget Cliente entity);
}
