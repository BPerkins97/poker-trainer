package de.poker.engine;

public class Game {
    private Player smallBlind;
    private Player bigBlind;
    private Player loJack;
    private Player hiJack;
    private Player cutOff;
    private Player button;

    public Player smallBlind() {
        return smallBlind;
    }

    public Player bigBlind() {
        return bigBlind;
    }

    public Player loJack() {
        return loJack;
    }

    public Player hiJack() {
        return hiJack;
    }

    public Player cutOff() {
        return cutOff;
    }

    public Player button() {
        return button;
    }

    public static class Factory {
        private double startingStack;
        private Game game = new Game();

        public static Factory newGame() {
            return new Factory();
        }

        public Factory smallBlind(String card1, String card2) {
            game.smallBlind = Player.of(startingStack, card1, card2);
            return this;
        }
        public Factory bigBlind(String card1, String card2) {
            game.bigBlind = Player.of(startingStack, card1, card2);
            return this;
        }

        public Factory loJack(String card1, String card2) {
            game.loJack = Player.of(startingStack, card1, card2);
            return this;
        }

        public Factory hiJack(String card1, String card2) {
            game.hiJack = Player.of(startingStack, card1, card2);
            return this;
        }

        public Factory cutOff(String card1, String card2) {
            game.cutOff = Player.of(startingStack, card1, card2);
            return this;
        }

        public Factory button(String card1, String card2) {
            game.button = Player.of(startingStack, card1, card2);
            return this;
        }

        public Factory startingStacks(int startingStack) {
            this.startingStack = startingStack;
            return this;
        }

        public Game build() {
            return game;
        }
    }
}
