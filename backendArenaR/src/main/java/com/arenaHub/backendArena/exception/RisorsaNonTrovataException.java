package com.arenaHub.backendArena.exception;

/** Diventa un 404. Esempio: {"errore": "Torneo non trovato"} */
public class RisorsaNonTrovataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RisorsaNonTrovataException(String messaggio) {
        super(messaggio);
    }
}