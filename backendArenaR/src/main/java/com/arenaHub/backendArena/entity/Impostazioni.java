package com.arenaHub.backendArena.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabella public.impostazioni: una sola riga (id = 1, vincolo impostazioni_riga_unica).
 *
 * L'id è SMALLINT nel database, quindi Short qui: con ddl-auto=validate un
 * Integer farebbe fallire l'avvio.
 */
@Entity
@Table(name = "impostazioni")
public class Impostazioni {

    public static final Short ID_UNICO = 1;

    @Id
    @Column(name = "id")
    private Short id;

    /** Quanti tornei attivi (aperti o chiusi) può avere ogni utente. Fra 3 e 999. */
    @Column(name = "max_tornei_attivi", nullable = false)
    private Integer maxTorneiAttivi;

    @Column(name = "aggiornato_il", nullable = false)
    private LocalDateTime aggiornatoIl;

    /** Admin che ha fatto l'ultima modifica. UUID semplice: non serve caricare il profilo. */
    @Column(name = "aggiornato_da")
    private UUID aggiornatoDa;

    public Impostazioni() {
    }

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public Integer getMaxTorneiAttivi() {
        return maxTorneiAttivi;
    }

    public void setMaxTorneiAttivi(Integer maxTorneiAttivi) {
        this.maxTorneiAttivi = maxTorneiAttivi;
    }

    public LocalDateTime getAggiornatoIl() {
        return aggiornatoIl;
    }

    public void setAggiornatoIl(LocalDateTime aggiornatoIl) {
        this.aggiornatoIl = aggiornatoIl;
    }

    public UUID getAggiornatoDa() {
        return aggiornatoDa;
    }

    public void setAggiornatoDa(UUID aggiornatoDa) {
        this.aggiornatoDa = aggiornatoDa;
    }
}