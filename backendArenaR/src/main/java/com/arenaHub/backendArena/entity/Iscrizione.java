package com.arenaHub.backendArena.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Tabella public.iscrizioni.
 *
 * È la tabella che scioglie il molti a molti fra profiles e tornei.
 *
 * Le due relazioni sono LAZY: altrimenti ogni volta che leggi un'iscrizione
 * Hibernate si porta dietro anche il torneo e il profilo, e su una lista
 * diventano decine di query inutili.
 */
@Entity
@Table(name = "iscrizioni")
public class Iscrizione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private Profilo utente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    /**
     * Nel database ha DEFAULT CURRENT_TIMESTAMP, ma la valorizziamo comunque
     * noi: così l'oggetto che restituiamo subito dopo l'inserimento ha già
     * la data, senza doverlo rileggere.
     */
    @Column(name = "data_iscrizione", nullable = false)
    private LocalDateTime dataIscrizione;

    public Iscrizione() {
    }

    public Iscrizione(Profilo utente, Torneo torneo) {
        this.utente = utente;
        this.torneo = torneo;
        this.dataIscrizione = LocalDateTime.now();
    }

    /** Scorciatoia per il JSON, che vuole l'UUID e non tutto il profilo. */
    public UUID getUtenteId() {
        return utente != null ? utente.getId() : null;
    }

    public Integer getTorneoId() {
        return torneo != null ? torneo.getId() : null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Profilo getUtente() {
        return utente;
    }

    public void setUtente(Profilo utente) {
        this.utente = utente;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public LocalDateTime getDataIscrizione() {
        return dataIscrizione;
    }

    public void setDataIscrizione(LocalDateTime dataIscrizione) {
        this.dataIscrizione = dataIscrizione;
    }
}