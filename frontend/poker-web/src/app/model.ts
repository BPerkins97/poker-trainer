export interface GameState {
  players: Player[];
  holeCards: HoleCards[];
  communityCards: CommunityCards;
  actionHistory: ActionHistory;
}

export interface Player {
  position: string;
  stack: number;
}

export interface HoleCards {
  position: string;
  firstCard: Card;
  secondCard: Card;
}

export interface Card {
  suit: string;
  value: string;
}

export interface CommunityCards {
  flop: Flop;
  turn: Card;
  river: Card;
}

export interface Flop {
  firstCard: Card;
  secondCard: Card;
  thirdCard: Card;
}

export interface ActionHistory {
  preflopActions: Action[];
  flopActions: Action[];
  turnActions: Action[];
  riverActions: Action[];
}

export interface Action {
  position: string;
  type: string;
  amount: number;
}

export enum Position {
  SMALL_BLIND = "SMALL_BLIND",
  BIG_BLIND = "BIG_BLIND",
  BUTTON = "BUTTON",
  CUTOFF = "CUTOFF",
  HIJACK = "HIJACK",
  LOJACK = "LOJACK"
}
