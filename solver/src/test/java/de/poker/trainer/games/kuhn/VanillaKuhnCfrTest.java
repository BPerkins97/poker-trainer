package de.poker.trainer.games.kuhn;

import de.poker.trainer.solver.vanillacfr.InMemoryNodeMap;
import de.poker.trainer.solver.vanillacfr.NodeMap;
import de.poker.trainer.solver.vanillacfr.VanillaCFR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VanillaKuhnCfrTest {

    @Test
    public void shouldSolveKuhn() {
        InMemoryNodeMap<String, String> nodeMap = new InMemoryNodeMap<>();
        VanillaCFR<String, String> cfr = new VanillaCFR<>(nodeMap, new KuhnGameFactory(), 2);

        double[] expectedValues = cfr.run(250_000);
        assertEquals(Math.abs(expectedValues[0]), Math.abs(expectedValues[1]));
        assertEquals(1.0 / 18.0, expectedValues[1], 0.01);
        Map<String, double[]> solution = new HashMap<>();
        solution.put("0p", new double[]{2.0 / 3.0, 1.0 / 3.0});
        solution.put("0b", new double[]{1.0, 0});
        solution.put("1p", new double[]{1.0, 0});
        solution.put("1b", new double[]{2.0 / 3.0, 1.0 / 3.0});
        solution.put("2p", new double[]{0, 1.0});
        solution.put("2b", new double[]{0, 1.0});
        for(int player1Card=0;player1Card<3;player1Card++) {
            for(int player2Card=0;player2Card<3;player2Card++) {
                if (player1Card != player2Card) {
                    KuhnGame game = new KuhnGame(new int[]{player1Card, player2Card});
                    iterateOverGame(game, nodeMap, solution);
                }
            }
        }
    }

    private void iterateOverGame(KuhnGame game, NodeMap<String, String> solved, Map<String, double[]> solution) {
        if(game.isGameOver()) {
            return;
        }
        if (solution.containsKey(game.getCurrentInfoSet())) {
            Assertions.assertArrayEquals(solved.getNode(game).getAverageStrategy(), solution.get(game.getCurrentInfoSet()), 0.01);
        }
        String[] actions = game.getLegalActions();
        for (String action : actions) {
            iterateOverGame(game.takeAction(action), solved, solution);
        }
    }
}
