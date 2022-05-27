package de.poker.trainer.engine;

import java.util.List;

public record HandResult(HandClass handClass, List<Card> cards) {

    public static int toInt(HandResult handResult) {
        return (int)(handResult.handClass.value() * Math.pow(13, 5)
                + handResult.cards().get(0).value().value() * Math.pow(13, 4)
                + handResult.cards().get(1).value().value() * Math.pow(13, 3)
                + handResult.cards().get(2).value().value() * Math.pow(13, 2)
                + handResult.cards().get(3).value().value() * Math.pow(13, 1)
                + handResult.cards().get(4).value().value());
    }
}
