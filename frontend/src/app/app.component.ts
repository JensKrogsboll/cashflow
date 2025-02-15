import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {TreeComponent} from "./tree/tree.component";

@Component({
  selector: 'app-root',
  imports: [TreeComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';
}
