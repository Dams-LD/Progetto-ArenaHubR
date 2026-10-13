package com.arenaHub.backendArena.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabella public.profiles.
 *
 * L'id NON è generato da noi: è lo stesso UUID dell'account in auth.users,
 * creato da Supabase quando l'utente si registra. Per questo non c'è
 * @GeneratedValue: il valore arriva già deciso da fuori.
 *
 * Email e password non stanno qui, vivono in auth.users.
 */
@Entity
@Table(name = "profiles")
public class Profilo {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 30)
    private String username;

    @Column(name = "avatar")
    private String avatar;

    /** Vale "UTENTE" oppure "ADMIN". È da qui che il backend decide i permessi. */
    @Column(name = "ruolo", nullable = false)
    private String ruolo;

    /** Costruttore vuoto richiesto da JPA. */
    public Profilo() {
    }

    public Profilo(UUID id, String username, String avatar, String ruolo) {
        this.id = id;
        this.username = username;
        this.avatar = avatar;
        this.ruolo = ruolo;
    }

    /** Comodo per i controlli sui permessi. */
    public boolean isAdmin() {
        return "ADMIN".equals(ruolo);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }
}