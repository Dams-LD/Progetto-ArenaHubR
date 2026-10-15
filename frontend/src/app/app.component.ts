import { Component, inject, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TorneoApiService } from './services/torneo-api.service';
import { SupabaseService } from './services/supabase.service';
import { Session } from '@supabase/supabase-js';
import { toTournament } from './adapters/torneo.adapter';
import { StatoTorneo, UtenteAdmin } from './models/torneo.model';
import { RegistratiComponent } from './registration.component';



/**
 * Modello usato dal template. Viene riempito da toTournament() a partire
 * dal JSON del backend: i nomi inglesi restano solo qui dentro.
 */
export interface Tournament {
  id: number;
  title: string;          // <- nome
  gioco: string;
  category: string;       // <- categoria (arriva da GET /api/categorie, non più fissa)
  description: string;
  regole?: string;        // solo dopo GET /api/tornei/{id}
  date: string;
  time: string;
  dateTime: string;
  reward?: string;        // NON esiste nel contratto: da togliere dal template
  maxSpots: number;       // <- postiMax
  availableSpots: number; // <- postiMax - iscritti
  stato: StatoTorneo;
  isFinished: boolean;    // <- stato !== 'aperto'
  isEnrolled: boolean;    // <- iscritto
  image: string;          // <- ricavata da gioco
}


export interface CalendarDay {
  dayNumber: number;
  dateStr: string;
  isCurrentMonth: boolean;
  isToday: boolean;
  events: Tournament[];
}


export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'danger' | 'info';
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RegistratiComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})


export class AppComponent implements OnInit {
  private readonly api = inject(TorneoApiService);
  private readonly supabase = inject(SupabaseService);
  /** Le risposte di Supabase arrivano fuori da Angular: zone.run forza l'aggiornamento della vista. */
  private readonly zone = inject(NgZone);

  // --- sessione (Supabase Auth) ---
  isLoggedIn = false;
  /** Resta false finché il backend non espone GET /api/auth/me. */
  isAdmin = false;
  showLoginModal = false;
  loginData = { email: '', password: '' };
  loginInCorso = false;

  // --- change request: limite tornei attivi ---
  maxTournamentsLimit = 0;              // arriva da GET /api/auth/me
  utenti: UtenteAdmin[] = [];           // pagina admin "Utenti"
  nuovoLimite = 3;                      // campo del form admin
  messaggioBlocco: string | null = null; // pop-up quando l'iscrizione viene rifiutata


  categorie: string[] = [];
  caricamento = false;


  currentTab: 'tornei' | 'calendario' | 'miei-tornei' | 'registrati' | 'utenti' = 'tornei';


  searchQuery: string = '';
  selectedCategory: string = 'ALL';
  selectedStatus: string = 'ALL';


  currentCalendarDate: Date = new Date(2026, 9, 1);
  calFilter: 'ALL' | 'MINE' | 'OPEN' = 'ALL';


  activeModal: 'details' | null = null;
  selectedTournament: Tournament | null = null;
  showAccountModal: boolean = false;
  modalTab: 'overview' | 'rules' | 'rewards' | 'ranking' = 'overview';


  user = {
    name: 'Marco_Rossi',
    avatar: 'controller.svg'
  };


  avatarOptions: string[] = [
    'caffe.svg',
    'cerchio.svg',
    'controller.svg',
    'cuffie.svg',
    'mouse.svg',
    'tastiera.svg'
  ];


  toasts: Toast[] = [];


  tournaments: Tournament[] = [];


  ngOnInit(): void {
    // Ripristina la sessione salvata da Supabase (ricaricando la pagina resti loggato)
    // e ascolta login/logout/rinnovo del token.
    this.supabase.client.auth.getSession().then(({ data }) =>
      this.zone.run(() => this.applicaSessione(data.session, false)));
    this.supabase.onCambioSessione(session =>
      this.zone.run(() => this.applicaSessione(session, true)));

    this.caricaTornei();
    this.api.categorie().subscribe({
      next: c => this.categorie = c,
      error: e => this.mostraErrore(e)
    });
  }


  caricaTornei(): void {
    this.caricamento = true;
    this.api.lista().subscribe({
      next: lista => {
        this.tournaments = lista.map(toTournament);
        this.caricamento = false;
      },
      error: e => {
        this.caricamento = false;
        this.mostraErrore(e);
      }
    });
  }


