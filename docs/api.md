# API ArenaHub — v1

Se qualcosa qui dentro cambia, va aggiornato anche `change-requests.md` e la versione sale.

---

## Come leggiamo le risposte

Campi in camelCase, date in ISO 8601 (`2026-10-16T15:00:00`), liste come array JSON.
Gli errori hanno sempre la stessa forma:

```json
{ "errore": "Torneo non trovato" }
```

Errori che possono capitare un po' ovunque e che non ripetiamo endpoint per endpoint:

```
400  dati non validi      {"errore": "Il campo nome è obbligatorio"}
401  sessione mancante    {"errore": "Devi effettuare il login"}
403  non sei admin        {"errore": "Operazione non consentita"}
```

**Stati del torneo:** `aperto` → `chiuso` → `concluso`.
Ci si iscrive e si annulla solo quando è `aperto`.

**Descrizione e regole** sono due campi diversi. La descrizione è corta e sta nelle card, le regole sono lunghe e servono solo nel pop-up. Per questo la lista dei tornei non porta le regole: con otto card sarebbero otto testi lunghi che nessuno legge. Il pop-up "Vedi" fa una chiamata al dettaglio.

**Immagini dei tornei:** stanno in `assets/` lato Angular e si scelgono dal campo `gioco`. Nel database non c'è niente.

**I form non sono endpoint.** Crea nuovo torneo e Modifica sono pagine Angular: si aprono senza chiamare il backend. La chiamata parte solo quando si preme Salva, ed è il punto 9 o il punto 10.

---

## Login e sessione

Si entra con **email e password**. Lo username serve per mostrarlo nella barra e nella lista iscritti, non per accedere.

Usiamo la sessione con cookie di Spring Security: dopo il login il browser si porta dietro il cookie da solo.

Siccome Angular sta sulla 4200 e Spring sulla 8080, servono due cose, una per parte:

- backend: CORS che accetta `http://localhost:4200` con `allowCredentials(true)`
- frontend: `withCredentials: true` su tutte le chiamate

Se ne manca una il login sembra funzionare ma poi ogni chiamata torna 401. Succede, controllare qui per primo.

Il backend risponde 401 anche quando il pulsante era visibile, per esempio se la sessione scade mentre l'utente sta sulla pagina. Il frontend intercetta il 401 e riapre il login invece di mostrare un errore generico.

> **Da decidere.** Nella documentazione avevamo scritto di usare lo schema `auth` di Supabase. Le due cose non vanno insieme: con Supabase Auth gli endpoint 1, 2 e 3 spariscono, il token arriva in `Authorization: Bearer`, e gli id utente diventano UUID. Da chiudere prima di scrivere codice.

---

## Elenco

| | | |
|---|---|---|
| 1 | `POST /api/auth/register` | pubblico |
| 2 | `POST /api/auth/login` | pubblico |
| 3 | `POST /api/auth/logout` | utente |
| 4 | `GET /api/auth/me` | utente |
| 5 | `PUT /api/auth/me` | utente |
| 6 | `GET /api/categorie` | pubblico |
| 7 | `GET /api/tornei` | pubblico |
| 8 | `GET /api/tornei/{id}` | pubblico |
| 9 | `POST /api/tornei` | admin |
| 10 | `PUT /api/tornei/{id}` | admin |
| 11 | `DELETE /api/tornei/{id}` | admin |
| 12 | `POST /api/tornei/{id}/iscrizioni` | utente |
| 13 | `DELETE /api/tornei/{id}/iscrizioni` | utente |
| 14 | `GET /api/iscrizioni/mie` | utente |
| 15 | `GET /api/tornei/{id}/iscrizioni` | admin |
| 16 | `GET /api/calendario` | pubblico |

---

## 1. POST /api/auth/register

Crea l'utente e apre già la sessione, così dopo la registrazione non deve rifare il login.

Richiesta:

