// Forma esatta del JSON del backend (docs/api.md). Non rinominare i campi qui.

export type StatoTorneo = 'aperto' | 'chiuso' | 'concluso';

/** GET /api/tornei e GET /api/tornei/{id} (regole solo nel dettaglio). */
export interface TorneoApi {
  id: number;
  nome: string;
  gioco: string;
  categoria: string;
  descrizione: string;
  regole?: string;
  dataOra: string;        // "2026-10-16T15:00:00", ora locale senza fuso
  postiMax: number;
  iscritti: number;
  stato: StatoTorneo;
  iscritto: boolean;
}


/** POST /api/tornei/{id}/iscrizioni */
export interface IscrizioneApi {
  id: number;
  torneoId: number;
  utenteId: string;       // UUID di Supabase, non un numero
  dataIscrizione: string;
}


/** GET /api/iscrizioni/mie */
export interface MiaIscrizioneApi {
  iscrizioneId: number;
  dataIscrizione: string;
  torneo: Omit<TorneoApi, 'iscritto' | 'regole'>;
}


export interface ErroreApi {
  errore: string;
}



/** GET /api/auth/me */
export interface UtenteMe {
  id: string;
  username: string;
  email: string;
  avatar: string | null;
  ruolo: 'UTENTE' | 'ADMIN';
  torneiAttivi: number;
  limiteTorneiAttivi: number;
}

/** GET /api/utenti (solo admin). Gli admin non compaiono: la vista li esclude. */
export interface UtenteAdmin {
  id: string;
  username: string;
  email: string;
  torneiAttivi: number;
  torneiTotali: number;
  sopraLimite: boolean;
}

/** GET e PUT /api/impostazioni */
export interface Limite {
  maxTorneiAttivi: number;
}
