package com.arenaHub.backendArena.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arenaHub.backendArena.entity.Iscrizione;

/**
 * Le query che filtrano per utente o torneo sono scritte a mano con @Query.
 * L'entity Iscrizione ha i metodi di comodo getUtenteId() e getTorneoId():
 * con le query ricavate dal nome, Spring Data li scambierebbe per campi
 * e genererebbe "i.utenteId", che non esiste (errore 500).
 */
@Repository
public interface IscrizioneRepository extends JpaRepository<Iscrizione, Integer> {

    /** Quanti iscritti ha ogni torneo, in una query sola. Ogni riga: [idTorneo, numeroIscritti]. */
    @Query("select i.torneo.id, count(i) from Iscrizione i group by i.torneo.id")
    List<Object[]> contaIscrittiPerTorneo();

    /** Gli id dei tornei a cui un utente è già iscritto. Serve al campo "iscritto". */
    @Query("select i.torneo.id from Iscrizione i where i.utente.id = :utenteId")
    List<Integer> trovaIdTorneiDiUtente(@Param("utenteId") UUID utenteId);

    /** Iscritti a un singolo torneo. */
    @Query("select count(i) from Iscrizione i where i.torneo.id = :torneoId")
    long countByTorneoId(@Param("torneoId") Integer torneoId);

    /** Per il controllo "sei già iscritto". */
    @Query("""
           select case when count(i) > 0 then true else false end
           from Iscrizione i
           where i.utente.id = :utenteId and i.torneo.id = :torneoId
           """)
    boolean existsByUtenteIdAndTorneoId(@Param("utenteId") UUID utenteId,
                                        @Param("torneoId") Integer torneoId);

    /** Per l'annullamento. */
    @Query("select i from Iscrizione i where i.utente.id = :utenteId and i.torneo.id = :torneoId")
    Optional<Iscrizione> findByUtenteIdAndTorneoId(@Param("utenteId") UUID utenteId,
                                                   @Param("torneoId") Integer torneoId);

    /** Tornei attivi di un utente: aperti o chiusi, i conclusi non contano. */
    @Query("""
           select count(i) from Iscrizione i
           where i.utente.id = :utenteId and i.torneo.stato <> 'concluso'
           """)
    long contaAttiviDiUtente(@Param("utenteId") UUID utenteId);

    /** "I miei tornei". join fetch: il torneo arriva nella stessa query. */
    @Query("""
           select i from Iscrizione i join fetch i.torneo t
           where i.utente.id = :utenteId
             and (:stato is null or t.stato = :stato)
           order by t.dataOra asc
           """)
    List<Iscrizione> trovaDiUtente(@Param("utenteId") UUID utenteId,
                                   @Param("stato") String stato);
}