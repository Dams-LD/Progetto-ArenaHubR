import type { Tournament } from '../app.component';
import { TorneoApi } from '../models/torneo.model';

/**
 * Converte il JSON del contratto nel modello che usa il template di Riccardo.
 * Tutta la traduzione dei nomi sta qui: il template resta com'è.
 */

// Ordine importante: 'f1' prima di 'fc', altrimenti "F1" non viene mai riconosciuto.
const IMMAGINI: ReadonlyArray<[string, string]> = [
  ['fortnite', 'fortnite.jpg'],
  ['warzone', 'warzone.jpg'],
  ['call of duty', 'warzone.jpg'],
  ['valorant', 'valorant.jpg'],
  ['pubg', 'pubg.jpg'],
  ['rocket', 'rocket.jpg'],
  ['mortal', 'mortal.jpg'],
  ['ufc', 'ufc.jpg'],
  ['f1', 'f1.jpg'],
  ['fc', 'fc.jpg'],
  ['fifa', 'fc.jpg']
];


export function immaginePerGioco(gioco: string): string {
  const g = (gioco ?? '').toLowerCase();
  return IMMAGINI.find(([chiave]) => g.includes(chiave))?.[1] ?? 'icona.jpg';
}


export function toTournament(t: TorneoApi): Tournament {
  const [date, oraCompleta = ''] = t.dataOra.split('T');
  return {
    id: t.id,
    title: t.nome,
    gioco: t.gioco,
    category: t.categoria,
    description: t.descrizione ?? '',
    regole: t.regole,
    date,                                   // "2026-10-16", usato dal calendario
    time: oraCompleta.slice(0, 5),          // "15:00"
    dateTime: new Date(t.dataOra).toLocaleString('it-IT', {
      day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit'
    }),
    maxSpots: t.postiMax,
    availableSpots: Math.max(0, t.postiMax - t.iscritti),
    stato: t.stato,
    // "chiuso" e "concluso" nascondono entrambi ISCRIVITI/ANNULLA, come da contratto
    isFinished: t.stato !== 'aperto',
    isEnrolled: t.iscritto,
    image: immaginePerGioco(t.gioco)
  };
}


