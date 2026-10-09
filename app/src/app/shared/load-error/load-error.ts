import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-load-error',
  imports: [MatButtonModule, MatCardModule, MatIconModule],
  templateUrl: './load-error.html',
  styleUrl: './load-error.scss',
})
export class LoadError {
  readonly message = input('Could not load the data.');
  readonly retry = output<void>();
}
