package algorithms.vanillacfr;

public interface GameFactory<ACTION, INFOSET> {
    Game<ACTION, INFOSET> generate();
}
