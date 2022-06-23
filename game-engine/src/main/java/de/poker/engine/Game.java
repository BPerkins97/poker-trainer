package de.poker.engine;

public class Game {

    public static Game withDeck(CardDeck deck) {
        return new Game();
    }

    public Player smallBlind() {
        return new Player();
    }
}
