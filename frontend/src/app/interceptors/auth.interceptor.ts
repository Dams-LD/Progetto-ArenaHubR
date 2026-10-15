import { HttpEvent, HttpInterceptorFn } from '@angular/common/http';
import { inject, NgZone } from '@angular/core';
import { from, Observable, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { SupabaseService } from '../services/supabase.service';

/**
 * Aggiunge "Authorization: Bearer <token>" alle sole chiamate verso il nostro backend.
 * Niente withCredentials: il token viaggia nell'header, non in un cookie.
 *
 * Il token arriva da una Promise di supabase-js, che si risolve fuori dalla
 * "zona" di Angular. Se la chiamata HTTP partisse da lì, anche la sua risposta
 * arriverebbe fuori zona e la pagina non si aggiornerebbe fino al clic successivo
 * (il problema del doppio clic). Per questo la chiamata viene fatta partire
 * dentro zone.run.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  const supabase = inject(SupabaseService);   // inject va chiamato qui, in modo sincrono
  const zone = inject(NgZone);

  return from(supabase.token()).pipe(
    switchMap(token => {
      const richiesta = token
        ? req.clone({ setHeaders: { Authorization: 'Bearer ' + token } })
        : req;
      return new Observable<HttpEvent<unknown>>(osservatore => {
        const sottoscrizione = zone.run(() => next(richiesta).subscribe(osservatore));
        return () => sottoscrizione.unsubscribe();
      });
    })
  );
};