  get activeTournamentsCount(): number {
    return this.tournaments.filter(t => !t.isFinished && t.availableSpots > 0).length;
  }


  get myTournaments(): Tournament[] {
    return this.tournaments.filter(t => t.isEnrolled);
  }


  get filteredTournaments(): Tournament[] {
    return this.tournaments.filter(t => {
      const matchQuery = !this.searchQuery ||
        t.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        t.description.toLowerCase().includes(this.searchQuery.toLowerCase());


      const matchCat = this.selectedCategory === 'ALL' || t.category === this.selectedCategory;


      let matchStatus = true;
      if (this.selectedStatus === 'OPEN') matchStatus = !t.isFinished;
      if (this.selectedStatus === 'FINISHED') matchStatus = t.isFinished;


      return matchQuery && matchCat && matchStatus;
    });
  }


  get currentMonthLabel(): string {
    const options: Intl.DateTimeFormatOptions = { month: 'long', year: 'numeric' };
    const str = this.currentCalendarDate.toLocaleDateString('it-IT', options);
    return str.charAt(0).toUpperCase() + str.slice(1);
  }


  get isCurrentMonthToday(): boolean {
    const now = new Date();
    return this.currentCalendarDate.getMonth() === now.getMonth() &&
      this.currentCalendarDate.getFullYear() === now.getFullYear();
  }


  get filteredCalendarTournaments(): Tournament[] {
    return this.tournaments.filter(t => {
      if (this.calFilter === 'MINE') return t.isEnrolled;
      if (this.calFilter === 'OPEN') return !t.isFinished && t.availableSpots > 0;
      return true;
    });
  }


  get monthTournamentsList(): Tournament[] {
    const year = this.currentCalendarDate.getFullYear();
    const month = this.currentCalendarDate.getMonth();
    return this.tournaments.filter(t => {
      if (!t.date) return false;
      const [tYear, tMonth] = t.date.split('-').map(Number);
      return tYear === year && (tMonth - 1) === month;
    });
  }


  get monthEventsCount(): number {
    return this.monthTournamentsList.length;
  }


  get monthEnrolledCount(): number {
    return this.monthTournamentsList.filter(t => t.isEnrolled).length;
  }


  get monthOpenSpotsCount(): number {
    return this.monthTournamentsList.reduce((acc, t) => acc + (t.availableSpots || 0), 0);
  }


  get calendarGridDays(): CalendarDay[] {
    const year = this.currentCalendarDate.getFullYear();
    const month = this.currentCalendarDate.getMonth();
    const todayStr = new Date().toISOString().split('T')[0];


    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);


    let startingDay = firstDayOfMonth.getDay() - 1;
    if (startingDay === -1) startingDay = 6;


    const totalDays = lastDayOfMonth.getDate();
    const daysArray: CalendarDay[] = [];


    const prevMonthLastDay = new Date(year, month, 0).getDate();
    for (let i = startingDay - 1; i >= 0; i--) {
      const pDay = prevMonthLastDay - i;
      const pDate = new Date(year, month - 1, pDay);
      const monthFormatted = String(pDate.getMonth() + 1).padStart(2, '0');
      const dayFormatted = String(pDay).padStart(2, '0');
      const dateStr = `pDate.getFullYear()-{monthFormatted}-${dayFormatted}`;
      daysArray.push({ dayNumber: pDay, dateStr, isCurrentMonth: false, isToday: false, events: [] });
    }


    for (let day = 1; day <= totalDays; day++) {
      const monthFormatted = String(month + 1).padStart(2, '0');
      const dayFormatted = String(day).padStart(2, '0');
      const dateStr = `year-{monthFormatted}-${dayFormatted}`;


      const dayEvents = this.filteredCalendarTournaments.filter(t => t.date === dateStr);


      daysArray.push({
        dayNumber: day,
        dateStr: dateStr,
        isCurrentMonth: true,
        isToday: dateStr === todayStr,
        events: dayEvents
      });
    }


