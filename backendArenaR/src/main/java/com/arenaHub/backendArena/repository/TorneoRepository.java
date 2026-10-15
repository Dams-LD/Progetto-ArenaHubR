package com.arenaHub.backendArena.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arenaHub.backendArena.entity.Torneo;

import jakarta.persistence.LockModeType;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Integer> {

    /**
     * Ricerca con i tre filtri del contratto, tutti facoltativi.
     * "(:parametro is null or colonna = :parametro)": se il parametro non arriva,
     * la condizione è sempre vera e sparisce.
     */
    @Query("""
           select t from Torneo t
           where (:stato is null or t.stato = :stato)
             and (:categoria is null or t.categoria = :categoria)
             and (:gioco is null or t.gioco = :gioco)
           order by t.dataOra asc
           """)
    List<Torneo> cerca(@Param("stato") String stato,
                       @Param("categoria") String categoria,
                       @Param("gioco") String gioco);

    /** Le categorie esistenti, per il menu "Ricerca per categoria". */
    @Query("select distinct t.categoria from Torneo t order by t.categoria asc")
    List<String> trovaCategorie();

    /**
     * SELECT ... FOR UPDATE sul torneo: due utenti che prendono l'ultimo posto
     * nello stesso istante non possono passare entrambi il controllo "al completo".
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Torneo t where t.id = :id")
    Optional<Torneo> trovaPerAggiornamento(@Param("id") Integer id);
}