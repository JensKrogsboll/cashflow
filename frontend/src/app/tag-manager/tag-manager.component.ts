import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatIconButton} from '@angular/material/button';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-tag-manager',
  templateUrl: './tag-manager.component.html',
  styleUrls: ['./tag-manager.component.scss'],
  imports: [
    MatFormFieldModule, MatIconModule, MatIconButton, MatInputModule, NgIf
  ]
})
export class TagManagerComponent {
  @Input() tag: string | null = null;

  @Output() tagChange = new EventEmitter<string | null>();

  onTagInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const newTag = input.value.trim();
    this.tagChange.emit(newTag || null);
  }

  clearTag(): void {
    this.tag = null;
    this.tagChange.emit(null);
  }
}
