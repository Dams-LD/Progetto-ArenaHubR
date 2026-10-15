package com.arenaHub.backendArena.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arenaHub.backendArena.entity.Profilo;

import jakarta.persistence.LockModeType;

@Repository
public interface ProfiloRepository extends JpaRepository<Profilo, UUID> {

    /**
     * SELECT ... FOR UPDATE sulla riga del profilo.
     *
     * Serve al limite dei tornei attivi: se lo stesso utente manda due
     * iscrizioni insieme (doppio click su due tornei diversi), la seconda
     * aspetta che la prima finisca e poi vede il conteggio aggiornato.
     * Senza il lock entrambe leggerebbero "2 su 3" e passerebbero tutte e due.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Profilo p where p.id = :id")
    Optional<Profilo> trovaPerAggiornamento(@Param("id") UUID id);

    /**
     * Lista utenti per l'admin: conteggi dalla vista v_utenti_iscrizioni
     * (solo ruolo UTENTE), email da auth.users. Query nativa perché nessuna
     * delle due è un'entity. Il backend si collega come postgres e può leggerle.
     *
     * Ogni riga: [id, username, email, torneiAttivi, torneiTotali]
     */
    @Query(value = """
            select v.id, v.username, u.email, v.tornei_attivi, v.tornei_totali
            from public.v_utenti_iscrizioni v
            join auth.users u on u.id = v.id
            order by v.username
            """, nativeQuery = true)
    List<Object[]> listaUtentiConConteggi();
}