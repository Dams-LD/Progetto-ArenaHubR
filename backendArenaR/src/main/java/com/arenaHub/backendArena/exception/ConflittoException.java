package com.arenaHub.backendArena.exception;

/** Diventa un 409. Esempio: {"errore": "Sei già iscritto a questo torneo"} */
//409: indica che una richiesta HTTP non può essere completata
//perché è in conflitto con lo stato corrente della risorsa sul server
public class ConflittoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflittoException(String messaggio) {
        super(messaggio);
    }
}