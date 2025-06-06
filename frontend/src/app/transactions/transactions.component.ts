import {HttpClient} from '@angular/common/http';
import {Component, AfterViewInit, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {MatTableModule} from '@angular/material/table';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {SelectionService} from '../tree/SelectionService';
import {DynamicFlatNode} from '../tree/DynamicFlatNode';

/**
 * @title Table retrieving data through HTTP
 */
@Component({
  selector: 'app-transactions',
  styleUrl: 'transactions.component.scss',
  templateUrl: 'transactions.component.html',
  imports: [MatProgressSpinnerModule, MatTableModule],
})
export class TransactionsComponent implements AfterViewInit {

  constructor(private selectionService: SelectionService, private http: HttpClient) {
  }

  displayedColumns: string[] = ['date', 'sequenceNumber', 'text', 'amount', 'label'];
  exampleDatabase!: ExampleHttpDatabase | null;
  data: GithubApi[] = [];

  isLoadingResults = true;
  isRateLimitReached = false;

  ngAfterViewInit() {
    this.exampleDatabase = new ExampleHttpDatabase(this.http);
    this.selectionService.selectedNode$.subscribe(node => {
      if (node) {
        this.exampleDatabase!.getRepoIssues(node)
          .subscribe(data => {
            this.data = data;
            this.isLoadingResults = false;
          });
      }
    });
  }
}

export interface GithubApi {
  id: number;
  date: string;
  sequenceNumber: number;
  text: string;
  amount: number;
  effectiveLabel: string;
}

/** An example database that the data source uses to retrieve data for the table. */
export class ExampleHttpDatabase {
  constructor(private _httpClient: HttpClient) {}

  getRepoIssues(treeNode: DynamicFlatNode): Observable<GithubApi[]> {
    const href = '/api/posting';
    const requestUrl = treeNode.id ? `${href}?treeNodeId=${treeNode.id}` : `${href}?label=${treeNode.item}`;

    return this._httpClient.get<GithubApi[]>(requestUrl);
  }
}
