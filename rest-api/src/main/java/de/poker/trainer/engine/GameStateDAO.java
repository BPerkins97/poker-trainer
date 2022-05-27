package de.poker.trainer.engine;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameStateDAO {
    private Map<Integer, GameState> gameStates = new HashMap<>();
    private int counter = 0;

    public int save(GameState gameState) {
        final int preCounter = counter;
        gameStates.put(preCounter, gameState);
        counter++;
        return preCounter;
    }

    public void update(int id, GameState gameState) {
        gameStates.put(id, gameState);
    }

    public GameState get(int id) {
        return gameStates.get(id);
    }
}
