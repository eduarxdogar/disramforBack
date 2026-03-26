package com.disramfor.api.dto;

import com.disramfor.api.entity.AutoPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface IAutoPartMapper {

    @Mapping(target = "imageUrl", expression = "java(mapImageUrl(entity))")
    AutoPartDTO toDTO(AutoPart entity);

    AutoPart toEntity(AutoPartDTO dto);

    default String mapImageUrl(AutoPart entity) {
        if (entity.getImageUrl() != null && !entity.getImageUrl().isEmpty()) {
            return entity.getImageUrl();
        }
        // Fallback or default construction: assets/img/productos/{codigo}.png
        return "assets/img/productos/" + entity.getId() + ".png";
    }
}
