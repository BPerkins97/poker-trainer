package kuhn.tensorflow;

import kuhn.cfr.Node;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class KuhnIterator {
    public int iterations;
    public int deckSize;
    public int[] cards;
    public int numBetOptions = 2;
    Trainer trainer;

    public KuhnIterator(int iterations, int deckSize) {
        trainer = new Trainer();
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
        for (int i = 0; i < iterations; i++) {
            int[] deck = randomDeck();
            cfr(deck, new ArrayList<>(), 2.0, 1, 1, false);
            trainer.trainWithBatch();
        }

        for (int i = 0; i < 100; i++) {
            int[] deck = randomDeck();
            utility[0] += cfr(deck, new ArrayList<>(), 2.0, 1, 1, true)[0];
        }
        System.out.println("Average game value: " + (utility[0] / iterations));
    }

    public double[] cfr(int[] deck, List<Integer> history, double pot, double probabilityP1, double probabilityP2, boolean debug) {
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

        double[] regrets = new double[2];
        for (int i = 0; i < numBetOptions; i++) {
            List<Integer> nextHistory = new ArrayList<>(history);
            nextHistory.add(i);
            KuhnDataSet kuhnDataSet = new KuhnDataSet();
            kuhnDataSet.player1 = actingPlayer == 0 ? 1 : 0;
            kuhnDataSet.player2 = actingPlayer == 1 ? 1 : 0;
            kuhnDataSet.holeCard = deck[actingPlayer];
            kuhnDataSet.history = nextHistory;
            regrets[i] = trainer.predict(kuhnDataSet);
        }

        double[] actionUtility = new double[numBetOptions];
        double[] strategy = getStrategy(regrets);
        KuhnDataSet[] kuhnDataSets = new KuhnDataSet[2];
        if (debug) {
            System.out.println(deck[actingPlayer] + "-" + history.stream().map(Object::toString).collect(Collectors.joining("-")) + " = " + Arrays.stream(strategy).mapToObj(d -> String.format("%.2f", d)).collect(Collectors.joining("-")));
        }
        for (int i = 0; i < numBetOptions; i++) {
            List<Integer> nextHistory = new ArrayList<>(history);
            nextHistory.add(i);
            pot += i;
            double pr1 = actingPlayer == 0 ? probabilityP1 * strategy[i] : probabilityP1;
            double pr2 = actingPlayer == 0 ? probabilityP2 : probabilityP2 * strategy[i];
            double[] r = cfr(deck, nextHistory, pot, pr1, pr2, debug);
            actionUtility[i] = r[actingPlayer];
            KuhnDataSet kuhnDataSet = new KuhnDataSet();
            kuhnDataSet.player1 = actingPlayer == 0 ? 1 : 0;
            kuhnDataSet.player2 = actingPlayer == 1 ? 1 : 0;
            kuhnDataSet.holeCard = deck[actingPlayer];
            kuhnDataSet.history = nextHistory;
            kuhnDataSet.value = r[actingPlayer];
            kuhnDataSets[i] = kuhnDataSet;

            result[0] += r[0] * strategy[i];
            result[1] += r[1] * strategy[i];
        }

        double prRegret = actingPlayer == 0 ? probabilityP2 : probabilityP1;

        for (int i=0;i<2;i++) {
            kuhnDataSets[i].value -= result[actingPlayer];
            kuhnDataSets[i].value *= prRegret;
            trainer.addTrainingSet(kuhnDataSets[i]);
        }

        return result;
    }

    private double[] getStrategy(double[] regrets) {
        double normalizingSum = 0;
        double[] strategy = new double[2];
        for (int i=0;i<2;i++) {
            strategy[i] = Math.max(regrets[i], 0);
            normalizingSum += strategy[i];
        }
        for (int i=0;i<2;i++) {
            if (normalizingSum > 0) {
                strategy[i] /= normalizingSum;
            } else {
                strategy[i] = 1.0 / 2;
            }
        }
        return strategy;
    }
}
