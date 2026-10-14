import { Injectable } from '@angular/core';
import { createClient, Session, SupabaseClient } from '@supabase/supabase-js';
import { environment } from '../../environments/environment';


/**
 * Unico punto di contatto con Supabase Auth.
 * Login, registrazione e logout NON passano dal nostro backend.
 */
@Injectable({ providedIn: 'root' })
export class SupabaseService {


  readonly client: SupabaseClient = createClient(
    environment.supabaseUrl,
    environment.supabaseAnonKey
  );


  /** Token corrente. supabase-js lo rinnova da solo prima della scadenza. */
  async token(): Promise<string | null> {
    const { data } = await this.client.auth.getSession();
    return data.session?.access_token ?? null;
  }


  login(email: string, password: string) {
    return this.client.auth.signInWithPassword({ email, password });
  }


  /** Lo username finisce in raw_user_meta_data: il trigger lo copia in profiles. */
  registra(username: string, email: string, password: string) {
    return this.client.auth.signUp({ email, password, options: { data: { username } } });
  }


  logout() {
    return this.client.auth.signOut();
  }


  onCambioSessione(callback: (session: Session | null) => void) {
    return this.client.auth.onAuthStateChange((_evento, session) => callback(session));
  }
}





