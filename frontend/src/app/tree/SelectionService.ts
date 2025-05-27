// selection.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import {DynamicFlatNode} from './DynamicFlatNode';

@Injectable({ providedIn: 'root' })
export class SelectionService {
  private selectedNodeSubject = new BehaviorSubject<DynamicFlatNode | null>(null);
  selectedNode$ = this.selectedNodeSubject.asObservable();

  selectNode(node: DynamicFlatNode) {
    this.selectedNodeSubject.next(node);
  }

  getSelectedNode(): DynamicFlatNode | null {
    return this.selectedNodeSubject.getValue();
  }
}
