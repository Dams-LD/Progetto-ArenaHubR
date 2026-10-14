package com.arenaHub.backendArena.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenaHub.backendArena.dto.UtenteDto;
import com.arenaHub.backendArena.service.UtenteService;

/** Login, registrazione e logout li fa Supabase: qui resta solo "chi sono". */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtenteService utenteService;

    public AuthController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    /** GET /api/auth/me */
    @GetMapping("/me")
    public UtenteDto me(@AuthenticationPrincipal Jwt jwt) {
        return utenteService.me(Jwts.utenteId(jwt), jwt.getClaimAsString("email"));
    }
}