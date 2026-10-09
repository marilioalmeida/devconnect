import { formatDate } from '@angular/common';
import { LOCALE_ID, Pipe, PipeTransform, inject } from '@angular/core';
import { Clock } from './clock';

const MINUTE_MS = 60_000;
const DAY_MS = 86_400_000;

@Pipe({ name: 'relativeTime', pure: false })
export class RelativeTimePipe implements PipeTransform {
  private readonly locale = inject(LOCALE_ID);
  private readonly clock = inject(Clock);

  transform(value: string | Date): string {
    const date = typeof value === 'string' ? new Date(value) : value;
    const now = new Date(this.clock.now());

    const minutes = Math.floor((now.getTime() - date.getTime()) / MINUTE_MS);

    if (minutes < 1) {
      return 'just now';
    }

    if (minutes < 60) {
      return `${minutes} min ago`;
    }

    const hours = Math.floor(minutes / 60);

    if (hours < 24) {
      return `${hours} h ago`;
    }

    const days = this.calendarDays(date, now);

    if (days <= 1) {
      return 'yesterday';
    }

    if (days < 7) {
      return `${days} days ago`;
    }

    return formatDate(date, 'MMM d, y', this.locale);
  }

  private calendarDays(date: Date, now: Date): number {
    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startOfDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    return Math.round((startOfToday.getTime() - startOfDate.getTime()) / DAY_MS);
  }
}
