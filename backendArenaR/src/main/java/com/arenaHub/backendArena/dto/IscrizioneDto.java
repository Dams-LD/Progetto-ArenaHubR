package com.arenaHub.backendArena.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Risposta 201 di POST /api/tornei/{id}/iscrizioni. */
public record IscrizioneDto(
        Integer id,
        Integer torneoId,
        UUID utenteId,
        LocalDateTime dataIscrizione) {
}