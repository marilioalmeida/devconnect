import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Avatar } from '../avatar/avatar';

@Component({
  selector: 'app-user-row',
  imports: [RouterLink, Avatar],
  templateUrl: './user-row.html',
  styleUrl: './user-row.scss',
})
export class UserRow {
  readonly userId = input.required<number>();
  readonly name = input.required<string>();
  readonly nickname = input<string | null>(null);
  readonly image = input<string | null>(null);
  readonly detail = input<string | null>(null);
  readonly avatarSize = input(44);
}
