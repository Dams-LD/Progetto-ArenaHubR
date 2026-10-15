package com.arenaHub.backendArena.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Quello che esce da GET /api/tornei e GET /api/tornei/{id}.
 *
 * Non restituiamo mai l'entity direttamente, per due motivi: qui ci sono
 * campi che nella tabella non esistono (iscritti, iscritto), e se domani
 * il database cambia non vogliamo che cambi anche la forma del JSON
 * definita nel contratto.
 *
 * Il campo "regole" compare solo nel dettaglio: nella lista resta null e
 * Jackson lo omette, come dice il contratto.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TorneoDto(
        Integer id,
        String nome,
        String gioco,
        String categoria,
        String descrizione,
        String regole,
        LocalDateTime dataOra,
        Integer postiMax,
        long iscritti,
        String stato,
        boolean iscritto) {

    /** Versione per la lista: senza regolamento. */
    public static TorneoDto perLista(Integer id, String nome, String gioco, String categoria,
                                     String descrizione, LocalDateTime dataOra, Integer postiMax,
                                     long iscritti, String stato, boolean iscritto) {
        return new TorneoDto(id, nome, gioco, categoria, descrizione, null,
                             dataOra, postiMax, iscritti, stato, iscritto);
    }
}