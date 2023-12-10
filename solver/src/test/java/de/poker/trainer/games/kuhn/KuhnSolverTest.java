package de.poker.trainer.games.kuhn;

import de.poker.trainer.solver.cfr.InMemoryNodeMap;
import de.poker.trainer.solver.cfr.NodeMap;
import de.poker.trainer.solver.cfr.VanillaCFR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KuhnSolverTest {

    public static final Map<String, double[]> SOLUTION = new HashMap<>();

    static {
        SOLUTION.put("0p", new double[]{2.0 / 3.0, 1.0 / 3.0});
        SOLUTION.put("0b", new double[]{1.0, 0});
        SOLUTION.put("1p", new double[]{1.0, 0});
        SOLUTION.put("1b", new double[]{2.0 / 3.0, 1.0 / 3.0});
        SOLUTION.put("2p", new double[]{0, 1.0});
        SOLUTION.put("2b", new double[]{0, 1.0});
    }

    @Test
    public void shouldSolveKuhnWithVanillaCFR() {
        InMemoryNodeMap<String, String> nodeMap = new InMemoryNodeMap<>();
        VanillaCFR<String, String> cfr = new VanillaCFR<>(nodeMap, new KuhnGameFactory(), 2);

        double[] expectedValues = cfr.run(500_000);
        assertEquals(Math.abs(expectedValues[0]), Math.abs(expectedValues[1]));
        assertEquals(1.0 / 18.0, expectedValues[1], 0.01);
        for(int player1Card=0;player1Card<3;player1Card++) {
            for(int player2Card=0;player2Card<3;player2Card++) {
                if (player1Card != player2Card) {
                    KuhnGame game = new KuhnGame(new int[]{player1Card, player2Card});
                    iterateOverGame(game, nodeMap, SOLUTION, 0.01);
                }
            }
        }
    }

    private void iterateOverGame(KuhnGame game, NodeMap<String, String> solved, Map<String, double[]> solution, double delta) {
        if(game.isGameOver()) {
            return;
        }
        if (solution.containsKey(game.getCurrentInfoSet())) {
            Assertions.assertArrayEquals(solved.getNode(game).getAverageStrategy(), solution.get(game.getCurrentInfoSet()), delta);
        }
        String[] actions = game.getLegalActions();
        for (String action : actions) {
            iterateOverGame(game.takeAction(action), solved, solution, delta);
        }
    }
}
