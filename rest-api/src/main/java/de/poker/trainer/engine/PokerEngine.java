package de.poker.trainer.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class PokerEngine {

    @Autowired
    private GameStateDAO gameStateDAO;

    @PostMapping("")
    public int newGame() {
        List<Player> players = Arrays.stream(Position.values())
                .map(p -> new Player(p, 100))
                .toList();
        GameState gameState = GameState.newGame(players, Card.randomlyShuffledDeck(17));
        return gameStateDAO.save(gameState);
    }

    @GetMapping("{id}")
    public GameState getGame(
            @PathVariable() int id) {
        return gameStateDAO.get(id);
    }

    @PostMapping("{id}/actions")
    public GameState takeAction(
            @PathVariable() int id,
            @RequestBody() Action action) {
        GameState gameState = gameStateDAO.get(id);
        GameState after = GameState.takeAction(gameState, action);
        gameStateDAO.update(id, after);
        return after;
    }
}
