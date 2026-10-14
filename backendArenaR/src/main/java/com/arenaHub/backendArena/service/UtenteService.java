package com.arenaHub.backendArena.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenaHub.backendArena.dto.UtenteAdminDto;
import com.arenaHub.backendArena.dto.UtenteDto;
import com.arenaHub.backendArena.entity.Profilo;
import com.arenaHub.backendArena.repository.IscrizioneRepository;
import com.arenaHub.backendArena.repository.ProfiloRepository;

@Service
public class UtenteService {

    private final AccessoService accessoService;
    private final ProfiloRepository profiloRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final ImpostazioniService impostazioniService;

    public UtenteService(AccessoService accessoService,
                         ProfiloRepository profiloRepository,
                         IscrizioneRepository iscrizioneRepository,
                         ImpostazioniService impostazioniService) {
        this.accessoService = accessoService;
        this.profiloRepository = profiloRepository;
        this.iscrizioneRepository = iscrizioneRepository;
        this.impostazioniService = impostazioniService;
    }

    /** GET /api/auth/me. L'email arriva dal token (claim "email"), non da profiles. */
    @Transactional(readOnly = true)
    public UtenteDto me(UUID utenteId, String email) {
        Profilo p = accessoService.profilo(utenteId);
        return new UtenteDto(p.getId(), p.getUsername(), email, p.getAvatar(), p.getRuolo(),
                iscrizioneRepository.contaAttiviDiUtente(utenteId),
                impostazioniService.limiteTorneiAttivi());
    }

    /** GET /api/utenti, solo admin (controllo nel controller). */
    @Transactional(readOnly = true)
    public List<UtenteAdminDto> listaPerAdmin() {
        int limite = impostazioniService.limiteTorneiAttivi();
        return profiloRepository.listaUtentiConConteggi().stream()
                .map(r -> {
                    long attivi = ((Number) r[3]).longValue();
                    return new UtenteAdminDto(
                            r[0] instanceof UUID u ? u : UUID.fromString(r[0].toString()),
                            (String) r[1],
                            (String) r[2],
                            attivi,
                            ((Number) r[4]).longValue(),
                            // "sopra" = ha più tornei del limite (limite abbassato dopo,
                            // oppure iscrizioni fatte prima della regola)
                            attivi > limite);
                })
                .toList();
    }
}