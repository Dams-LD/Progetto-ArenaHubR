package com.arenaHub.backendArena.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arenaHub.backendArena.entity.Iscrizione;

@Repository
public interface IscrizioneRepository extends JpaRepository<Iscrizione, Integer> {

    /**
     * Quanti iscritti ha ogni torneo, tutto in una query sola.
     *
     * Ogni riga del risultato è una coppia [idTorneo, numeroIscritti].
     * Senza questa, per otto card servirebbero otto query separate:
     * è il classico problema N+1, e su una lista si sente subito.
     */
    @Query("select i.torneo.id, count(i) from Iscrizione i group by i.torneo.id")
    List<Object[]> contaIscrittiPerTorneo();

    /** Gli id dei tornei a cui un utente è già iscritto. Serve al campo "iscritto". */
    @Query("select i.torneo.id from Iscrizione i where i.utente.id = :utenteId")
    List<Integer> trovaIdTorneiDiUtente(@Param("utenteId") UUID utenteId);

    /** Iscritti a un singolo torneo. */
    long countByTorneoId(Integer torneoId);

    /** Per il controllo "sei già iscritto". */
    boolean existsByUtenteIdAndTorneoId(UUID utenteId, Integer torneoId);
}