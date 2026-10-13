package com.arenaHub.backendArena.exception;

/** Diventa un 409. Esempio: {"errore": "Sei già iscritto a questo torneo"} */
public class ConflittoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflittoException(String messaggio) {
        super(messaggio);
    }
}