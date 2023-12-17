package de.poker.trainer.games.nlhe;

public record Card(CardRank rank, CardSuit suit) {

    public int value() {
        return rank.value() * 4 + suit.value();
    }

    @Override
    public String toString() {
        return rank.representation + suit.representation;
    }
}
