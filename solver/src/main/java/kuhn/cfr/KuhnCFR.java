package kuhn.cfr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class KuhnCFR {
    public int numBets = 2;
    public int iterations;
    public int deckSize;
    public int[] cards;
    public int numBetOptions = 2;
    public Map<String, Node> nodes = new HashMap<>();

    public KuhnCFR(int iterations, int deckSize) {
        this.iterations = iterations;
        this.deckSize = deckSize;
        cards = new int[deckSize];
        for (int i = 0; i < deckSize; i++) {
            cards[i] = i;
        }
    }

    public int[] randomDeck() {
        int[] deck = new int[2];
        deck[0] = ThreadLocalRandom.current().nextInt(deckSize);
        do {
            deck[1] = ThreadLocalRandom.current().nextInt(deckSize);
        } while (deck[0] == deck[1]);
        return deck;
    }

    public void cfrIterationsExternal() {
        double[] utility = new double[2];
        for (int i = 1; i <= iterations; i++) {
            int[] deck = randomDeck();
            cfr(deck, new ArrayList<>(), 2.0, 1, 1);
            nodes.forEach((k,v) -> v.updateStrategy());
        }
        System.out.println("Average game value: " + (utility[0] / iterations));
    }

    public double[] cfr(int[] deck, List<Integer> history, double pot, double probabilityP1, double probabilityP2) {
        int numPlays = history.size();
        int actingPlayer = numPlays % 2;
        int opponentPlayer = (actingPlayer + 1) % 2;
        double[] result = new double[2];
        if (numPlays >= 2) {
            if (history.get(history.size() - 1) == 0 && history.get(history.size() - 2) == 1) {
                result[0] = actingPlayer == 0 ? 1 : -1;
                result[1] = actingPlayer == 1 ? 1 : -1;
                return result;
            }

            if ((history.get(history.size() - 1) == 0 && history.get(history.size() - 2) == 0) || (history.get(history.size() - 1) == 1 && history.get(history.size() - 2) == 1)) {
                double value = pot / 2;
                result[0] = deck[0] > deck[1] ? value : -value;
                result[1] = deck[0] > deck[1] ? -value : value;
                return result;
            }
        }

        String infoSet = deck[actingPlayer] + "-" + history.stream().map(Object::toString).collect(Collectors.joining("-"));
        if (!nodes.containsKey(infoSet)) {
            nodes.put(infoSet, new Node(numBetOptions));
        }
        double[] actionUtility = new double[numBetOptions];
        double[] strategy = nodes.get(infoSet).getStrategy();
        for (int i = 0; i < numBetOptions; i++) {
            List<Integer> nextHistory = new ArrayList<>(history);
            nextHistory.add(i);
            pot += i;
            double pr1 = actingPlayer == 0 ? probabilityP1 * strategy[i] : probabilityP1;
            double pr2 = actingPlayer == 0 ? probabilityP2 : probabilityP2 * strategy[i];
            double[] r = cfr(deck, nextHistory, pot, pr1, pr2);
            actionUtility[i] = r[actingPlayer];
            result[0] += r[0] * strategy[i];
            result[1] += r[1] * strategy[i];
        }

        double prReach = actingPlayer == 0 ? probabilityP1 : probabilityP2;
        double prRegret = actingPlayer == 0 ? probabilityP2 : probabilityP1;
        nodes.get(infoSet).reachProbability += prReach;
        for (int i = 0; i < numBetOptions; i++) {
            double regret = actionUtility[i] - result[actingPlayer];
            nodes.get(infoSet).regretSum[i] += regret * prRegret;
        }

        return result;
    }
}
