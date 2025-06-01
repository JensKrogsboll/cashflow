import {AfterViewInit, Component, ElementRef, OnInit, QueryList, ViewChildren} from '@angular/core';
import {MatTableModule} from '@angular/material/table';
import {SelectionService} from '../tree/SelectionService';
import {HttpClient} from '@angular/common/http';
import {DynamicFlatNode} from '../tree/DynamicFlatNode';
import {Observable} from 'rxjs';
import {ExampleHttpDatabase, GithubApi} from '../transactions/transactions.component';
import {NgClass, NgFor} from '@angular/common';

@Component({
  selector: 'app-expenses',
  templateUrl: './expenses.component.html',
  styleUrls: ['./expenses.component.scss'],
  imports: [ MatTableModule, NgFor, NgClass ]
})
export class ExpensesComponent implements AfterViewInit {

  @ViewChildren('categoryRow', { read: ElementRef }) rows!: QueryList<ElementRef>;

  constructor(private selectionService: SelectionService, private http: HttpClient) {
  }

  months: string[] = [];
  categories: string[] = [];
  displayedColumns: string[] = [];
  flashCategory: string | null = null;
  highlightedCategory: string | null = null;

  edb!: ExpensesHttpDatabase | null;
  data: ExpenseData = {};

  ngAfterViewInit(): void {
    this.edb = new ExpensesHttpDatabase(this.http);
    this.selectionService.selectedNode$.subscribe(node => {
      if (node) {
        this.edb!.getReport()
          .subscribe(data => {
            this.data = data;
            this.categories = Object.keys(this.data);
            const allMonths = new Set<string>();
            this.categories.forEach(category => {
              const entries = this.data[category] || {};
              Object.keys(entries).forEach(month => allMonths.add(month));
            });
            this.months = Array.from(allMonths).sort();
            this.displayedColumns = ['category', ...this.months];
          });
        // Scroll after a slight delay to allow DOM update
        setTimeout(() => {
          const el = this.rows.find(ref =>
            ref.nativeElement.getAttribute('data-category') === node.item
          );
          el?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
          this.flashCategory = node.effectiveLabel ? node.effectiveLabel : node.item;
          setTimeout(() => this.flashCategory = null, 3000); // clear after animation
        }, 10);
        this.highlightedCategory = node.effectiveLabel ? node.effectiveLabel : node.item;
      }
    });

  }

  shouldFlash(category: string): boolean {
    return this.flashCategory === category;
  }

  getValue(category: string, month: string): string {
    const value = this.data[category]?.[month];
    return value === null || value === undefined
      ? '-'
      : this.formatNumber(value);
  }

  private formatNumber(value: number): string {
    return value.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }

  isNegative(category: string, month: string): boolean {
    const value = this.data[category]?.[month];
    return typeof value === 'number' && value < 0;
  }


  shouldHighlight(category : string): boolean {
    return this.highlightedCategory === category;
  }
}

export interface ExpenseData {
  [category: string]: {
    [month: string]: number | null;
  };
}

export class ExpensesHttpDatabase {
  constructor(private _httpClient: HttpClient) {}

  getReport(): Observable<ExpenseData> {
    const href = '/api/spendings/detailed';
    return this._httpClient.get<ExpenseData>(href);
  }
}
