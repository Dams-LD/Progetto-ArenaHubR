package com.arenaHub.backendArena.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenaHub.backendArena.dto.IscrizioneDto;
import com.arenaHub.backendArena.dto.MiaIscrizioneDto;
import com.arenaHub.backendArena.dto.TorneoDto;
import com.arenaHub.backendArena.entity.Iscrizione;
import com.arenaHub.backendArena.entity.Profilo;
import com.arenaHub.backendArena.entity.Torneo;
import com.arenaHub.backendArena.exception.ConflittoException;
import com.arenaHub.backendArena.exception.RegolaViolataException;
import com.arenaHub.backendArena.exception.RichiestaNonValidaException;
import com.arenaHub.backendArena.exception.RisorsaNonTrovataException;
import com.arenaHub.backendArena.repository.IscrizioneRepository;
import com.arenaHub.backendArena.repository.ProfiloRepository;
import com.arenaHub.backendArena.repository.TorneoRepository;

@Service
public class IscrizioneService {

    /** Codice che il trigger del database usa quando il limite è superato. */
    private static final String SQLSTATE_LIMITE = "TL001";

    private static final Set<String> STATI_VALIDI = Set.of("aperto", "chiuso", "concluso");

    private final IscrizioneRepository iscrizioneRepository;
    private final TorneoRepository torneoRepository;
    private final ProfiloRepository profiloRepository;
    private final ImpostazioniService impostazioniService;

    public IscrizioneService(IscrizioneRepository iscrizioneRepository,
                             TorneoRepository torneoRepository,
                             ProfiloRepository profiloRepository,
                             ImpostazioniService impostazioniService) {
        this.iscrizioneRepository = iscrizioneRepository;
        this.torneoRepository = torneoRepository;
        this.profiloRepository = profiloRepository;
        this.impostazioniService = impostazioniService;
    }

    /**
     * POST /api/tornei/{id}/iscrizioni
     *
     * I due lock (prima profilo, poi torneo, sempre in quest'ordine) rendono
     * sicuri i controlli anche con richieste contemporanee.
     */
    @Transactional
    public IscrizioneDto iscrivi(Integer torneoId, UUID utenteId) {

        Profilo profilo = profiloRepository.trovaPerAggiornamento(utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Profilo non trovato"));

        Torneo torneo = torneoRepository.trovaPerAggiornamento(torneoId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Torneo non trovato"));

        if (!torneo.isAperto()) {
            throw new RegolaViolataException("Le iscrizioni a questo torneo sono chiuse");
        }
        if (iscrizioneRepository.existsByUtenteIdAndTorneoId(utenteId, torneoId)) {
            throw new ConflittoException("Sei già iscritto a questo torneo");
        }
        if (iscrizioneRepository.countByTorneoId(torneoId) >= torneo.getPostiMax()) {
            throw new RegolaViolataException("Il torneo è al completo");
        }

        int limite = impostazioniService.limiteTorneiAttivi();
        if (iscrizioneRepository.contaAttiviDiUtente(utenteId) >= limite) {
            throw new RegolaViolataException(messaggioLimite(limite));
        }

        try {
            // saveAndFlush: l'INSERT parte adesso, così un errore del database
            // (vincolo UNIQUE o trigger TL001) lo intercettiamo qui
            Iscrizione salvata = iscrizioneRepository.saveAndFlush(new Iscrizione(profilo, torneo));
            return new IscrizioneDto(salvata.getId(), torneoId, utenteId, salvata.getDataIscrizione());

        } catch (DataAccessException e) {
            // NB: dopo un errore Postgres annulla la transazione e rifiuta altre query,
            // quindi usiamo il limite già letto sopra invece di rileggerlo
            if (SQLSTATE_LIMITE.equals(sqlState(e))) {
                throw new RegolaViolataException(messaggioLimite(limite));
            }
            if (e instanceof DataIntegrityViolationException) {
                throw new ConflittoException("Sei già iscritto a questo torneo");
            }
            throw e;
        }
    }

    /** DELETE /api/tornei/{id}/iscrizioni */
    @Transactional
    public void annulla(Integer torneoId, UUID utenteId) {
        Iscrizione iscrizione = iscrizioneRepository.findByUtenteIdAndTorneoId(utenteId, torneoId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Iscrizione non trovata"));

        if (!iscrizione.getTorneo().isAperto()) {
            throw new RegolaViolataException("Non puoi annullare: le iscrizioni sono chiuse");
        }
        iscrizioneRepository.delete(iscrizione);
    }

    /** GET /api/iscrizioni/mie?stato= */
    @Transactional(readOnly = true)
    public List<MiaIscrizioneDto> mie(UUID utenteId, String stato) {
        if (stato != null && !STATI_VALIDI.contains(stato)) {
            throw new RichiestaNonValidaException("Stato non valido");
        }
        Map<Integer, Long> conteggi = new HashMap<>();
        for (Object[] riga : iscrizioneRepository.contaIscrittiPerTorneo()) {
            conteggi.put((Integer) riga[0], (Long) riga[1]);
        }
        return iscrizioneRepository.trovaDiUtente(utenteId, stato).stream()
                .map(i -> {
                    Torneo t = i.getTorneo();
                    TorneoDto dto = TorneoDto.perLista(t.getId(), t.getNome(), t.getGioco(),
                            t.getCategoria(), t.getDescrizione(), t.getDataOra(), t.getPostiMax(),
                            conteggi.getOrDefault(t.getId(), 0L), t.getStato(), true);
                    return new MiaIscrizioneDto(i.getId(), i.getDataIscrizione(), dto);
                })
                .toList();
    }

    private String messaggioLimite(int limite) {
        return "Non puoi iscriverti: hai già raggiunto il limite di " + limite + " tornei attivi.";
    }

    /** Cerca il codice SQLSTATE risalendo la catena delle cause (Spring > Hibernate > JDBC). */
    private String sqlState(Throwable e) {
        for (Throwable c = e; c != null; c = c.getCause()) {
            if (c instanceof SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
        }
        return null;
    }
}