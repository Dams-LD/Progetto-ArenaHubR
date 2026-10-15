package com.arenaHub.backendArena.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenaHub.backendArena.dto.AggiornaTorneoDto;
import com.arenaHub.backendArena.dto.CreaTorneoDto;
import com.arenaHub.backendArena.dto.TorneoDto;
import com.arenaHub.backendArena.entity.Torneo;
import com.arenaHub.backendArena.exception.RichiestaNonValidaException;
import com.arenaHub.backendArena.exception.RegolaViolataException;
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

    /** Crea un torneo aperto dopo aver verificato che la data sia futura. */
    @Transactional
    public TorneoDto crea(CreaTorneoDto richiesta) {
        String nome = richiesta.nome().trim();
        String gioco = richiesta.gioco().trim();
        String categoria = richiesta.categoria().trim();
        if (nome.length() < 3 || nome.length() > 100) {
            throw new RichiestaNonValidaException("Il nome deve avere fra 3 e 100 caratteri");
        }
        if (gioco.length() < 2 || gioco.length() > 50) {
            throw new RichiestaNonValidaException("Il gioco deve avere fra 2 e 50 caratteri");
        }
        if (categoria.isEmpty()) {
            throw new RichiestaNonValidaException("Seleziona o inserisci una categoria");
        }
        if (!richiesta.dataOra().isAfter(LocalDateTime.now())) {
            throw new RichiestaNonValidaException("La data deve essere futura");
        }

        Torneo torneo = new Torneo();
        torneo.setNome(nome);
        torneo.setGioco(gioco);
        torneo.setCategoria(categoria);
        torneo.setDescrizione(richiesta.descrizione() == null ? "" : richiesta.descrizione().trim());
        torneo.setRegole(richiesta.regole() == null ? "" : richiesta.regole().trim());
        torneo.setDataOra(richiesta.dataOra());
        torneo.setPostiMax(richiesta.postiMax());
        torneo.setStato("aperto");

        Torneo salvato = torneoRepository.saveAndFlush(torneo);
        return new TorneoDto(
                salvato.getId(),
                salvato.getNome(),
                salvato.getGioco(),
                salvato.getCategoria(),
                salvato.getDescrizione(),
                salvato.getRegole(),
                salvato.getDataOra(),
                salvato.getPostiMax(),
                0,
                salvato.getStato(),
                false);
    }

    /** Aggiorna i dati senza permettere regressioni di stato o posti insufficienti. */
    @Transactional
    public TorneoDto aggiorna(Integer id, AggiornaTorneoDto richiesta, UUID utenteId) {
        Torneo torneo = torneoRepository.trovaPerAggiornamento(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Torneo non trovato"));
        String nome = richiesta.nome().trim();
        String gioco = richiesta.gioco().trim();
        String categoria = richiesta.categoria().trim();
        validaCampiTorneo(nome, gioco, categoria, richiesta.dataOra());

        if (!statoConsentito(torneo.getStato(), richiesta.stato())) {
            throw new RegolaViolataException("Il passaggio di stato richiesto non è consentito");
        }

        long iscritti = iscrizioneRepository.countByTorneoId(id);
        if (richiesta.postiMax() < iscritti) {
            throw new RegolaViolataException(
                    "Ci sono già " + iscritti + " iscritti: i posti non possono scendere sotto " + iscritti);
        }

        torneo.setNome(nome);
        torneo.setGioco(gioco);
        torneo.setCategoria(categoria);
        torneo.setDescrizione(richiesta.descrizione() == null ? "" : richiesta.descrizione().trim());
        torneo.setRegole(richiesta.regole() == null ? "" : richiesta.regole().trim());
        torneo.setDataOra(richiesta.dataOra());
        torneo.setPostiMax(richiesta.postiMax());
        torneo.setStato(richiesta.stato());

        Torneo salvato = torneoRepository.saveAndFlush(torneo);
        return new TorneoDto(
                salvato.getId(),
                salvato.getNome(),
                salvato.getGioco(),
                salvato.getCategoria(),
                salvato.getDescrizione(),
                salvato.getRegole(),
                salvato.getDataOra(),
                salvato.getPostiMax(),
                iscritti,
                salvato.getStato(),
                utenteId != null && iscrizioneRepository.existsByUtenteIdAndTorneoId(utenteId, id));
    }

    /** Elimina solo tornei senza iscritti, come previsto dal contratto. */
    @Transactional
    public void elimina(Integer id) {
        Torneo torneo = torneoRepository.trovaPerAggiornamento(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Torneo non trovato"));
        long iscritti = iscrizioneRepository.countByTorneoId(id);
        if (iscritti > 0) {
            throw new RegolaViolataException(
                    "Il torneo ha " + iscritti + " iscritti e non può essere eliminato");
        }
        torneoRepository.delete(torneo);
    }

    private void validaCampiTorneo(String nome, String gioco, String categoria, LocalDateTime dataOra) {
        if (nome.length() < 3 || nome.length() > 100) {
            throw new RichiestaNonValidaException("Il nome deve avere fra 3 e 100 caratteri");
        }
        if (gioco.length() < 2 || gioco.length() > 50) {
            throw new RichiestaNonValidaException("Il gioco deve avere fra 2 e 50 caratteri");
        }
        if (categoria.isEmpty()) {
            throw new RichiestaNonValidaException("Seleziona una categoria");
        }
        if (!dataOra.isAfter(LocalDateTime.now())) {
            throw new RichiestaNonValidaException("La data deve essere futura");
        }
    }

    private boolean statoConsentito(String statoAttuale, String nuovoStato) {
        return statoAttuale.equals(nuovoStato)
                || ("aperto".equals(statoAttuale) && "chiuso".equals(nuovoStato))
                || ("chiuso".equals(statoAttuale) && "concluso".equals(nuovoStato));
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