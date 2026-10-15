package com.arenaHub.backendArena.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenaHub.backendArena.entity.Impostazioni;
import com.arenaHub.backendArena.repository.ImpostazioniRepository;

@Service
public class ImpostazioniService {

    private final ImpostazioniRepository impostazioniRepository;

    public ImpostazioniService(ImpostazioniRepository impostazioniRepository) {
        this.impostazioniRepository = impostazioniRepository;
    }

    @Transactional(readOnly = true)
    public int limiteTorneiAttivi() {
        return riga().getMaxTorneiAttivi();
    }

    /**
     * Non retroattivo per costruzione: cambiamo solo il numero, nessuna
     * iscrizione esistente viene toccata. Chi è sopra il nuovo limite resta
     * iscritto e semplicemente non può aggiungerne altre.
     * Il controllo admin lo fa il controller prima di chiamare qui.
     */
    @Transactional
    public int aggiornaLimite(int nuovoLimite, UUID adminId) {
        Impostazioni imp = riga();
        imp.setMaxTorneiAttivi(nuovoLimite);
        imp.setAggiornatoIl(LocalDateTime.now());
        imp.setAggiornatoDa(adminId);
        return imp.getMaxTorneiAttivi();
    }

    private Impostazioni riga() {
        return impostazioniRepository.findById(Impostazioni.ID_UNICO)
                .orElseThrow(() -> new IllegalStateException(
                        "Manca la riga id=1 nella tabella impostazioni"));
    }
}