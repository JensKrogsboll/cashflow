import {TreeComponent} from "../tree/tree.component";
import {Component} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatSidenavModule} from '@angular/material/sidenav';
import {TransactionsComponent} from '../transactions/transactions.component';
import {TagManagerComponent} from '../tag-manager/tag-manager.component';
import {MatTabsModule} from '@angular/material/tabs';

@Component({
  selector: 'app-main',
    imports: [
        TreeComponent, TransactionsComponent, MatButtonModule, MatSidenavModule, MatTabsModule
    ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent {
}
