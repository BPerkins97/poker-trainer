import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Action, GameState} from "./model";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class PokerApiService {

  constructor(
    private http: HttpClient
  ) { }

  public newGame() : Observable<number> {
    return this.http.post<number>("api/games", {});
  }

  public getGame(id : number) : Observable<GameState> {
    return this.http.get<GameState>("api/games/" + id);
  }

  public takeAction(id: number, action: Action) : Observable<GameState> {
    return this.http.post<GameState>("api/games/" + id, action);
  }
}
