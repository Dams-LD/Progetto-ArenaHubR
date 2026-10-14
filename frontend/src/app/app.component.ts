import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface Tournament {
  id: number;
  title: string;
  category: 'FPS' | 'SPORT' | 'ARCADE';
  description: string;
  date: string;
  time: string;
  dateTime: string;
  reward: string;
  maxSpots: number;
  availableSpots: number;
  isFinished: boolean;
  isEnrolled: boolean;
  image: string;
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
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  currentTab: 'tornei' | 'calendario' | 'miei-tornei' = 'tornei';

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
    avatar: 'assets/img/avatar1.png'
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

  tournaments: Tournament[] = [
    {
      id: 1,
      title: 'EA SPORTS FC 26 Cup',
      category: 'SPORT',
      description: 'Torneo 1v1 Ultimate Team sul nuovo simulatore calcistico.',
      date: '2026-10-18',
      time: '15:00',
      dateTime: '18 Ottobre 2026 - 15:00',
      reward: 'Trofeo ITS + Maglia Ufficiale',
      maxSpots: 32,
      availableSpots: 4,
      isFinished: false,
      isEnrolled: true,
      image: 'fc.jpg'
    },
    {
      id: 2,
      title: 'F1 26 Virtual Grand Prix',
      category: 'SPORT',
      description: 'Gara di velocità e strategia sul circuito di Monza.',
      date: '2026-10-20',
      time: '21:00',
      dateTime: '20 Ottobre 2026 - 21:00',
      reward: 'Set di Pneumatici Virtuali + Skin Auto Esclusiva',
      maxSpots: 20,
      availableSpots: 2,
      isFinished: false,
      isEnrolled: false,
      image: 'f1.jpg'
    },
    {
      id: 3,
      title: 'Fortnite Victory Royale',
      category: 'FPS',
      description: 'Battle Royale Zero Build in modalità Singolo.',
      date: '2026-10-22',
      time: '17:00',
      dateTime: '22 Ottobre 2026 - 17:00',
      reward: 'V-Bucks + Skin Esclusiva',
      maxSpots: 100,
      availableSpots: 18,
      isFinished: false,
      isEnrolled: false,
      image: 'fortnite.jpg'
    },
    {
      id: 4,
      title: 'Call of Duty Warzone Resurgence',
      category: 'FPS',
      description: 'Torneo Trios su mappa Ritorno ad alta intensità.',
      date: '2026-10-24',
      time: '18:30',
      dateTime: '24 Ottobre 2026 - 18:30',
      reward: 'Skin Esclusiva',
      maxSpots: 24,
      availableSpots: 0,
      isFinished: true,
      isEnrolled: false,
      image: 'warzone.jpg'
    },
    {
      id: 5,
      title: 'Rocket League 3v3 Arena',
      category: 'SPORT',
      description: 'Campionato a squadre 3v3 con tabellone ad eliminazione.',
      date: '2026-10-25',
      time: '16:00',
      dateTime: '25 Ottobre 2026 - 16:00',
      reward: 'Badge eSports',
      maxSpots: 16,
      availableSpots: 5,
      isFinished: false,
      isEnrolled: true,
      image: 'rocket.jpg'
    },
    {
      id: 6,
      title: 'Mortal Kombat 1 Kolosseum',
      category: 'ARCADE',
      description: 'Sfida 1v1 picchiaduro ad eliminazione diretta.',
      date: '2026-10-27',
      time: '20:30',
      dateTime: '27 Ottobre 2026 - 20:30',
      reward: 'Arcade Stick Custom ITS',
      maxSpots: 16,
      availableSpots: 7,
      isFinished: false,
      isEnrolled: false,
      image: 'mortal.jpg'
    },
    {
      id: 7,
      title: 'UFC 5 Octagon Showdown',
      category: 'ARCADE',
      description: 'Incontri MMA virtuali per conquistare la cintura di campione.',
      date: '2026-10-28',
      time: '19:00',
      dateTime: '28 Ottobre 2026 - 19:00',
      reward: 'Cintura Campione ITS Arena',
      maxSpots: 16,
      availableSpots: 3,
      isFinished: false,
      isEnrolled: false,
      image: 'ufc.jpg'
    },
    {
      id: 8,
      title: 'PUBG Survival Battle',
      category: 'FPS',
      description: 'Survival 50 giocatori: l\'ultimo guerriero vince la coppa.',
      date: '2026-10-29',
      time: '18:00',
      dateTime: '29 Ottobre 2026 - 18:00',
      reward: 'Mouse Gaming Wireless',
      maxSpots: 50,
      availableSpots: 12,
      isFinished: false,
      isEnrolled: false,
      image: 'pubg.jpg'
    },
    {
      id: 9,
      title: 'Valorant Open League',
      category: 'FPS',
      description: 'Torneo 5v5 tattico sui server ufficiali della competizione.',
      date: '2026-10-30',
      time: '17:30',
      dateTime: '30 Ottobre 2026 - 17:30',
      reward: 'Badge Riconoscimento',
      maxSpots: 16,
      availableSpots: 1,
      isFinished: false,
      isEnrolled: false,
      image: 'valorant.jpg'
    }
  ];

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
      const dateStr = `${pDate.getFullYear()}-${monthFormatted}-${dayFormatted}`;
      daysArray.push({ dayNumber: pDay, dateStr, isCurrentMonth: false, isToday: false, events: [] });
    }

    for (let day = 1; day <= totalDays; day++) {
      const monthFormatted = String(month + 1).padStart(2, '0');
      const dayFormatted = String(day).padStart(2, '0');
      const dateStr = `${year}-${monthFormatted}-${dayFormatted}`;

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
      const dateStr = `${nDate.getFullYear()}-${monthFormatted}-${dayFormatted}`;
      daysArray.push({ dayNumber: i, dateStr, isCurrentMonth: false, isToday: false, events: [] });
    }

    return daysArray;
  }

  getCapacityPercent(t: Tournament): number {
    if (!t.maxSpots) return 0;
    return Math.round(((t.maxSpots - t.availableSpots) / t.maxSpots) * 100);
  }

  enrollUser(t: Tournament): void {
    if (t.availableSpots > 0 && !t.isEnrolled) {
      t.isEnrolled = true;
      t.availableSpots--;
      this.addToast(`Iscrizione a "${t.title}" completata con successo!`, 'success');
    }
  }

  unenrollUser(t: Tournament): void {
    if (t.isEnrolled) {
      t.isEnrolled = false;
      t.availableSpots++;
      this.addToast(`Cancellata iscrizione a "${t.title}".`, 'info');
    }
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
      case 'ARCADE': return 'fa-solid fa-hand-fist';
      default: return 'fa-solid fa-trophy';
    }
  }

  openDetailsModal(t: Tournament): void {
    this.selectedTournament = t;
    this.activeModal = 'details';
    this.modalTab = 'overview';
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
    if (img && !img.src.endsWith('placeholder.jpg')) {
      img.src = 'assets/img/placeholder.jpg';
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