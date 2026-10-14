
--gli utenti sono stati creati manualmente in supabase, perciò la seguente query, serve solo per assegnare i ruoli 



UPDATE public.profiles p
SET
    username = seed.username,
    ruolo = seed.ruolo
FROM auth.users u
JOIN (
    VALUES
        ('admin1@example.com', 'admin_arena1', 'ADMIN'),
        ('admin2@example.com', 'admin_arena2', 'ADMIN'),
        ('user1@example.com', 'player_alpha', 'UTENTE'),
        ('user2@example.com', 'player_bravo', 'UTENTE'),
        ('user3@example.com', 'player_charlie', 'UTENTE'),
        ('user4@example.com', 'player_delta', 'UTENTE')
) AS seed(email, username, ruolo)
    ON LOWER(u.email) = LOWER(seed.email)
WHERE p.id = u.id;

-------------------------------------------------------------------------------------------------------------------------------------------------------------------------

BEGIN;
 
ALTER TABLE public.iscrizioni DISABLE TRIGGER trg_check_limite_iscrizioni;
 
INSERT INTO public.tornei (
    nome,
    gioco,
    categoria,
    descrizione,
    regole,
    data_ora,
    posti_max,
    stato
)
SELECT
    seed.nome,
    seed.gioco,
    seed.categoria,
    seed.descrizione,
    seed.regole,
    seed.data_ora,
    seed.posti_max,
    seed.stato
FROM (
    VALUES
    (
        'Torneo Warzone',
        'Warzone',
        'FPS',
        'Competizione su Call of Duty Warzone.',
        'I partecipanti devono rispettare gli orari stabiliti. I risultati vengono verificati dagli organizzatori.',
        TIMESTAMP '2026-10-18 15:00:00',
        16,
        'aperto'
    ),
    (
        'Torneo Fortnite',
        'Fortnite',
        'BATTLE ROYALE',
        'Torneo competitivo di Fortnite.',
        'I partecipanti devono rispettare il regolamento comunicato dagli organizzatori. Eventuali contestazioni saranno valutate dall amministrazione.',
        TIMESTAMP '2026-10-22 15:00:00',
        32,
        'aperto'
    ),
    (
        'Torneo Valorant',
        'Valorant',
        'FPS',
        'Torneo competitivo di Valorant.',
        'Le partite seguono le impostazioni concordate. I risultati finali vengono confermati dagli organizzatori.',
        TIMESTAMP '2026-10-24 15:00:00',
        16,
        'aperto'
    ),
    (
        'Torneo FC26',
        'FC26',
        'SPORT',
        'Torneo di calcio virtuale su FC26.',
        'Gli incontri si disputano secondo il tabellone organizzato. Le decisioni organizzative spettano agli amministratori.',
        TIMESTAMP '2026-10-25 15:00:00',
        16,
        'aperto'
    ),
    (
        'Torneo F1',
        'F1',
        'SPORT',
        'Competizione automobilistica virtuale di Formula 1.',
        'Le gare seguono le impostazioni concordate dagli organizzatori. La classifica viene determinata dai risultati delle gare.',
        TIMESTAMP '2026-10-20 15:00:00',
        12,
        'chiuso'
    ),
    (
        'Torneo UFC',
        'UFC',
        'PICCHIADURO',
        'Torneo dedicato alle sfide virtuali UFC.',
        'Gli incontri seguono le impostazioni concordate. Gli organizzatori verificano i risultati.',
        TIMESTAMP '2026-10-05 15:00:00',
        8,
        'concluso'
    ),
    (
        'Torneo Mortal Kombat',
        'Mortal Kombat',
        'PICCHIADURO',
        'Torneo competitivo dedicato a Mortal Kombat.',
        'Gli incontri si svolgono secondo il tabellone stabilito dagli organizzatori.',
        TIMESTAMP '2026-10-11 15:00:00',
        8,
        'concluso'
    ),
    (
        'Torneo Rocket League',
        'Rocket League',
        'SPORT',
        'Competizione a squadre di Rocket League.',
        'Le partite si disputano con impostazioni standard. I risultati vengono confermati dagli organizzatori.',
        TIMESTAMP '2026-10-24 14:00:00',
        8,
        'aperto'
    ),
    (
        'Torneo PUBG',
        'PUBG',
        'BATTLE ROYALE',
        'Torneo competitivo su PUBG.',
        'I partecipanti devono rispettare gli orari e le regole stabilite dagli organizzatori.',
        TIMESTAMP '2026-10-27 15:00:00',
        24,
        'chiuso'
    )
) AS seed (
    nome,
    gioco,
    categoria,
    descrizione,
    regole,
    data_ora,
    posti_max,
    stato
)
WHERE NOT EXISTS (
    SELECT 1
    FROM public.tornei t
    WHERE LOWER(t.nome) = LOWER(seed.nome)
);
 
