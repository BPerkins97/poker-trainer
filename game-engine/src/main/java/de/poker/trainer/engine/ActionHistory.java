package de.poker.trainer.engine;

import java.util.List;

public record ActionHistory(List<Action> preflopActions, List<Action> flopActions, List<Action> turnActions, List<Action> riverActions) {
}
