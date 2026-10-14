package com.arenaHub.backendArena.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenaHub.backendArena.dto.TorneoDto;
import com.arenaHub.backendArena.entity.Torneo;
import com.arenaHub.backendArena.exception.RichiestaNonValidaException;
import com.arenaHub.backendArena.exception.RisorsaNonTrovataException;
import com.arenaHub.backendArena.repository.IscrizioneRepository;
import com.arenaHub.backendArena.repository.TorneoRepository;

@Service
public class TorneoService {

    /** Gli unici stati ammessi. Serve a rifiutare ?stato=pippo con un 400. */
    private static final Set<String> STATI_VALIDI = Set.of("aperto", "chiuso", "concluso");

    private final TorneoRepository torneoRepository;
    private final IscrizioneRepository iscrizioneRepository;

    public TorneoService(TorneoRepository torneoRepository,
                         IscrizioneRepository iscrizioneRepository) {
        this.torneoRepository = torneoRepository;
        this.iscrizioneRepository = iscrizioneRepository;
    }

    /**
     * Il catalogo.
     *
     * utenteId è null quando chiama un visitatore senza token: in quel caso
     * il campo "iscritto" resta false per tutti, come dice il contratto.
     */
    @Transactional(readOnly = true)
    public List<TorneoDto> cerca(String stato, String categoria, String gioco, UUID utenteId) {

        if (stato != null && !STATI_VALIDI.contains(stato)) {
            throw new RichiestaNonValidaException("Stato non valido");
        }

        List<Torneo> tornei = torneoRepository.cerca(stato, categoria, gioco);

        Map<Integer, Long> conteggi = conteggiIscritti();
        Set<Integer> mieiTornei = torneiDiUtente(utenteId);

        return tornei.stream()
                .map(t -> TorneoDto.perLista(
                        t.getId(),
                        t.getNome(),
                        t.getGioco(),
                        t.getCategoria(),
                        t.getDescrizione(),
                        t.getDataOra(),
                        t.getPostiMax(),
                        conteggi.getOrDefault(t.getId(), 0L),
                        t.getStato(),
                        mieiTornei.contains(t.getId())))
                .toList();
    }

    /** Il dettaglio, quello che apre il pop-up "Vedi". Qui le regole ci sono. */
    @Transactional(readOnly = true)
    public TorneoDto dettaglio(Integer id, UUID utenteId) {

        Torneo t = torneoRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Torneo non trovato"));

        boolean iscritto = utenteId != null
                && iscrizioneRepository.existsByUtenteIdAndTorneoId(utenteId, id);

        return new TorneoDto(
                t.getId(),
                t.getNome(),
                t.getGioco(),
                t.getCategoria(),
                t.getDescrizione(),
                t.getRegole(),
                t.getDataOra(),
                t.getPostiMax(),
                iscrizioneRepository.countByTorneoId(id),
                t.getStato(),
                iscritto);
    }

    /** Le categorie per il menu di ricerca. */
    @Transactional(readOnly = true)
    public List<String> categorie() {
        return torneoRepository.trovaCategorie();
    }

    // --- metodi di appoggio ---

    /** Trasforma le coppie [idTorneo, conteggio] in una mappa comoda da leggere. */
    private Map<Integer, Long> conteggiIscritti() {
        Map<Integer, Long> mappa = new HashMap<>();
        for (Object[] riga : iscrizioneRepository.contaIscrittiPerTorneo()) {
            mappa.put((Integer) riga[0], (Long) riga[1]);
        }
        return mappa;
    }

    private Set<Integer> torneiDiUtente(UUID utenteId) {
        if (utenteId == null) {
            return Set.of();
        }
        return new HashSet<>(iscrizioneRepository.trovaIdTorneiDiUtente(utenteId));
    }
}