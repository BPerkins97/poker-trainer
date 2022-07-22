package de.poker.solver.map;

import de.poker.solver.game.Action;
import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

// TODO this can be more efficient
public class HoldEmNodeMap {
    Map<String, Map<String, ActionMap>>[][] map = new Map[Constants.NUM_BETTING_ROUNDS][Constants.NUM_PLAYERS];

    public HoldEmNodeMap() {
        for (int i = 0; i< Constants.NUM_BETTING_ROUNDS; i++) {
            for (int j = 0; j< Constants.NUM_PLAYERS; j++) {
                map[i][j] = new HashMap<>();
            }
        }
    }

    public void forEach(BiConsumer<String, ActionMap> consumer) {
        for (int i = 0; i< Constants.NUM_BETTING_ROUNDS; i++) {
            for (int j = 0; j< Constants.NUM_PLAYERS; j++) {
                map[i][j].forEach((k, v) -> v.forEach(consumer));
            }
        }
    }

    public void updateForCurrentPlayer(HoldEmGameTree gameTree, ActionMap node) {
        return; // TODO solange wir in Memory arbeiten, können wir das getrost ignorieren, da wir immer mit Referenzen arbeiten
    }

    public ActionMap getNodeForCurrentPlayer(HoldEmGameTree gameTree) {
        Map<String, ActionMap> tempMap = map[gameTree.bettingRound][gameTree.currentPlayer].get(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer]);
        if (Objects.isNull(tempMap)) {
            tempMap = new HashMap<>();
            map[gameTree.bettingRound][gameTree.currentPlayer].put(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer], tempMap);
        }

        ActionMap node = tempMap.get(gameTree.history());
        if  (Objects.isNull(node)) {
            node = new ActionMap();
            tempMap.put(gameTree.history(), node);
        }

        return node;
    }

    public void discount(double discountValue) {
        forEach((key, node) -> node.discount(discountValue));
    }

    public void saveToFile(File file) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (int i=0;i<Constants.NUM_BETTING_ROUNDS;i++) {
            for (int j=0;j<Constants.NUM_PLAYERS;j++) {
                for (Map.Entry<String, Map<String, ActionMap>> entry1 : map[i][j].entrySet()) {
                    for (Map.Entry<String, ActionMap> entry2 : entry1.getValue().entrySet()) {
                        for (Map.Entry<Action, Node> entry3 : entry2.getValue().map.entrySet()) {
                            String output = i + ";" + j + ";" + entry1.getKey() + ";" + entry2.getKey() + ";" + entry3.getKey().presentation() + ";" + entry3.getValue().regret + ";" + entry3.getValue().averageAction + "\r\n";
                            bufferedWriter.write(output);
                        }
                    }
                }
            }
        }
        bufferedWriter.close();
    }

    public void loadFromFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while (!Objects.isNull(line = reader.readLine())) {
            if (line.isEmpty()) {
                System.out.println("encountered empty line");
                continue;
            }
            String[] parts = line.split(";");
            int bettingRound = Integer.parseInt(parts[0]);
            int player = Integer.parseInt(parts[1]);
            String cards = parts[2];
            String history = parts[3];
            String myAction = parts[4];
            int regret = Integer.parseInt(parts[5]);
            int averageAction = Integer.parseInt(parts[6]);
            Map<String, ActionMap> stringActionMapMap = map[bettingRound][player].get(cards);
            if (Objects.isNull(stringActionMapMap)) {
                stringActionMapMap = new HashMap<>();
                map[bettingRound][player].put(cards, stringActionMapMap);
            }
            ActionMap actionMap = stringActionMapMap.get(history);
            if (Objects.isNull(actionMap)) {
                actionMap = new ActionMap();
                stringActionMapMap.put(history, actionMap);
            }
            Node value = new Node();
            value.regret = regret;
            value.averageAction = averageAction;
            actionMap.map.put(Action.of(myAction), value);
        }
        reader.close();
    }
}
