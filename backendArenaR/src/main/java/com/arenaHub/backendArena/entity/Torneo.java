package com.arenaHub.backendArena.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabella public.tornei.
 *
 * Nessun campo "iscritti": quel numero è il conteggio delle righe in
 * iscrizioni e lo calcola il service con una query, non è una colonna.
 *
 * Lo stato vale "aperto", "chiuso" oppure "concluso". Resta una stringa
 * perché nel database è un text con un CHECK, e tenerlo uguale evita
 * conversioni che poi sfuggono.
 */
@Entity
@Table(name = "tornei")
public class Torneo {

    /** L'id lo genera Postgres: GENERATED ALWAYS AS IDENTITY. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "gioco", nullable = false, length = 50)
    private String gioco;

    /** Raggruppa più giochi: FPS, SPORT e simili. Serve alla ricerca. */
    @Column(name = "categoria", nullable = false)
    private String categoria;

    /** Testo breve della card, massimo 300 caratteri. */
    @Column(name = "descrizione", nullable = false, length = 300)
    private String descrizione;

    /** Regolamento esteso, si legge solo nel pop-up. Massimo 2000 caratteri. */
    @Column(name = "regole", nullable = false, length = 2000)
    private String regole;

    /** Nel database è timestamp without time zone, quindi LocalDateTime. */
    @Column(name = "data_ora", nullable = false)
    private LocalDateTime dataOra;

    @Column(name = "posti_max", nullable = false)
    private Integer postiMax;

    @Column(name = "stato", nullable = false)
    private String stato;

    public Torneo() {
    }

    public boolean isAperto() {
        return "aperto".equals(stato);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getGioco() {
        return gioco;
    }

    public void setGioco(String gioco) {
        this.gioco = gioco;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getRegole() {
        return regole;
    }

    public void setRegole(String regole) {
        this.regole = regole;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    public Integer getPostiMax() {
        return postiMax;
    }

    public void setPostiMax(Integer postiMax) {
        this.postiMax = postiMax;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}