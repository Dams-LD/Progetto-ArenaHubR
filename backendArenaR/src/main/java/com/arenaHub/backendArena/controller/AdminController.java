package com.arenaHub.backendArena.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenaHub.backendArena.dto.LimiteDto;
import com.arenaHub.backendArena.dto.UtenteAdminDto;
import com.arenaHub.backendArena.service.AccessoService;
import com.arenaHub.backendArena.service.ImpostazioniService;
import com.arenaHub.backendArena.service.UtenteService;

import jakarta.validation.Valid;

/** Endpoint della change request sul limite dei tornei attivi. */
@RestController
@RequestMapping("/api")
public class AdminController {

    private final AccessoService accessoService;
    private final ImpostazioniService impostazioniService;
    private final UtenteService utenteService;

    public AdminController(AccessoService accessoService,
                           ImpostazioniService impostazioniService,
                           UtenteService utenteService) {
        this.accessoService = accessoService;
        this.impostazioniService = impostazioniService;
        this.utenteService = utenteService;
    }

    /** GET /api/impostazioni: qualsiasi utente loggato. */
    @GetMapping("/impostazioni")
    public LimiteDto leggiLimite() {
        return new LimiteDto(impostazioniService.limiteTorneiAttivi());
    }

    /** PUT /api/impostazioni: solo admin, 1-999. */
    @PutMapping("/impostazioni")
    public LimiteDto aggiornaLimite(@Valid @RequestBody LimiteDto richiesta,
                                    @AuthenticationPrincipal Jwt jwt) {
        UUID adminId = Jwts.utenteId(jwt);
        accessoService.richiediAdmin(adminId);
        return new LimiteDto(impostazioniService.aggiornaLimite(richiesta.maxTorneiAttivi(), adminId));
    }

    /** GET /api/utenti: solo admin. */
    @GetMapping("/utenti")
    public List<UtenteAdminDto> utenti(@AuthenticationPrincipal Jwt jwt) {
        accessoService.richiediAdmin(Jwts.utenteId(jwt));
        return utenteService.listaPerAdmin();
    }
}