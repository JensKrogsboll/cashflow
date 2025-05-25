import {CollectionViewer, SelectionChange, DataSource} from '@angular/cdk/collections';
import {FlatTreeControl} from '@angular/cdk/tree';
import {ChangeDetectionStrategy, Component, Injectable, inject, signal} from '@angular/core';
import {BehaviorSubject, merge, Observable, publish} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTreeModule} from '@angular/material/tree';
import {HttpClient} from '@angular/common/http';
import {SelectionService} from './SelectionService';
import {NgFor, NgIf} from '@angular/common';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatAutocompleteModule, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';

/** Flat node with expandable and level information */
export class DynamicFlatNode {
  constructor(
      public id: bigint,
      public item: string,
      public label: string,
      public level = 1,
      public expandable = false,
      public isLoading = signal(false),
  ) {}
}

export class Label {
  constructor(public id: bigint, public name: string) {
  }
}

export class TreeNode {
  constructor(
    public id: bigint,
    public name: string,
    public label: Label,
    public effectiveLabel: Label
  ) {}
}

/**
 * Database for dynamic data. Fetches tree structure from REST API.
 */
@Injectable({providedIn: 'root'})
export class DynamicDatabase {
  private apiUrl = '/api/tree'; // Replace with your API endpoint

  constructor(private http: HttpClient) {}


  /** Fetch root nodes */
  getRootNodes(): Observable<TreeNode[]> {
    return this.http.get<TreeNode[]>(`${this.apiUrl}`);
  }

  /** Fetch child nodes */
  getChildren(node: bigint): Observable<TreeNode[]> {
    return this.http.get<TreeNode[]>(`${this.apiUrl}?parentId=${node}`);
  }

  /** Check if node is expandable */
  isExpandable(node: TreeNode): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${node}`);
  }
}

/**
 * DataSource for the tree component, dynamically fetching data from REST API.
 */
export class DynamicDataSource implements DataSource<DynamicFlatNode> {
  dataChange = new BehaviorSubject<DynamicFlatNode[]>([]);

  get data(): DynamicFlatNode[] {
    return this.dataChange.value;
  }
  set data(value: DynamicFlatNode[]) {
    this._treeControl.dataNodes = value;
    this.dataChange.next(value);
  }

  constructor(
      private _treeControl: FlatTreeControl<DynamicFlatNode>,
      private _database: DynamicDatabase,
      private selectionService: SelectionService
  ) {}

  connect(collectionViewer: CollectionViewer): Observable<DynamicFlatNode[]> {
    this._treeControl.expansionModel.changed.subscribe(change => {
      if ((change as SelectionChange<DynamicFlatNode>).added ||
          (change as SelectionChange<DynamicFlatNode>).removed) {
        this.handleTreeControl(change as SelectionChange<DynamicFlatNode>);
      }
    });

    return merge(collectionViewer.viewChange, this.dataChange).pipe(map(() => this.data));
  }

  disconnect(collectionViewer: CollectionViewer): void {}

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<DynamicFlatNode>) {
    if (change.added) {
      change.added.forEach(node => this.toggleNode(node, true));
    }
    if (change.removed) {
      change.removed.slice().reverse().forEach(node => this.toggleNode(node, false));
    }
  }

  /**
   * Toggle the node, fetching children from API.
   */
  toggleNode(node: DynamicFlatNode, expand: boolean) {
    const index = this.data.indexOf(node);
    if (index < 0) return;

    node.isLoading.set(true);

    if (expand) {
      this._database.getChildren(node.id).pipe(
          tap(() => node.isLoading.set(false))
      ).subscribe(children => {
        const nodes = children.map(name => {
          const label = name.effectiveLabel == null ? "" : name.effectiveLabel.name;
          return new DynamicFlatNode(name.id, name.name, label, node.level + 1, true);
        });
        this.data.splice(index + 1, 0, ...nodes);
        this.dataChange.next(this.data);
      });
      this.selectionService.selectNode(node.id);
    } else {
      let count = 0;
      for (let i = index + 1; i < this.data.length && this.data[i].level > node.level; i++, count++) {}
      this.data.splice(index + 1, count);
      this.dataChange.next(this.data);
      node.isLoading.set(false);
      this.selectionService.selectNode(node.id);
    }
  }
}

/**
 * Tree component with dynamic data.
 */
@Component({
  selector: 'app-tree',
  templateUrl: 'tree.component.html',
  styleUrl: 'tree.component.scss',
  imports: [MatTreeModule, MatButtonModule, MatIconModule, MatProgressBarModule, NgIf, MatAutocompleteModule, MatFormFieldModule,
    MatInputModule, ReactiveFormsModule, NgFor],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TreeComponent {
  constructor(private selectionService: SelectionService, private http: HttpClient) {
    const database = inject(DynamicDatabase);

    this.treeControl = new FlatTreeControl<DynamicFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new DynamicDataSource(this.treeControl, database, selectionService);

    // Fetch root nodes from API
    database.getRootNodes().subscribe(rootNodes => {
      this.dataSource.data = rootNodes.map(name => {
        const label = name.effectiveLabel == null ? "" : name.effectiveLabel.name;
        return new DynamicFlatNode(name.id, name.name, label, 0, true)
      });
    });
  }

  treeControl: FlatTreeControl<DynamicFlatNode>;
  dataSource: DynamicDataSource;
  labelOptions: string[] = [];

  getLevel = (node: DynamicFlatNode) => node.level;
  isExpandable = (node: DynamicFlatNode) => node.expandable;
  hasChild = (_: number, _nodeData: DynamicFlatNode) => _nodeData.expandable;

  fetchLabels(): void {
    this.http.get<string[]>('/api/label').subscribe(options => {
      this.labelOptions = options;
    });
  }

  editLabel(node: any): void {
    node.editingLabel = true;
    node.labelControl = new FormControl(node.label || '');

    // Optional: refresh label list on edit
    this.fetchLabels();
  }

  onLabelSelected(node: any, event: MatAutocompleteSelectedEvent): void {
    const selectedLabel = event.option.value;
    console.log("SELECTED LABEL: " + selectedLabel);
    this.applyLabel(node, selectedLabel);
  }

  saveLabel(node: any): void {
    const enteredLabel = node.labelControl.value?.trim();
    this.applyLabel(node, enteredLabel);
  }

  applyLabel(node: any, label: string | null): void {
    node.label = label;
    node.editingLabel = false;

    if (!node.id || !label) return;

    const encoded = encodeURIComponent(label);
    this.http.post(`/api/tree/${node.id}/label/${encoded}`, {}).subscribe({
      next: () => console.log(`Label "${label}" applied to node ${node.id}`),
      error: (err) => console.error(`Failed to set label:`, err)
    });
  }

}
