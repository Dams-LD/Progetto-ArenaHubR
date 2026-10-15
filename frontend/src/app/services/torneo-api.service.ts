import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { IscrizioneApi, Limite, MiaIscrizioneApi, TorneoApi, UtenteAdmin, UtenteMe } from '../models/torneo.model';


/** Tutte le chiamate HTTP verso i tornei. I componenti non usano HttpClient direttamente. */
@Injectable({ providedIn: 'root' })
export class TorneoApiService {


  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;


  lista(): Observable<TorneoApi[]> {
    return this.http.get<TorneoApi[]>(`${this.base}/tornei`);
  }


  dettaglio(id: number): Observable<TorneoApi> {
    return this.http.get<TorneoApi>(`${this.base}/tornei/${id}`);
  }


  categorie(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/categorie`);
  }


  iscriviti(id: number): Observable<IscrizioneApi> {
    return this.http.post<IscrizioneApi>(`${this.base}/tornei/${id}/iscrizioni`, null);
  }


  annulla(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/tornei/${id}/iscrizioni`);
  }


  mie(): Observable<MiaIscrizioneApi[]> {
    return this.http.get<MiaIscrizioneApi[]>(`${this.base}/iscrizioni/mie`);
  }

  me(): Observable<UtenteMe> {
    return this.http.get<UtenteMe>(`${this.base}/auth/me`);
  }

  utenti(): Observable<UtenteAdmin[]> {
    return this.http.get<UtenteAdmin[]>(`${this.base}/utenti`);
  }

  aggiornaLimite(maxTorneiAttivi: number): Observable<Limite> {
    return this.http.put<Limite>(`${this.base}/impostazioni`, { maxTorneiAttivi });
  }
}