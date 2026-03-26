package com.disramfor.api.dto;

import jakarta.validation.groups.Default;

public interface IValidationGroups {

    // Grupo para cuando un ADMIN crea/actualiza (requiere asesorId)
    interface AdminAction extends Default {}

    // Grupo para cuando un ASESOR crea/actualiza (NO requiere asesorId)
    interface AsesorAction extends Default {}
}
