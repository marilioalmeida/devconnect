import { Component, DestroyRef, effect, inject, input, signal } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Notifier } from '../../core/notification/notifier';
import { highlight } from './syntax-highlight';

@Component({
  selector: 'app-code-block',
  imports: [MatButtonModule, MatIconModule],
  templateUrl: './code-block.html',
  styleUrl: './code-block.scss',
})
export class CodeBlock {
  readonly code = input.required<string>();
  readonly language = input<string>('');

  private readonly sanitizer = inject(DomSanitizer);
  private readonly notifier = inject(Notifier);

  protected readonly highlighted = signal<SafeHtml | null>(null);
  protected readonly copied = signal(false);
  protected readonly canCopy = typeof navigator !== 'undefined' && !!navigator.clipboard;

  private timer: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    effect(async (onCleanup) => {
      const code = this.code();
      const language = this.language();

      let active = true;
      onCleanup(() => (active = false));

      this.highlighted.set(null);

      const html = await highlight(code, language);

      if (active && html !== null) {
        this.highlighted.set(this.sanitizer.bypassSecurityTrustHtml(html));
      }
    });

    inject(DestroyRef).onDestroy(() => {
      if (this.timer !== null) {
        clearTimeout(this.timer);
      }
    });
  }

  protected async copy(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.code());
    } catch {
      this.notifier.error('Could not copy. Select the text manually.');
      return;
    }

    this.copied.set(true);
    this.timer = setTimeout(() => this.copied.set(false), 1500);
  }
}
