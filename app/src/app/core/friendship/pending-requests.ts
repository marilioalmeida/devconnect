import { Service, computed, inject } from '@angular/core';
import { FriendshipApi } from './friendship-api';

@Service()
export class PendingRequests {
  readonly resource = inject(FriendshipApi).receivedRequests();

  readonly total = computed(() => (this.resource.hasValue() ? this.resource.value().length : 0));
}
