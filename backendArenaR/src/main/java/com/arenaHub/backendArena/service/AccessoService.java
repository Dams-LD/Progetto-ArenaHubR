package com.arenaHub.backendArena.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenaHub.backendArena.entity.Profilo;
import com.arenaHub.backendArena.exception.RisorsaNonTrovataException;
import com.arenaHub.backendArena.repository.ProfiloRepository;

/**
 * Chi è l'utente e cosa può fare. Il ruolo si legge SEMPRE da profiles,
 * mai da quello che manda il frontend.
 */
@Service
public class AccessoService {

    private final ProfiloRepository profiloRepository;

    public AccessoService(ProfiloRepository profiloRepository) {
        this.profiloRepository = profiloRepository;
    }

    /** Token valido ma riga in profiles mancante = trigger non attivo. */
    @Transactional(readOnly = true)
    public Profilo profilo(UUID utenteId) {
        return profiloRepository.findById(utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Profilo non trovato"));
    }

    /** Lancia 403 (gestito da GlobalExceptionHandler) se l'utente non è admin. */
    @Transactional(readOnly = true)
    public void richiediAdmin(UUID utenteId) {
        if (!profilo(utenteId).isAdmin()) {
            throw new AccessDeniedException("Operazione non consentita");
        }
    }
}