```json
{ "username": "marco", "email": "marco@example.com", "password": "password123" }
```

username 3-30 caratteri e univoco, email valida e univoca, password almeno 8 caratteri.

Risposta 201:

```json
{ "id": 12, "username": "marco", "email": "marco@example.com", "avatar": null, "ruolo": "UTENTE" }
```

Errori:

```
409  {"errore": "Username già registrato"}
409  {"errore": "Email già registrata"}
```

---

## 2. POST /api/auth/login

```json
{ "email": "marco@example.com", "password": "password123" }
```

Risposta 200:

```json
{ "id": 12, "username": "marco", "email": "marco@example.com", "avatar": "avatar3.png", "ruolo": "UTENTE" }
```

Il ruolo serve al frontend per far comparire la voce ADMIN. I permessi veri li controlla comunque il backend.

Errori:

```
401  {"errore": "Email o password non corretti"}
```

---

## 3. POST /api/auth/logout

Chiude la sessione. Risposta 204, niente corpo.

---

## 4. GET /api/auth/me

Il frontend la chiama appena carica la pagina, per sapere se la sessione è ancora buona e cosa scrivere nella barra.

Risposta 200: stessa roba del login.

Se non c'è sessione, 401.

---

## 5. PUT /api/auth/me

Serve al box ACCOUNT per cambiare username e avatar. Email, password e ruolo non si toccano da qui.

```json
{ "username": "marco_88", "avatar": "avatar7.png" }
```

Risposta 200: l'utente aggiornato, stessa struttura del punto 4.

Errori:

```
409  {"errore": "Username già registrato"}
```

> **Da decidere.** Se l'avatar è scelto da una galleria fissa, il backend salva solo il nome del file e non serve altro. Se invece si vuole caricare un'immagine dal telefono servono Supabase Storage o una cartella sul server. Per l'MVP conviene la galleria.

---

## 6. GET /api/categorie

Riempie il menu "Ricerca per categoria" e il campo categoria nel form di creazione.

```json
["FPS", "SPORT", "PICCHIADURO", "CORSE"]
```

Per ora lo ricaviamo con un `SELECT DISTINCT categoria FROM torneo`. Se poi diventa una tabella a sé, l'endpoint resta uguale.

---

## 7. GET /api/tornei

La lista del catalogo. Parametri opzionali: `stato`, `categoria`, `gioco`.

Esempi: `/api/tornei?stato=aperto`, `/api/tornei?categoria=FPS`

Risposta 200:

```json
[
  {
    "id": 3,
    "nome": "Torneo Moto GP",
    "gioco": "Moto GP",
    "categoria": "SPORT",
    "descrizione": "Torneo a eliminazione diretta aperto a tutti gli studenti.",
    "dataOra": "2026-10-16T15:00:00",
    "postiMax": 10,
    "iscritti": 5,
    "stato": "aperto",
    "iscritto": false
  }
]
```

I posti residui li calcola il frontend con `postiMax - iscritti`, la card mostra 5/10.

`iscritto` dice se l'utente loggato sta già dentro quel torneo. Per un visitatore è sempre `false`. Serve a far comparire "SEI ISCRITTO" disattivato al posto di "ISCRIVITI", senza doverci fare una seconda chiamata.

Errori:

```
400  {"errore": "Stato non valido"}
```

---

## 8. GET /api/tornei/{id}

Quello che apre il pop-up "Vedi". Uguale al punto 7 ma con in più `regole`:

```json
{
  "id": 3,
  "nome": "Torneo Moto GP",
  "gioco": "Moto GP",
  "categoria": "SPORT",
  "descrizione": "Torneo a eliminazione diretta aperto a tutti gli studenti.",
  "regole": "1. Impostazioni standard.\n2. Gare al meglio di tre manche.\n3. Chi non si presenta entro 10 minuti perde a tavolino.",
  "dataOra": "2026-10-16T15:00:00",
  "postiMax": 10,
  "iscritti": 5,
  "stato": "aperto",
  "iscritto": false
}
```

