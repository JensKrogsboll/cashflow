// selection.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SelectionService {
  private selectedNodeIdSubject = new BehaviorSubject<string | null>(null);
  selectedNodeId$ = this.selectedNodeIdSubject.asObservable();

  selectNode(id: bigint) {
    this.selectedNodeIdSubject.next("" + id);
  }

  getSelectedNodeId(): string | null {
    return this.selectedNodeIdSubject.getValue();
  }
}
