package com.arenaHub.backendArena.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arenaHub.backendArena.dto.TorneoDto;
import com.arenaHub.backendArena.service.TorneoService;

/**
 * Gli endpoint pubblici del catalogo.
 *
 * Sono aperti a tutti, ma se il token c'è lo leggiamo lo stesso: serve a
 * riempire il campo "iscritto" e a far comparire "SEI ISCRITTO" sulle card
 * giuste. Quando il token non c'è, jwt è semplicemente null.
 */
@RestController
@RequestMapping("/api")
public class TorneoController {

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    /** GET /api/tornei?stato=&categoria=&gioco= */
    @GetMapping("/tornei")
    public List<TorneoDto> lista(
            @RequestParam(required = false) String stato,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String gioco,
            @AuthenticationPrincipal Jwt jwt) {

        return torneoService.cerca(stato, categoria, gioco, utenteDa(jwt));
    }

    /** GET /api/tornei/{id} — il pop-up "Vedi", con il regolamento. */
    @GetMapping("/tornei/{id}")
    public TorneoDto dettaglio(@PathVariable Integer id,
                               @AuthenticationPrincipal Jwt jwt) {

        return torneoService.dettaglio(id, utenteDa(jwt));
    }

    /** GET /api/categorie — riempie il menu "Ricerca per categoria". */
    @GetMapping("/categorie")
    public List<String> categorie() {
        return torneoService.categorie();
    }

    /**
     * Tira fuori l'UUID dell'utente dal token.
     *
     * Supabase lo mette nel claim "sub", che è lo standard JWT per
     * identificare il soggetto. Senza token restituiamo null e il chiamante
     * viene trattato come visitatore.
     */
    private UUID utenteDa(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            return null;
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}