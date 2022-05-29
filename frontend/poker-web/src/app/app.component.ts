import {Component, OnInit} from '@angular/core';
import {PokerApiService} from "./poker-api.service";
import {GameState, Position, Actions} from "./model";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  positions = Position;
  id: number|undefined;
  game: GameState|undefined;
  title = 'poker-web';

  constructor(
    private pokerAPI: PokerApiService
  ) {
  }

  ngOnInit(): void {
    this.pokerAPI.newGame().subscribe(r => this.id = r);
  }

  reload(): void {
    if (this.id) {
      this.pokerAPI.getGame(this.id).subscribe(r => this.game = r);
    }
  }

  player(position: Position) {
    return this.game?.players
      .find(p => p.position == position);
  }

  holeCards(position: Position) {
    return this.game?.holeCards
      .find(p => p.position == position);
  }

  fold(position: Position) {
  console.log(position);
      if (this.id != undefined) {
      console.log("takeAction");
      this.pokerAPI.takeAction(this.id, {
              position: position,
              type: Actions.FOLD,
              amount: 0
          });
      }

  }
}
