package com.arenaHub.backendArena.dto;

import java.time.LocalDateTime;

/** Una riga di GET /api/iscrizioni/mie. Il torneo è senza regole (null, omesso). */
public record MiaIscrizioneDto(
        Integer iscrizioneId,
        LocalDateTime dataIscrizione,
        TorneoDto torneo) {
}