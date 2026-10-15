package com.arenaHub.backendArena.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.arenaHub.backendArena.dto.CreaTorneoDto;
import com.arenaHub.backendArena.dto.AggiornaTorneoDto;
import com.arenaHub.backendArena.dto.TorneoDto;
import com.arenaHub.backendArena.service.AccessoService;
import com.arenaHub.backendArena.service.TorneoService;

import jakarta.validation.Valid;

/**
 * Endpoint del catalogo: lettura pubblica e creazione riservata agli admin.
 *
 * Sono aperti a tutti, ma se il token c'è lo leggiamo lo stesso: serve a
 * riempire il campo "iscritto" e a far comparire "SEI ISCRITTO" sulle card
 * giuste. Quando il token non c'è, jwt è semplicemente null.
 */
@RestController
@RequestMapping("/api")
public class TorneoController {

    private final TorneoService torneoService;
    private final AccessoService accessoService;

    public TorneoController(TorneoService torneoService, AccessoService accessoService) {
        this.torneoService = torneoService;
        this.accessoService = accessoService;
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

    /** POST /api/tornei — solo admin. I nuovi tornei nascono aperti. */
    @PostMapping("/tornei")
    @ResponseStatus(HttpStatus.CREATED)
    public TorneoDto crea(@Valid @RequestBody CreaTorneoDto richiesta,
                          @AuthenticationPrincipal Jwt jwt) {
        UUID adminId = utenteDa(jwt);
        accessoService.richiediAdmin(adminId);
        return torneoService.crea(richiesta);
    }

    /** PUT /api/tornei/{id} — solo admin. */
    @PutMapping("/tornei/{id}")
    public TorneoDto aggiorna(@PathVariable Integer id,
                              @Valid @RequestBody AggiornaTorneoDto richiesta,
                              @AuthenticationPrincipal Jwt jwt) {
        UUID adminId = utenteDa(jwt);
        accessoService.richiediAdmin(adminId);
        return torneoService.aggiorna(id, richiesta, adminId);
    }

    /** DELETE /api/tornei/{id} — solo admin, solo se non ci sono iscritti. */
    @DeleteMapping("/tornei/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        accessoService.richiediAdmin(utenteDa(jwt));
        torneoService.elimina(id);
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