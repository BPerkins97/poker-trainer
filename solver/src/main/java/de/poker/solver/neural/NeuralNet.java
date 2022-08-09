package de.poker.solver.neural;

import de.poker.solver.game.*;
import org.bytedeco.javacpp.annotation.Const;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeuralNet {
    private static final File FILE = new File("C:/Temp/test.txt");
    // The max history length is when everyone checks and at the end everyone bets 1 bb, so 100 raises + 23 actions
    private static final int MAX_HISTORY_LENGTH = 124;
    // Number of inputs are:
    // 6 - flags for the position of each player
    // 13 for first card - for value
    // 14 for second - 1 if suit matches first card
    // 15 for third - 2 for matching suits
    // 16 for four
    // 16 for fifth
    // 16 for sixth
    // 16 for seventh
    // 4 for the round in which we are in: preflop, flop, turn, river
    // 6 for the stacks of the individual players relative to the pot
    // 3 for the type of action, fold, call, raise
    // 1 for the amount of chips for the action relative to the pot
    private static final int NB_INPUTS = 127;
    private static final MultiLayerNetwork NETWORK;
    private static final List<DataPoint> TRAINING_DATA;

    static {
        if (FILE.exists()) {
            try {
                NETWORK = MultiLayerNetwork.load(FILE, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .trainingWorkspaceMode(WorkspaceMode.ENABLED).inferenceWorkspaceMode(WorkspaceMode.ENABLED)
                    .seed(123L)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new RmsProp.Builder().rmsDecay(0.95).learningRate(1e-2).build())
                    .list()
                    .layer(new LSTM.Builder().name("lstm1")
                            .activation(Activation.LEAKYRELU).nIn(NB_INPUTS).nOut(100).build())
                    .layer(new LSTM.Builder().name("lstm2")
                            .activation(Activation.LEAKYRELU).nOut(100).build())
                    .layer(new LSTM.Builder().name("lstm3")
                            .activation(Activation.LEAKYRELU).nOut(100).build())
                    .layer(new RnnOutputLayer.Builder().name("output")
                            .activation(Activation.IDENTITY).nOut(1).lossFunction(LossFunctions.LossFunction.MSE)
                            .build())
                    .build();

            NETWORK = new MultiLayerNetwork(conf);
            NETWORK.init();
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        TRAINING_DATA = new ArrayList<>();
    }

    private NeuralNet() {}

    public static void addTrainingData(HoldEmGameTree gameState, double expectedValue) {
        synchronized (TRAINING_DATA) {
            TRAINING_DATA.add(new DataPoint(gameState, expectedValue));
            if (TRAINING_DATA.size() > 100) {
                train();
            }
        }
    }

    public static void save() throws IOException {
        synchronized (NETWORK) {
            NETWORK.save(FILE);
        }
    }

    private static void train() {
        List<DataPoint> trainingSet;
        synchronized (TRAINING_DATA) {
            trainingSet = new ArrayList<>(TRAINING_DATA);
            TRAINING_DATA.clear();
        }
        int size = trainingSet.size();
        INDArray features = Nd4j.zeros(new int[]{size, NB_INPUTS, MAX_HISTORY_LENGTH}, DataType.FLOAT);
        INDArray featuresMask = Nd4j.zeros(new int[]{size, MAX_HISTORY_LENGTH}, DataType.FLOAT);
        INDArray labels = Nd4j.zeros(new int[]{size, 1, MAX_HISTORY_LENGTH}, DataType.FLOAT);
        INDArray labelsMask = Nd4j.zeros(new int[]{size, MAX_HISTORY_LENGTH}, DataType.FLOAT);

        for (int i = 0; i < size; i++) {
            HoldEmGameTree infoSet = trainingSet.get(i).gameState();
            List<HoldEmGameTree> history = infoSet.history();
            int historyLength = history.size() - 1;
            int lengthOffset = MAX_HISTORY_LENGTH - historyLength;
            int player = history.get(history.size() - 2).currentPlayer;
            for (int j = 0; j < historyLength; j++) {
                featuresMask.putScalar(new int[]{i, j + lengthOffset}, 1);
                // player position
                stateToPosition(features, i, player, j, history.get(j), history.get(j+1).actionTaken());


            }
            labelsMask.putScalar(new int[]{i, MAX_HISTORY_LENGTH - 1}, 1);
            labels.putScalar(new int[]{i, 0, 0}, trainingSet.get(i).regret());
        }

        DataSet dataSet = new DataSet(features, labels, featuresMask, labelsMask);
        synchronized (NETWORK) {
            NETWORK.fit(dataSet);
        }
    }

    public static Strategy getStrategy(HoldEmGameTree gameState) {
        Strategy strategy = new Strategy();
        strategy.actions = HoldEmGameTree.getPossibleActions(gameState);

        INDArray features = Nd4j.zeros(new int[]{strategy.actions.length, NB_INPUTS, gameState.history().size()});
        for (int j=0;j<strategy.actions.length;j++) {
            for (int i=0;i<gameState.history().size()-1;i++) {
                stateToPosition(features, j, gameState.currentPlayer, i, gameState.history().get(i), gameState.history().get(i+1).actionTaken());
            }
            stateToPosition(features, j, gameState.currentPlayer, gameState.history.size()-1, gameState.history().get(gameState.history.size()-1), strategy.actions[j]);
        }
        INDArray predictedEvs;
        synchronized (NETWORK) {
            predictedEvs = NETWORK.output(features);
        }
        strategy.expectedValues = new double[strategy.actions.length];
        for (int i=0;i<strategy.actions.length;i++) {
            strategy.expectedValues[i] = predictedEvs.getDouble(i, 0,gameState.history.size()-1);
        }
        return strategy;
    }

    private static void stateToPosition(INDArray features, int i, int player, int j, HoldEmGameTree holdEmGameTree, Action action) {
        putPlayerPosition(features, i, player, j); // 6
        putBettingRound(features, i, holdEmGameTree.bettingRound, j); // 4
        putCards(features, i, player, holdEmGameTree, j); // 106
        putPlayerStacks(features, i, j, holdEmGameTree, player); // 6
        // 122
        putAction(features, i, j, holdEmGameTree, action); // 4
        putPot(features, i, j, holdEmGameTree); // 1
    }

    private static void putAction(INDArray features, int i, int j, HoldEmGameTree holdEmGameTree, Action action) {
        features.putScalar(new int[]{i, 122+action.typeValue(), j}, 1);
        features.putScalar(new int[]{i, 125, j}, 1.0 * action.amount() / Constants.STARTING_STACK_SIZE);
    }

    private static void putPot(INDArray features, int i, int j, HoldEmGameTree state) {
        features.putScalar(new int[]{i, 126, j}, 1.0 * state.getPot() / Constants.STARTING_STACK_SIZE / Constants.NUM_PLAYERS);
    }

    private static void putPlayerStacks(INDArray features, int i, int j, HoldEmGameTree state, int player) {
        for (int k = 0; k < Constants.NUM_PLAYERS; k++) {
            features.putScalar(new int[]{i, 116+k, j}, 1.0 * state.getStack(player) / Constants.STARTING_STACK_SIZE);
        }
    }

    private static void putCards(INDArray features, int i, int player, HoldEmGameTree holdEmGameTree, int j) {
        Suit[] suits = new Suit[4];
        Card[] holeCards = holdEmGameTree.getHoleCardsFor(player);
        suits[3] = holeCards[0].suit();
        // 13 for first card
        features.putScalar(new int[]{i, 10 + holeCards[0].value().value(), j}, 1);
        // 14 for second card
        features.putScalar(new int[]{i, 23 + holeCards[1].value().value(), j}, 1);
        if (!holeCards[1].suit().equals(suits[3])) {
            features.putScalar(new int[]{i, 36, j}, 1);
            suits[0] = holeCards[1].suit();
        }

        if (holdEmGameTree.bettingRound < 1) {
            return;
        }
        // 15 for third
        Card[] communityCards = HoldEmGameTree.getCommunityCards(holdEmGameTree);
        features.putScalar(new int[]{i, 37 + communityCards[0].value().value(), j}, 1);
        if (!suits[3].equals(communityCards[0].suit())) {
            for (int k = 0; k < 2; k++) {
                if (Objects.isNull(suits[k])) {
                    suits[k] = communityCards[0].suit();
                    features.putScalar(new int[]{i, 50 + k, j}, 1);
                    break;
                }
                if (suits[k].equals(communityCards[0].suit())) {
                    features.putScalar(new int[]{i, 50 + k, j}, 1);
                }
            }
        }
        // 16 for fourth
        features.putScalar(new int[]{i, 52 + communityCards[1].value().value(), j}, 1);
        if (!suits[3].equals(communityCards[1].suit())) {
            for (int k = 0; k < 3; k++) {
                if (Objects.isNull(suits[k])) {
                    suits[k] = communityCards[1].suit();
                    features.putScalar(new int[]{i, 65 + k, j}, 1);
                    break;
                }
                if (suits[k].equals(communityCards[1].suit())) {
                    features.putScalar(new int[]{i, 65 + k, j}, 1);
                }
            }
        }
        // 16 for fifth
        features.putScalar(new int[]{i, 68 + communityCards[2].value().value(), j}, 1);
        if (!suits[3].equals(communityCards[2].suit())) {
            for (int k = 0; k < 3; k++) {
                if (Objects.isNull(suits[k])) {
                    suits[k] = communityCards[2].suit();
                    features.putScalar(new int[]{i, 81 + k, j}, 1);
                    break;
                }
                if (suits[k].equals(communityCards[2].suit())) {
                    features.putScalar(new int[]{i, 81 + k, j}, 1);
                }
            }
        }
        if (holdEmGameTree.bettingRound < 2) {
            return;
        }
        // 16 for sixth
        features.putScalar(new int[]{i, 84 + communityCards[3].value().value(), j}, 1);
        if (!suits[3].equals(communityCards[3].suit())) {
            for (int k = 0; k < 3; k++) {
                if (Objects.isNull(suits[k])) {
                    suits[k] = communityCards[3].suit();
                    features.putScalar(new int[]{i, 97 + k, j}, 1);
                    break;
                }
                if (suits[k].equals(communityCards[3].suit())) {
                    features.putScalar(new int[]{i, 97 + k, j}, 1);
                }
            }
        }
        if (holdEmGameTree.bettingRound < 3) {
            return;
        }
        // 16 for seventh
        features.putScalar(new int[]{i, 100 + communityCards[4].value().value(), j}, 1);
        if (!suits[3].equals(communityCards[4].suit())) {
            for (int k = 0; k < 3; k++) {
                if (Objects.isNull(suits[k])) {
                    suits[k] = communityCards[4].suit();
                    features.putScalar(new int[]{i, 113 + k, j}, 1);
                    break;
                }
                if (suits[k].equals(communityCards[4].suit())) {
                    features.putScalar(new int[]{i, 113 + k, j}, 1);
                }
            }
        }
    }

    private static void putBettingRound(INDArray features, int i, byte bettingRound, int j) {
        features.putScalar(new int[]{i, 6 + bettingRound, j}, 1);
    }

    private static void putPlayerPosition(INDArray features, int i, int player, int j) {
        features.putScalar(new int[]{i, 0 + player, j}, 1);
    }
}
