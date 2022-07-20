package de.poker.solver.map;

import de.poker.solver.game.Action;
import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;

import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;

// TODO It probably is best to do this in a relational database
// TODO this can be more efficient
public class HoldEmNodeMap {
    Map<String, Map<String, ActionMap>>[][] map = new HashMap[Constants.NUM_BETTING_ROUNDS][Constants.NUM_PLAYERS];
    private Connection connection;

    public HoldEmNodeMap() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost/poker_trainer?" +
                "user=PokerTrainer&password=password");
    }

    public void forEach(BiConsumer<String, ActionMap> consumer) {
        for (int i = 0; i < Constants.NUM_BETTING_ROUNDS; i++) {
            for (int j = 0; j < Constants.NUM_PLAYERS; j++) {
                map[i][j].forEach((k, v) -> v.forEach(consumer));
            }
        }
    }

    public void updateForCurrentPlayer(HoldEmGameTree gameTree, ActionMap node) {
        if (node.infosetId < 0) {
            StringBuilder stringBuilder = new StringBuilder("INSERT INTO INFOSET(BETTING_ROUND, PLAYER, CARDS, HISTORY) VALUES (")
                    .append(gameTree.bettingRound)
                    .append(", ").append(gameTree.currentPlayer)
                    .append(", '").append(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer])
                    .append("', '").append(gameTree.history()).append("')");
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString());
                preparedStatement.executeUpdate();
                stringBuilder = new StringBuilder("SELECT INFOSET_ID FROM INFOSET WHERE " +
                        "BETTING_ROUND = ").append(gameTree.bettingRound)
                        .append(" AND PLAYER = ").append(gameTree.currentPlayer)
                        .append(" AND CARDS = '").append(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer])
                        .append("' AND HISTORY = '").append(gameTree.history()).append("'");
                preparedStatement = connection.prepareStatement(stringBuilder.toString());
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                if (resultSet.next()) {
                    int infoSetId = resultSet.getInt(1);
                    node.infosetId = infoSetId;
                } else {
                    throw new IllegalStateException();
                    // Something went wrong
                }
                stringBuilder = new StringBuilder("INSERT INTO NODES (INFOSET_ID, ACTION_ID, REGRET, AVERAGE_ACTION) VALUES ");
                Set<Map.Entry<Action, Node>> entries = node.map.entrySet();
                Iterator<Map.Entry<Action, Node>> iterator = entries.iterator();
                for (int i=0;i<entries.size()-1;i++) {
                    Map.Entry<Action, Node> next = iterator.next();
                    stringBuilder.append("(")
                            .append(node.infosetId).append(",'")
                            .append(next.getKey().presentation()).append("',")
                            .append(next.getValue().regret).append(",")
                            .append(next.getValue().averageAction)
                            .append("), ");
                }
                Map.Entry<Action, Node> next = iterator.next();
                stringBuilder.append("(")
                        .append(node.infosetId).append(",'")
                        .append(next.getKey().presentation()).append("',")
                        .append(next.getValue().regret).append(",")
                        .append(next.getValue().averageAction)
                        .append(")");
                preparedStatement = connection.prepareStatement(stringBuilder.toString());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            Iterator<Map.Entry<Action, Node>> iterator = node.map.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<Action, Node> next = iterator.next();
                StringBuilder sb = new StringBuilder("UPDATE NODES SET  REGRET = REGRET + ").append(next.getValue().regret)
                        .append(", AVERAGE_ACTION = AVERAGE_ACTION +").append(next.getValue().averageAction)
                        .append(" WHERE INFOSET_ID = ").append(node.infosetId)
                        .append(" AND ACTION_ID = '").append(next.getKey().presentation()).append("'");
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public ActionMap getNodeForCurrentPlayer(HoldEmGameTree gameTree) {
        StringBuilder stringBuilder = new StringBuilder("SELECT n.ACTION_ID, n.REGRET, n.INFOSET_ID FROM INFOSET info JOIN NODES n on n.INFOSET_ID = info.INFOSET_ID AND info.BETTING_ROUND = ")
                .append(gameTree.bettingRound)
                .append(" AND info.PLAYER = ").append(gameTree.currentPlayer)
                .append(" AND info.CARDS = '").append(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer]);
        if (gameTree.history().isEmpty()) {
            stringBuilder.append("'");
        } else {
            stringBuilder.append("' AND info.HISTORY = '").append(gameTree.history()).append("'");
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            ActionMap actionMap = new ActionMap();
            if (!resultSet.next()) {
                return actionMap;
            }
            actionMap.infosetId = resultSet.getInt(3);
            do {
                resultSet.getRow();
                String action = resultSet.getString(1);
                int regret = resultSet.getInt(2);
                Node value = new Node();
                value.regret = regret;
                actionMap.map.put(Action.of(action), value);
            } while (resultSet.next());
            return actionMap;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void discount(double discountValue) {
        forEach((key, node) -> node.discount(discountValue));
        // TODO maybe only discount touched nones
    }
}
