import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { SupabaseService } from '../services/supabase.service';

/**
 * Aggiunge "Authorization: Bearer <token>" alle sole chiamate verso il nostro backend.
 * Niente withCredentials: il token viaggia nell'header, non in un cookie.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  const supabase = inject(SupabaseService);   // va chiamato in modo sincrono, qui
  return from(supabase.token()).pipe(
    switchMap(token =>
      next(token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req)
    )
  );
};

