package com.arenaHub.backendArena.dto;

import java.util.UUID;

/**
 * GET /api/auth/me. Oltre ai dati del profilo porta il conteggio dei tornei
 * attivi e il limite corrente: così "Tornei attivi: 2 su 3" non costa
 * una seconda chiamata.
 */
public record UtenteDto(
        UUID id,
        String username,
        String email,
        String avatar,
        String ruolo,
        long torneiAttivi,
        int limiteTorneiAttivi) {
}