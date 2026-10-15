package com.arenaHub.backendArena.controller;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

/** L'UUID dell'utente sta nel claim "sub" del token Supabase. */
final class Jwts {

    private Jwts() {
    }

    /** Per gli endpoint protetti: qui il token c'è sempre (lo garantisce SecurityConfig). */
    static UUID utenteId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}