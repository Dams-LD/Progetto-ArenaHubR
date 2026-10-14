package com.arenaHub.backendArena.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** GET e PUT /api/impostazioni. */
public record LimiteDto(
        @NotNull(message = "Il limite è obbligatorio")
        @Min(value = 1, message = "Il limite deve essere compreso fra 1 e 999")
        @Max(value = 999, message = "Il limite deve essere compreso fra 1 e 999")
        Integer maxTorneiAttivi) {
}