package com.arenaHub.backendArena.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo di PUT /api/tornei/{id}. */
public record AggiornaTorneoDto(
        @NotBlank(message = "Inserisci il nome del torneo")
        @Size(min = 3, max = 100, message = "Il nome deve avere fra 3 e 100 caratteri")
        String nome,

        @NotBlank(message = "Inserisci il gioco")
        @Size(min = 2, max = 50, message = "Il gioco deve avere fra 2 e 50 caratteri")
        String gioco,

        @NotBlank(message = "Seleziona una categoria")
        String categoria,

        @Size(max = 300, message = "La descrizione non può superare 300 caratteri")
        String descrizione,

        @Size(max = 2000, message = "Il regolamento non può superare 2000 caratteri")
        String regole,

        @NotNull(message = "Inserisci data e ora del torneo")
        LocalDateTime dataOra,

        @NotNull(message = "Inserisci il numero massimo di posti")
        @Min(value = 2, message = "I posti devono essere fra 2 e 128")
        @Max(value = 128, message = "I posti devono essere fra 2 e 128")
        Integer postiMax,

        @NotBlank(message = "Seleziona lo stato del torneo")
        String stato) {
}