Errori:

```
404  {"errore": "Torneo non trovato"}
```

---

## 9. POST /api/tornei

Quello che parte quando l'admin preme Salva nel form "Crea nuovo torneo". Il torneo nasce sempre in stato `aperto`.

Solo admin. Il controllo lo fa il backend guardando il ruolo in sessione, non si fida di quello che arriva dal frontend: se chiama un utente normale, o qualcuno da Postman senza sessione, torna 403 (o 401 se non è loggato per niente) e non viene creato niente.

```json
{
  "nome": "Torneo F1",
  "gioco": "F1",
  "categoria": "SPORT",
  "descrizione": "Campionato su quattro circuiti.",
  "regole": "1. Assistenze disattivate.\n2. Danni realistici.",
  "dataOra": "2026-10-21T14:30:00",
  "postiMax": 8
}
```

Obbligatori: nome (3-100), gioco (2-50), categoria, dataOra futura, postiMax fra 2 e 128.
Facoltativi: descrizione (max 300), regole (max 2000).

Risposta 201 con il torneo creato, `iscritti: 0`.

Errori:

```
400  {"errore": "La data deve essere futura"}
403  {"errore": "Operazione non consentita"}
```

---

## 10. PUT /api/tornei/{id}

Il form "Modifica", stesso discorso del punto 9: solo admin, controllo sul backend. Qui dentro c'è anche `stato`, ed è così che l'admin chiude un torneo o lo segna come concluso.

Stessi campi della creazione più:

```json
{ "stato": "chiuso" }
```

Passaggi ammessi: `aperto → chiuso`, `chiuso → concluso`, oppure stato invariato se si cambiano solo i dati.

Errori:

```
404  {"errore": "Torneo non trovato"}
422  {"errore": "Un torneo concluso non può tornare aperto"}
422  {"errore": "Ci sono già 5 iscritti: i posti non possono scendere sotto 5"}
```

---

## 11. DELETE /api/tornei/{id}

Il pulsante ELIMINA, con la conferma "sei sicuro?". Funziona solo se il torneo non ha nessuno iscritto, come avevamo deciso. Se ci sono iscritti la strada giusta è chiuderlo con il punto 10.

Risposta 204.

Errori:

```
404  {"errore": "Torneo non trovato"}
422  {"errore": "Il torneo ha 5 iscritti e non può essere eliminato"}
```

---

## 12. POST /api/tornei/{id}/iscrizioni

Il pulsante ISCRIVITI. Non manda niente nel corpo: chi è l'utente lo sa già il backend dalla sessione.

Risposta 201:

```json
{ "id": 87, "torneoId": 3, "utenteId": 12, "dataIscrizione": "2026-10-14T10:30:00" }
```

Errori:

```
404  {"errore": "Torneo non trovato"}
409  {"errore": "Sei già iscritto a questo torneo"}
422  {"errore": "Le iscrizioni a questo torneo sono chiuse"}
422  {"errore": "Il torneo è al completo"}
```

---

## 13. DELETE /api/tornei/{id}/iscrizioni

L'annullamento, si fa dalla pagina "I miei tornei". Risposta 204.

Errori:

```
404  {"errore": "Iscrizione non trovata"}
422  {"errore": "Non puoi annullare: le iscrizioni sono chiuse"}
```

---

## 14. GET /api/iscrizioni/mie

I tornei dell'utente loggato. Con `?stato=concluso` diventa lo storico del profilo.

```json
[
  {
    "iscrizioneId": 87,
    "dataIscrizione": "2026-10-14T10:30:00",
    "torneo": {
      "id": 3,
      "nome": "Torneo Moto GP",
      "gioco": "Moto GP",
      "categoria": "SPORT",
      "descrizione": "Torneo a eliminazione diretta aperto a tutti gli studenti.",
      "dataOra": "2026-10-16T15:00:00",
      "postiMax": 10,
      "iscritti": 5,
      "stato": "aperto"
    }
  }
]
```

