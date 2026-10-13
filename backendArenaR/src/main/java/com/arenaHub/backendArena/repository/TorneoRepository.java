package com.arenaHub.backendArena.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arenaHub.backendArena.entity.Torneo;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Integer> {

    /**
     * Ricerca con i tre filtri del contratto, tutti facoltativi.
     *
     * Il trucco è "(:parametro is null or colonna = :parametro)": quando il
     * parametro non arriva, quella condizione è sempre vera e di fatto sparisce.
     * Così basta una query sola invece di otto combinazioni diverse.
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
}