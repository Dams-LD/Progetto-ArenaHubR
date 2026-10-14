package com.arenaHub.backendArena.dto;

import java.util.UUID;

/** Una riga della pagina admin "Utenti". Gli admin non compaiono (li esclude la vista). */
public record UtenteAdminDto(
        UUID id,
        String username,
        String email,
        long torneiAttivi,
        long torneiTotali,
        boolean sopraLimite) {
}