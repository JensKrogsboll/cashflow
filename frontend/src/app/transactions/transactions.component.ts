import {HttpClient} from '@angular/common/http';
import {Component, AfterViewInit, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {MatTableModule} from '@angular/material/table';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {SelectionService} from '../tree/SelectionService';

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

  displayedColumns: string[] = ['date', 'sequenceNumber', 'text', 'amount'];
  exampleDatabase!: ExampleHttpDatabase | null;
  data: GithubApi[] = [];

  isLoadingResults = true;
  isRateLimitReached = false;

  ngAfterViewInit() {
    this.exampleDatabase = new ExampleHttpDatabase(this.http);
    this.selectionService.selectedNodeId$.subscribe(nodeId => {
      if (nodeId) {
        this.exampleDatabase!.getRepoIssues(nodeId)
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
  amount: number
}

/** An example database that the data source uses to retrieve data for the table. */
export class ExampleHttpDatabase {
  constructor(private _httpClient: HttpClient) {}

  getRepoIssues(treeNodeId: string): Observable<GithubApi[]> {
    const href = '/api/posting';
    const requestUrl = `${href}?treeNodeId=${treeNodeId}`;

    return this._httpClient.get<GithubApi[]>(requestUrl);
  }
}
