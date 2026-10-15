package com.arenaHub.backendArena.exception;

/** Diventa un 400. Esempio: {"errore": "Stato non valido"} */
public class RichiestaNonValidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RichiestaNonValidaException(String messaggio) {
        super(messaggio);
    }
}