    const totalCells = daysArray.length > 35 ? 42 : 35;
    const remainingCells = totalCells - daysArray.length;
    for (let i = 1; i <= remainingCells; i++) {
      const nDate = new Date(year, month + 1, i);
      const monthFormatted = String(nDate.getMonth() + 1).padStart(2, '0');
      const dayFormatted = String(i).padStart(2, '0');
      const dateStr = `nDate.getFullYear()-{monthFormatted}-${dayFormatted}`;
      daysArray.push({ dayNumber: i, dateStr, isCurrentMonth: false, isToday: false, events: [] });
    }


    return daysArray;
  }


  getCapacityPercent(t: Tournament): number {
    if (!t.maxSpots) return 0;
    return Math.round(((t.maxSpots - t.availableSpots) / t.maxSpots) * 100);
  }


  enrollUser(t: Tournament): void {
    if (!this.isLoggedIn) {
      this.showLoginModal = true;
      return;
    }
    // ISCRIVITI resta cliccabile anche oltre il limite: decide il backend
    if (t.isFinished || t.isEnrolled || t.availableSpots === 0) return;
    this.api.iscriviti(t.id).subscribe({
      next: () => {
        // aggiorniamo l'oggetto solo DOPO la conferma del backend
        t.isEnrolled = true;
        t.availableSpots--;
        this.addToast(`Iscrizione a "${t.title}" completata con successo!`, 'success');
      },
      error: e => {
        // 422 = regola violata (limite raggiunto, torneo pieno o chiuso): pop-up
        if (e.status === 422) {
          this.messaggioBlocco = e.error?.errore ?? 'Iscrizione non consentita';
        } else {
          this.mostraErrore(e);
        }
      }
    });
  }


  unenrollUser(t: Tournament): void {
    if (!t.isEnrolled || t.isFinished) return;
    this.api.annulla(t.id).subscribe({
      next: () => {
        t.isEnrolled = false;
        t.availableSpots++;
        this.addToast(`Cancellata iscrizione a "${t.title}".`, 'info');
      },
      error: e => this.mostraErrore(e)
    });
  }


  /** Legge il messaggio {"errore": "..."} del contratto e lo mostra in un toast. */
  mostraErrore(e: HttpErrorResponse): void {
    if (e.status === 0) {
      this.addToast('Backend non raggiungibile: è avviato sulla 8080?', 'danger');
      return;
    }
    const messaggio = e.error?.errore ?? `Errore imprevisto (${e.status})`;
    this.addToast(messaggio, 'danger');
    if (e.status === 401) {
      this.showLoginModal = true;
    }
  }


  // --- autenticazione ---

  async onLoginSubmit(): Promise<void> {
    const { email, password } = this.loginData;
    if (!email || !password) {
      this.addToast('Inserisci email e password', 'danger');
      return;
    }
    const { error } = await this.supabase.login(email, password);
    this.zone.run(() => {
      if (error) {
        this.addToast('Email o password non corretti', 'danger');
        return;
      }
      this.showLoginModal = false;
      this.loginData = { email: '', password: '' };
    });
  }

  async logout(): Promise<void> {
    await this.supabase.logout();
    this.zone.run(() => {
      this.showAccountModal = false;
      this.currentTab = 'tornei';
      this.addToast('Disconnesso', 'info');
    });
  }

  // --- registrazione (componente app-registrati) ---

  /** Il componente di registrazione ha creato l'account: Supabase ha già fatto il login. */
  onRegistrato(username: string): void {
    this.currentTab = 'tornei';
    this.addToast(`Benvenuto ${username}, registrazione completata!`, 'success');
  }

  /** "Hai già un account? Accedi qui" dal form di registrazione. */
  apriLoginDaRegistrazione(): void {
    this.currentTab = 'tornei';
    this.showLoginModal = true;
  }


  /**
   * Unico punto che aggiorna lo stato di login.
   * ricarica = true quando la sessione cambia mentre la pagina è aperta:
   * il campo "iscritto" dei tornei dipende da chi è loggato.
   */
  private applicaSessione(session: Session | null, ricarica: boolean): void {
    const eraLoggato = this.isLoggedIn;
    this.isLoggedIn = session !== null;
    if (session && !eraLoggato) {
      this.caricaMe();
    }
    if (session) {
      const meta = session.user.user_metadata ?? {};
      this.user = { ...this.user, name: meta['username'] ?? session.user.email ?? 'Utente' };
    } else {
      this.isAdmin = false;
    }
    // il rinnovo del token non cambia l'utente: non serve ricaricare
    if (ricarica && eraLoggato !== this.isLoggedIn) {
      this.caricaTornei();
    }
  }

  // --- change request: limite tornei attivi ---

  /** Tornei dell'utente che contano per il limite: aperti o chiusi, non conclusi. */
  get mieiTorneiAttivi(): number {
    return this.tournaments.filter(t => t.isEnrolled && t.stato !== 'concluso').length;
  }

  /** Ruolo e limite. Se fallisce l'utente resta un utente normale. */
  private caricaMe(): void {
    this.api.me().subscribe({
      next: me => {
        this.isAdmin = me.ruolo === 'ADMIN';
        this.maxTournamentsLimit = me.limiteTorneiAttivi;
        this.nuovoLimite = me.limiteTorneiAttivi;
        this.user = { ...this.user, name: me.username };
      },
      error: () => this.isAdmin = false
    });
  }

  apriUtenti(): void {
    this.currentTab = 'utenti';
    this.api.utenti().subscribe({
      next: lista => this.utenti = lista,
      error: e => this.mostraErrore(e)
    });
  }

  salvaLimite(): void {
    const n = Number(this.nuovoLimite);
    if (!Number.isInteger(n) || n < 1 || n > 999) {
      this.addToast('Il limite deve essere un numero intero fra 1 e 999', 'danger');
      return;
    }
    this.api.aggiornaLimite(n).subscribe({
      next: l => {
        this.maxTournamentsLimit = l.maxTorneiAttivi;
        this.addToast(`Limite aggiornato a ${l.maxTorneiAttivi} tornei attivi`, 'success');
        this.apriUtenti();   // ricalcola chi è sopra il limite
      },
      error: e => this.mostraErrore(e)
    });
  }

  /** Da usare nel form admin di modifica torneo, prima di salvare lo stato. */
  confermaCambioStato(nuovoStato: string): boolean {
    return nuovoStato !== 'concluso'
      || confirm('Attenzione: un torneo concluso non può tornare aperto. Continuare?');
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.selectedCategory = 'ALL';
    this.selectedStatus = 'ALL';
  }


  changeMonth(step: number): void {
    const newDate = new Date(this.currentCalendarDate);
    newDate.setMonth(newDate.getMonth() + step);
    this.currentCalendarDate = newDate;
  }


  goToToday(): void {
    this.currentCalendarDate = new Date();
  }


  getCategoryIcon(cat: string): string {
    switch (cat) {
      case 'FPS': return 'fa-solid fa-crosshairs';
      case 'SPORT': return 'fa-solid fa-gamepad';
      case 'PICCHIADURO': return 'fa-solid fa-hand-fist';
      case 'BATTLE ROYALE': return 'fa-solid fa-parachute-box';
      default: return 'fa-solid fa-trophy';
    }
  }


  openDetailsModal(t: Tournament): void {
    this.selectedTournament = t;
    this.activeModal = 'details';
    this.modalTab = 'overview';
    // la lista non porta le regole: le chiediamo al dettaglio (contratto, punto 8)
    if (t.regole === undefined) {
      this.api.dettaglio(t.id).subscribe({
        next: d => t.regole = d.regole ?? '',
        error: e => this.mostraErrore(e)
      });
    }
  }


  closeModal(): void {
    this.activeModal = null;
    this.selectedTournament = null;
  }


  saveAccount(): void {
    this.showAccountModal = false;
    this.addToast('Profilo giocatore aggiornato!', 'success');
  }


  handleImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    if (img && !img.src.endsWith('icona.jpg')) {
      img.src = 'icona.jpg';   // assets/img/placeholder.jpg non esiste nel progetto
    }
  }


  addToast(message: string, type: 'success' | 'danger' | 'info' = 'info'): void {
    const id = Date.now();
    this.toasts.push({ id, message, type });
    setTimeout(() => {
      this.toasts = this.toasts.filter(toast => toast.id !== id);
    }, 3500);
  }


  getToastIcon(type: string): string {
    switch (type) {
      case 'success': return 'fa-circle-check text-green';
      case 'danger': return 'fa-circle-xmark text-red';
      default: return 'fa-circle-info text-blue';
    }
  }
}
