package de.poker.solver.database;

import de.poker.solver.game.Action;
import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.ActionMap;
import de.poker.solver.map.Node;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DAO {
    private final Connection connection;

    public DAO() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/poker";
        String user = "root";
        String password = "password";
        connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
    }

    public void updateNodes(NodeMap nodeMap) throws SQLException {
        CallableStatement callableStatement = connection.prepareCall("CALL INSERT_OR_UPDATE_NODES(?,?,?,?,?,?,?)");
        for (Map.Entry<InfoSet, ActionMap> entry : nodeMap.map.entrySet()) {
            for (Map.Entry<Action, Node> nodeEntry : entry.getValue().getMap().entrySet()) {
                if (regretHasntChanged(nodeEntry)) {
                    continue;
                }
                callableStatement.setByte(1, entry.getKey().player());
                callableStatement.setLong(2, entry.getKey().cards());
                callableStatement.setString(3, entry.getKey().history());
                callableStatement.setByte(4, nodeEntry.getKey().type());
                callableStatement.setShort(5, (short)nodeEntry.getKey().amount());
                callableStatement.setInt(6, nodeEntry.getValue().getRegretChange());
                callableStatement.setShort(7, (short) nodeEntry.getValue().getAverageAction());
                callableStatement.addBatch();
            }
        }
        callableStatement.executeBatch();
        connection.commit();
    }

    private boolean regretHasntChanged(Map.Entry<Action, Node> nodeEntry) {
        return nodeEntry.getValue().getRegretChange() == 0;
    }

    public NodeMap getNodes(HoldEmGameTree gameTree) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder("SELECT PLAYER, CARDS, HISTORY, ACTION_ID, AMOUNT, REGRET FROM NODE WHERE (");

        for (int i = 0; i < Constants.NUM_PLAYERS - 1; i++) {
            stringBuilder.append("(");
            playerConditional(gameTree, stringBuilder, i);
            stringBuilder.append(") OR ");
        }

        stringBuilder.append("(");
        playerConditional(gameTree, stringBuilder, Constants.NUM_PLAYERS - 1);
        stringBuilder.append("))");


        PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString());
        preparedStatement.executeQuery();
        ResultSet resultSet = preparedStatement.getResultSet();
        Map<InfoSet, ActionMap> map = new HashMap<>();
        while (resultSet.next()) {
            byte playerId = resultSet.getByte("PLAYER");
            long cards = resultSet.getLong("CARDS");
            String history = resultSet.getString("HISTORY");
            InfoSet infoSet = new InfoSet(playerId, cards, history);

            Action action = Action.of(resultSet.getByte("ACTION_ID"), resultSet.getInt("AMOUNT"));
            ActionMap actionMap = map.get(infoSet);
            if (Objects.isNull(actionMap)) {
                actionMap = new ActionMap();
                map.put(infoSet, actionMap);
            }
            Node node = new Node(resultSet.getInt("REGRET"), 0);
            actionMap.addAction(action, node);
        }
        return new NodeMap(map);
    }

    private void playerConditional(HoldEmGameTree gameTree, StringBuilder stringBuilder, int player) {
        stringBuilder.append("PLAYER = ").append(player).append(" AND (CARDS = ").append(gameTree.cardInfoSet(0, player))
                .append(" OR CARDS = ").append(gameTree.cardInfoSet(1, player))
                .append(" OR CARDS = ").append(gameTree.cardInfoSet(2, player))
                .append(" OR CARDS = ").append(gameTree.cardInfoSet(3, player)).append(")");
    }
}
