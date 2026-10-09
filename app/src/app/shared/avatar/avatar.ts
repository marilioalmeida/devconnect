import { Component, computed, input, linkedSignal } from '@angular/core';
import { initialsOf, hueOf } from './visual-identity';

@Component({
  selector: 'app-avatar',
  imports: [],
  templateUrl: './avatar.html',
  styleUrl: './avatar.scss',
})
export class Avatar {
  readonly name = input.required<string>();
  readonly image = input<string | null>(null);
  readonly size = input(40);

  protected readonly unavailable = linkedSignal({
    source: this.image,
    computation: () => false,
  });

  protected readonly showImage = computed(() => !!this.image() && !this.unavailable());
  protected readonly initials = computed(() => initialsOf(this.name()));
  protected readonly hue = computed(() => hueOf(this.name()));
}