WITH dati_iscrizioni (username, nome_torneo, data_iscrizione) AS (
    VALUES
        ('player_alpha', 'Torneo Warzone', TIMESTAMP '2026-10-12 10:00:00'),
        ('player_bravo', 'Torneo Warzone', TIMESTAMP '2026-10-12 10:10:00'),
        ('player_charlie', 'Torneo Warzone', TIMESTAMP '2026-10-12 10:20:00'),
 
        ('player_alpha', 'Torneo Valorant', TIMESTAMP '2026-10-12 11:00:00'),
        ('player_charlie', 'Torneo Valorant', TIMESTAMP '2026-10-12 11:10:00'),
 
        ('player_bravo', 'Torneo Fortnite', TIMESTAMP '2026-10-12 12:00:00'),
        ('player_charlie', 'Torneo Fortnite', TIMESTAMP '2026-10-12 12:10:00'),
 
        ('player_alpha', 'Torneo FC26', TIMESTAMP '2026-10-12 13:00:00'),
        ('player_delta', 'Torneo FC26', TIMESTAMP '2026-10-12 13:10:00'),
 
        ('player_alpha', 'Torneo F1', TIMESTAMP '2026-10-10 10:00:00'),
        ('player_charlie', 'Torneo F1', TIMESTAMP '2026-10-10 10:10:00'),
 
        ('player_alpha', 'Torneo UFC', TIMESTAMP '2026-10-01 10:00:00'),
        ('player_delta', 'Torneo UFC', TIMESTAMP '2026-10-01 10:10:00'),
 
        ('player_bravo', 'Torneo Mortal Kombat', TIMESTAMP '2026-10-02 10:00:00'),
        ('player_delta', 'Torneo Mortal Kombat', TIMESTAMP '2026-10-02 10:10:00'),
 
        ('player_bravo', 'Torneo Rocket League', TIMESTAMP '2026-10-12 14:00:00'),
        ('player_delta', 'Torneo Rocket League', TIMESTAMP '2026-10-12 14:10:00'),
 
        ('player_charlie', 'Torneo PUBG', TIMESTAMP '2026-10-10 11:00:00'),
        ('player_delta', 'Torneo PUBG', TIMESTAMP '2026-10-10 11:10:00')
)
INSERT INTO public.iscrizioni (
    utente_id,
    torneo_id,
    data_iscrizione
)
SELECT
    p.id,
    t.id,
    d.data_iscrizione
FROM dati_iscrizioni d
JOIN public.profiles p
    ON LOWER(p.username) = LOWER(d.username)
   AND p.ruolo = 'UTENTE'
JOIN public.tornei t
    ON LOWER(t.nome) = LOWER(d.nome_torneo)
ON CONFLICT (utente_id, torneo_id) DO NOTHING;
 
ALTER TABLE public.iscrizioni ENABLE TRIGGER trg_check_limite_iscrizioni;
 
COMMIT;