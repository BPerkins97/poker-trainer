import { Component, OnInit, Input } from '@angular/core';
import { Card, Suits } from '../model';

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss']
})
export class CardComponent {
  @Input("card") card: Card|undefined;
  suits = Suits;

  constructor() { }
}