Il pulsante "Annulla" compare solo sui tornei ancora aperti.

---

## 15. GET /api/tornei/{id}/iscrizioni

La lista iscritti lato admin.

```json
[
  {
    "iscrizioneId": 87,
    "utenteId": 12,
    "username": "marco",
    "email": "marco@example.com",
    "dataIscrizione": "2026-10-14T10:30:00"
  }
]
```

Errori:

```
404  {"errore": "Torneo non trovato"}
```

---

## 16. GET /api/calendario

Riempie la pagina Calendario. Restituisce i tornei **aperti** di un mese.

Il filtro sullo stato sta qui dentro, non fra i parametri: così il frontend non se lo può dimenticare e sul calendario non finiscono tornei già chiusi su cui non ci si può più iscrivere.

I campi sono pochi di proposito: al calendario servono data e nome per colorare le caselle, non tutta la scheda. Quando l'utente clicca e vuole il regolamento, si va di punto 8.

Parametro obbligatorio: `mese` nel formato `AAAA-MM`.

Esempio: `/api/calendario?mese=2026-10`

Risposta 200:

```json
[
  {
    "id": 3,
    "nome": "Torneo Moto GP",
    "gioco": "Moto GP",
    "dataOra": "2026-10-16T15:00:00"
  },
  {
    "id": 7,
    "nome": "Torneo Warzone",
    "gioco": "Warzone",
    "dataOra": "2026-10-24T18:00:00"
  }
]
```

Il mese senza tornei aperti torna `[]`, non è un errore: il frontend disegna la griglia senza caselle colorate.

La lista può contenere due tornei nello stesso giorno. Il frontend decide se la casella li apre tutti e due o se ne mostra una lista.

Errori:

```
400  {"errore": "Mese non valido"}
```

## API AGGIUNTE VERSIONE 1.2 E CON CHANGE REQUEST

# GET /api/impostazioni

# PUT /api/impostazioni

# GET /api/utenti




---

## Quale schermata chiama cosa

| Schermata | Chiamate |
|---|---|
| Catalogo, visitatore | 6, 7 |
| Catalogo, utente loggato | 4, 6, 7 |
| Ricerca per categoria | 7 con `?categoria=` |
| Pop-up "Vedi" | 8 |
| Iscriviti | 12 |
| Login / Registrati | 2 / 1 |
| Box ACCOUNT | 5 |
| I miei tornei | 14 |
| Storico profilo | 14 con `?stato=concluso` |
| Annulla iscrizione | 13 |
| Calendario | 16 |
| Calendario, click su un giorno | 8 |
| Admin, catalogo | 7 |
| Admin, form crea (apertura) | 6 per il menu categoria |
| Admin, form crea (salva) | 9 |
| Admin, modifica e chiusura | 10 |
| Admin, elimina | 11 |
| Admin, lista iscritti | 15 |

---

## Fuori dalla v1

La classifica dei tornei conclusi sta nei wireframe ma è bonus, la facciamo solo se chiudiamo l'MVP in tempo. Stesso discorso per lista d'attesa, password dimenticata, richiesta torneo da parte dell'utente e upload dell'avatar.

## Cose ancora da chiudere

1. Sessione Spring Security o Supabase Auth — tocca cinque endpoint e il tipo degli id, va deciso subito
2. Avatar: galleria fissa o upload
3. Categoria: campo testo o tabella (lo decide Damiano)
4. Che aspetto ha la card quando il torneo è `chiuso` ma non ancora concluso
5. Dove sta il selettore di stato nel form admin
6. Come disegnamo la schermata con la lista iscritti
7. Cosa succede sul calendario se in un giorno ci sono due tornei
8. Nomi definitivi di tabelle e colonne