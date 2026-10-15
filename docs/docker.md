# Avviare ArenaHub con Docker

Docker avvia **frontend** (Angular + nginx) e **backend** (Spring Boot) in due container.
Il **database resta su Supabase**: non è dentro Docker, perché anche l'accesso degli utenti passa da Supabase Auth.

## Cosa serve

- Docker Desktop installato e **acceso** (su Windows e Mac).
- La stringa di connessione di Supabase (vedi sotto).

## Prima volta

1. Nella cartella principale del progetto copia `.env.example` in un nuovo file chiamato `.env`.
2. Apri `.env` e inserisci i valori veri. Li trovi su Supabase: **Connect → Session pooler**.
   - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://` + host del pooler + `:5432/postgres`
   - `SPRING_DATASOURCE_USERNAME`: di solito `postgres.` seguito dal codice del progetto
   - `SPRING_DATASOURCE_PASSWORD`: la password del database
3. Avvia tutto:

```bash
docker compose up --build
```

La prima volta scarica le librerie e ci mette qualche minuto. Poi:

- sito: http://localhost:4200
- API del backend: http://localhost:8080/api

## Comandi di tutti i giorni

| Cosa | Comando |
|---|---|
| Avviare (in primo piano, con i log) | `docker compose up` |
| Avviare in sottofondo | `docker compose up -d` |
| Ricompilare dopo aver cambiato il codice | `docker compose up --build` |
| Vedere i log del backend | `docker compose logs -f backend` |
| Fermare tutto | `docker compose down` |

## Regole da ricordare

- **`.env` non va mai su Git** (è già nel `.gitignore`). Su Git va solo `.env.example`, con valori finti.
- Il frontend deve restare sulla porta **4200**: il backend accetta chiamate solo da `http://localhost:4200` (`ORIGINE_FRONTEND` in `SecurityConfig.java`).
- Prima di avviare con Docker, ferma `ng serve` e il backend avviato da Spring Tool Suite: userebbero le stesse porte (4200 e 8080).
- L'indirizzo del backend nel frontend (`apiUrl` in `environment.ts`) è `http://localhost:8080/api`: funziona perché il browser gira sullo stesso computer dei container.

## Se qualcosa non funziona

| Problema | Cosa fare |
|---|---|
| `Cannot connect to the Docker daemon` | Docker Desktop non è acceso: aprilo e aspetta che sia pronto |
| `Manca SPRING_DATASOURCE_URL nel file .env` | Il file `.env` non esiste o la riga è vuota |
| Il backend si ferma subito con un errore di connessione al database | Controlla i tre valori in `.env`. Usa il **Session pooler** (porta 5432): la connessione diretta spesso richiede IPv6, che i container non hanno |
| La password contiene il simbolo `$` | In `.env` scrivilo doppio: `$$` |
| Nel browser compare un errore CORS | Il sito non è aperto da `http://localhost:4200` |
| `port is already allocated` | Un altro programma usa la porta 4200 o 8080: chiudilo |
