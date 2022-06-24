package de.poker.engine;

public class Action {
    private final Type type;
    private final Player.Position position;
    private final double amount;

    private Action(Type type, Player.Position position, double amount) {
        this.type = type;
        this.position = position;
        this.amount = amount;
    }

    private Action(Type type, Player.Position position) {
        this(type, position, 0);
    }

    public static Action fold(Player.Position position) {
        return new Action(Type.FOLD, position);
    }

    public Player.Position position() {
        return position;
    }

    public Action.Type type() {
        return type;
    }

    public static Action check(Player.Position position) {
        return new Action(Type.CHECK, position);
    }

    public static enum Type {
        FOLD,
        CHECK
    }
}
