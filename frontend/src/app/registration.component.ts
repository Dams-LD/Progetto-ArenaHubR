import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Output, inject, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupabaseService } from './services/supabase.service';

/**
 * Pagina di registrazione. L'account lo crea Supabase Auth; il trigger
 * handle_new_user del database crea la riga in profiles con lo username.
 * Il backend non è coinvolto.
 */
@Component({
  selector: 'app-registrati',
  standalone: true,
  imports: [CommonModule, FormsModule],
  // Angular 22 usa OnPush di default: con Eager la pagina si aggiorna
  // anche quando modifichiamo gli oggetti dopo una risposta HTTP
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistratiComponent {
  private readonly supabase = inject(SupabaseService);
  private readonly zone = inject(NgZone);
  private readonly cdr = inject(ChangeDetectorRef);

  /** Registrazione riuscita: passa lo username al componente principale. */
  @Output() registrato = new EventEmitter<string>();
  /** "Hai già un account? Accedi qui". */
  @Output() switchToLogin = new EventEmitter<void>();

  registerData = {
    gamerTag: '',
    email: '',
    password: '',
    confirmPassword: '',
  };

  errore = '';
  invioInCorso = false;

  async onRegisterSubmit(): Promise<void> {
    this.errore = '';
    const username = this.registerData.gamerTag.trim();
    const email = this.registerData.email.trim();
    const { password, confirmPassword } = this.registerData;

    // Stessi vincoli della tabella profiles: se il trigger fallisse,
    // Supabase restituirebbe solo un generico "Database error"
    if (username.length < 3 || username.length > 30) {
      this.errore = 'Il GamerTag deve avere fra 3 e 30 caratteri.';
      return;
    }
    if (!email) {
      this.errore = 'Inserisci la tua email.';
      return;
    }
    if (password.length < 8) {
      this.errore = 'La password deve avere almeno 8 caratteri.';
      return;
    }
    if (password !== confirmPassword) {
      this.errore = 'Le due password non coincidono.';
      return;
    }
    //if (!acceptTerms) {
    //  this.errore = 'Devi accettare il regolamento per proseguire.';
    //  return;
    //}

    this.invioInCorso = true;
    let data: Awaited<ReturnType<SupabaseService['registra']>>['data'] | null = null;
    let error: { message: string } | null = null;
    try {
      ({ data, error } = await this.supabase.registra(username, email, password));
    } catch (e) {
      // errore di rete o di Supabase: senza questo catch il pulsante resterebbe "in corso"
      error = { message: e instanceof Error ? e.message : String(e) };
    }

    // La risposta di Supabase arriva fuori da Angular: senza zone.run la pagina non si aggiorna
    this.zone.run(() => {
      this.invioInCorso = false;
      if (error) {
        this.errore = this.traduciErrore(error.message);
        return;
      }
      if (!data?.session) {
        // succede solo se in Supabase la conferma email è attiva
        this.errore = 'Account creato: conferma l\'indirizzo dalla email che hai ricevuto, poi accedi.';
        return;
      }
      // con la conferma email spenta Supabase ha già fatto il login
      this.registrato.emit(username);
    });
    this.cdr.detectChanges();
  }

  goLogin(): void {
    this.switchToLogin.emit();
  }

  /** Supabase risponde in inglese. */
  private traduciErrore(messaggio: string): string {
    const m = (messaggio ?? '').toLowerCase();
    if (m.includes('already registered') || m.includes('already been registered')) {
      return 'Questa email è già registrata.';
    }
    if (m.includes('database error')) {
      // il trigger non ha potuto creare il profilo: username già preso
      return 'GamerTag già in uso, scegline un altro.';
    }
    if (m.includes('password')) {
      return 'Password non valida: usa almeno 8 caratteri.';
    }
    if (m.includes('email')) {
      return 'Email non valida.';
    }
    return 'Registrazione non riuscita, riprova.';
  }
}