package com.arenaHub.backendArena.exception;

/** Diventa un 422. Esempio: {"errore": "Il torneo è al completo"} */
public class RegolaViolataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegolaViolataException(String messaggio) {
        super(messaggio);
    }
}