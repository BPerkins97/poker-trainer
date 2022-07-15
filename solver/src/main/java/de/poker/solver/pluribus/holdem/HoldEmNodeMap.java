package de.poker.solver.pluribus.holdem;

import de.poker.solver.pluribus.Node;
import de.poker.solver.pluribus.NodeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class HoldEmNodeMap implements NodeMap<HoldEmGameTree, String> {
    private Map<String, Node>[][][][][] riverMap = new HashMap[HoldEmConstants.NUM_PLAYERS][HoldEmConstants.NUM_HOLE_CARD_COMBINATIONS][HoldEmConstants.NUM_FLOP_COMBINATIONS][HoldEmConstants.NUM_CARDS][HoldEmConstants.NUM_CARDS];
    private Map<String, Node>[][][][] turnMap = new HashMap[HoldEmConstants.NUM_PLAYERS][HoldEmConstants.NUM_HOLE_CARD_COMBINATIONS][HoldEmConstants.NUM_FLOP_COMBINATIONS][HoldEmConstants.NUM_CARDS];
    private Map<String, Node>[][][] flopMap = new HashMap[HoldEmConstants.NUM_PLAYERS][HoldEmConstants.NUM_HOLE_CARD_COMBINATIONS][HoldEmConstants.NUM_FLOP_COMBINATIONS];
    private Map<String, Node>[][] preFlopMap = new HashMap[HoldEmConstants.NUM_PLAYERS][HoldEmConstants.NUM_HOLE_CARD_COMBINATIONS];

    public HoldEmNodeMap() {
        System.out.println("hello");
        for (int i=0;i<HoldEmConstants.NUM_PLAYERS;i++) {
            for (int j=0;j<HoldEmConstants.NUM_HOLE_CARD_COMBINATIONS;j++) {
                for (int k=0;k<HoldEmConstants.NUM_FLOP_COMBINATIONS;k++) {
                    for (int l=0;l<HoldEmConstants.NUM_CARDS;l++) {
                        for (int m=0;m<HoldEmConstants.NUM_CARDS;m++) {
                            riverMap[i][j][k][l][m] = new HashMap<>();
                        }
                        turnMap[i][j][k][l] = new HashMap<>();
                    }
                    flopMap[i][j][k] = new HashMap<>();
                }
                preFlopMap[i][j] = new HashMap<>();
            }
        }
    }

    @Override
    public void forEach(BiConsumer<String, Node> consumer) {
        for (int i=0;i<HoldEmConstants.NUM_PLAYERS;i++) {
            for (int j=0;j<HoldEmConstants.NUM_HOLE_CARD_COMBINATIONS;j++) {
                for (int k=0;k<HoldEmConstants.NUM_FLOP_COMBINATIONS;k++) {
                    for (int l=0;l<HoldEmConstants.NUM_CARDS;l++) {
                        for (int m=0;m<HoldEmConstants.NUM_CARDS;m++) {
                            riverMap[i][j][k][l][m].forEach(consumer);
                        }
                        turnMap[i][j][k][l].forEach(consumer);
                    }
                    flopMap[i][j][k].forEach(consumer);
                }
                preFlopMap[i][j].forEach(consumer);
            }
        }
    }

    @Override
    public void updateForCurrentPlayer(HoldEmGameTree gameTree, Node node) {
        switch (gameTree.bettingRound) {
            case 0:
                updatePreFlop(gameTree, node);
                break;
            case 1:
                updateFlop(gameTree, node);
                break;
            case 2:
                updateTurn(gameTree, node);
                break;
            case 3:
                updateRiver(gameTree, node);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void updatePreFlop(HoldEmGameTree gameTree, Node node) {
        preFlopMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]].put(gameTree.history, node);
    }

    private void updateTurn(HoldEmGameTree gameTree, Node node) {
        turnMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]][gameTree.cardInfoSets[2][gameTree.currentPlayer]].put(gameTree.history, node);
    }

    private void updateFlop(HoldEmGameTree gameTree, Node node) {
        flopMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]].put(gameTree.history, node);
    }

    private void updateRiver(HoldEmGameTree gameTree, Node node) {
        riverMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]][gameTree.cardInfoSets[2][gameTree.currentPlayer]][gameTree.cardInfoSets[3][gameTree.currentPlayer]].put(gameTree.history, node);
    }

    @Override
    public Node getNodeForCurrentPlayer(HoldEmGameTree gameTree) {
        return switch (gameTree.bettingRound) {
            case 0 -> getPreFlop(gameTree);
            case 1 -> getFlop(gameTree);
            case 2 -> getTurn(gameTree);
            case 3 -> getRiver(gameTree);
            default -> throw new IllegalStateException();
        };
    }

    private Node getRiver(HoldEmGameTree gameTree) {
        if (!riverMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]][gameTree.cardInfoSets[2][gameTree.currentPlayer]][gameTree.cardInfoSets[3][gameTree.currentPlayer]].containsKey(gameTree.history)) {
            riverMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]][gameTree.cardInfoSets[2][gameTree.currentPlayer]][gameTree.cardInfoSets[3][gameTree.currentPlayer]].put(gameTree.history, new Node(gameTree.actions()));
        }
        return riverMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]][gameTree.cardInfoSets[2][gameTree.currentPlayer]][gameTree.cardInfoSets[3][gameTree.currentPlayer]].get(gameTree.history);
    }

    private Node getTurn(HoldEmGameTree gameTree) {
        if (!turnMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]][gameTree.cardInfoSets[2][gameTree.currentPlayer]].containsKey(gameTree.history)) {
            turnMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]][gameTree.cardInfoSets[2][gameTree.currentPlayer]].put(gameTree.history, new Node(gameTree.actions()));
        }
        return turnMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]][gameTree.cardInfoSets[2][gameTree.currentPlayer]].get(gameTree.history);
    }

    private Node getFlop(HoldEmGameTree gameTree) {
        if (!flopMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]].containsKey(gameTree.history)) {
            flopMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]].put(gameTree.history, new Node(gameTree.actions()));
        }
        return flopMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]][gameTree.cardInfoSets[1][gameTree.currentPlayer]].get(gameTree.history);
    }

    private Node getPreFlop(HoldEmGameTree gameTree) {
        if (!preFlopMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]].containsKey(gameTree.history)) {
            preFlopMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]].put(gameTree.history, new Node(gameTree.actions()));
        }
        return preFlopMap[gameTree.currentPlayer][gameTree.cardInfoSets[0][gameTree.currentPlayer]].get(gameTree.history);
    }

    @Override
    public void discount(double discountValue) {
        forEach((key, node) -> node.discount(discountValue));
        // TODO maybe only discount touched nones
    }
}
