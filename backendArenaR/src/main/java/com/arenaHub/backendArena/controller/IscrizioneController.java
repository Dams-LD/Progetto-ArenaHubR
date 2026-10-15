package com.arenaHub.backendArena.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.arenaHub.backendArena.dto.IscrizioneDto;
import com.arenaHub.backendArena.dto.MiaIscrizioneDto;
import com.arenaHub.backendArena.service.IscrizioneService;

@RestController
@RequestMapping("/api")
public class IscrizioneController {

    private final IscrizioneService iscrizioneService;

    public IscrizioneController(IscrizioneService iscrizioneService) {
        this.iscrizioneService = iscrizioneService;
    }

    /** POST /api/tornei/{id}/iscrizioni: nessun corpo, l'utente arriva dal token. */
    @PostMapping("/tornei/{id}/iscrizioni")
    @ResponseStatus(HttpStatus.CREATED)
    public IscrizioneDto iscrivi(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        return iscrizioneService.iscrivi(id, Jwts.utenteId(jwt));
    }

    /** DELETE /api/tornei/{id}/iscrizioni */
    @DeleteMapping("/tornei/{id}/iscrizioni")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annulla(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        iscrizioneService.annulla(id, Jwts.utenteId(jwt));
    }

    /** GET /api/iscrizioni/mie?stato=concluso */
    @GetMapping("/iscrizioni/mie")
    public List<MiaIscrizioneDto> mie(@RequestParam(required = false) String stato,
                                      @AuthenticationPrincipal Jwt jwt) {
        return iscrizioneService.mie(Jwts.utenteId(jwt), stato);
    }
}