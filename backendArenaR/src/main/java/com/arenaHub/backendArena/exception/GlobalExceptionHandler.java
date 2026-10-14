package com.arenaHub.backendArena.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Trasforma ogni eccezione nel formato del contratto: {"errore": "..."}.
 *
 * I 401/403 che nascono nei filtri (token mancante o non valido) non passano
 * di qui: li gestisce SecurityConfig. Qui arrivano solo gli errori lanciati
 * dentro controller e service.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // --- eccezioni nostre ---

    @ExceptionHandler(RisorsaNonTrovataException.class)
    public ResponseEntity<Map<String, String>> nonTrovata(RisorsaNonTrovataException e) {
        return errore(404, e.getMessage());
    }

    @ExceptionHandler(ConflittoException.class)
    public ResponseEntity<Map<String, String>> conflitto(ConflittoException e) {
        return errore(409, e.getMessage());
    }

    @ExceptionHandler(RegolaViolataException.class)
    public ResponseEntity<Map<String, String>> regolaViolata(RegolaViolataException e) {
        return errore(422, e.getMessage());
    }

    @ExceptionHandler(RichiestaNonValidaException.class)
    public ResponseEntity<Map<String, String>> richiestaNonValida(RichiestaNonValidaException e) {
        return errore(400, e.getMessage());
    }

    // --- errori di input generati da Spring ---

    /** @Valid fallito: restituiamo il messaggio del primo campo sbagliato. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validazione(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Dati non validi");
        return errore(400, msg);
    }

    /** JSON malformato o data in formato sbagliato. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonIllegibile(HttpMessageNotReadableException e) {
        return errore(400, "Corpo della richiesta non valido");
    }

    /** Esempio: /api/tornei/abc */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tipoSbagliato(MethodArgumentTypeMismatchException e) {
        return errore(400, "Parametro non valido: " + e.getName());
    }

    /** Esempio: /api/calendario senza ?mese= */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> parametroMancante(MissingServletRequestParameterException e) {
        return errore(400, "Parametro mancante: " + e.getParameterName());
    }

    /**
     * Rete di sicurezza per il vincolo UNIQUE (utente_id, torneo_id):
     * due click rapidi su ISCRIVITI passano entrambi il controllo nel service,
     * il secondo INSERT lo blocca il database.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integrita(DataIntegrityViolationException e) {
        log.warn("Violazione di integrità: {}", e.getMostSpecificCause().getMessage());
        return errore(409, "Operazione in conflitto con i dati esistenti");
    }

    /**
     * Va gestita esplicitamente: senza questo metodo un eventuale @PreAuthorize
     * negato finirebbe nell'handler generico qui sotto e diventerebbe un 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accessoNegato(AccessDeniedException e) {
        return errore(403, "Operazione non consentita");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> endpointInesistente(NoResourceFoundException e) {
        return errore(404, "Risorsa non trovata");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> metodoNonAmmesso(HttpRequestMethodNotSupportedException e) {
        return errore(405, "Metodo non consentito");
    }

    /** Tutto il resto: logghiamo lo stack trace, al client va un messaggio generico. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generico(Exception e) {
        log.error("Errore non gestito", e);
        return errore(500, "Errore interno del server");
    }

    private ResponseEntity<Map<String, String>> errore(int stato, String messaggio) {
        // Map.of non accetta null: un'eccezione senza messaggio farebbe un secondo errore
        String testo = (messaggio == null || messaggio.isBlank()) ? "Errore" : messaggio;
        return ResponseEntity.status(stato).body(Map.of("errore", testo));
    }
}
