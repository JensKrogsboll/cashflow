import {signal} from '@angular/core';

/** Flat node with expandable and level information */
export class DynamicFlatNode {
  constructor(
    public id: bigint,
    public item: string,
    public label: string,
    public effectiveLabel: string,
    public level = 1,
    public expandable = false,
    public isLoading = signal(false),
  ) {}
}
