package de.poker.trainer.engine;

import java.util.ArrayList;
import java.util.List;

public class PlayerBuilder {
    private int stackSize;


    public static PlayerBuilder builder() {
        return new PlayerBuilder();
    }

    public PlayerBuilder stackSize(int stackSize) {
        this.stackSize = stackSize;
        return this;
    }

    public List<Player> build() {
        List<Player> players = new ArrayList<>();
        players.add(new Player(Position.SMALL_BLIND, stackSize));
        players.add(new Player(Position.BIG_BLIND, stackSize));
        players.add(new Player(Position.LOJACK, stackSize));
        players.add(new Player(Position.HIJACK, stackSize));
        players.add(new Player(Position.CUTOFF, stackSize));
        players.add(new Player(Position.BUTTON, stackSize));
        return players;
    }
}
