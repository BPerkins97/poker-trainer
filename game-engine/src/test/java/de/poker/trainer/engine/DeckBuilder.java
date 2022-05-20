package de.poker.trainer.engine;

import java.util.*;

import static de.poker.trainer.engine.CardSuit.*;
import static de.poker.trainer.engine.CardValue.*;

/**
 * This class is tightely coupled to the Game State implementation
 */
public class DeckBuilder {
    private Map<Position, List<Card>> holeCards = new HashMap<>();
    private CommunityCards communityCards = new CommunityCards(null, null, null);

    public static DeckBuilder builder() {
        return new DeckBuilder();
    }

    public static DeckBuilder random() {
        return builder()
                .smallBlind(ACE, SPADE, ACE, HEART)
                .bigBlind(KING, HEART, KING, SPADE)
                .lojack(QUEEN, SPADE, QUEEN, HEART)
                .hijack(JACK, SPADE, JACK, HEART)
                .cutoff(TEN, SPADE, TEN, HEART)
                .button(NINE, SPADE, NINE, HEART)
                .flop(EIGHT, SPADE, EIGHT, CLUB, EIGHT, HEART)
                .turn(SEVEN, SPADE)
                .river(SIX, SPADE);
    }

    public DeckBuilder holeCards(Position position, CardValue value1, CardSuit suit1, CardValue value2, CardSuit suit2) {
        this.holeCards.put(position, Arrays.asList(new Card(value1, suit1), new Card(value2, suit2)));
        return this;
    }

    public DeckBuilder smallBlind(CardValue value1, CardSuit suit1, CardValue value2, CardSuit suit2) {
        return holeCards(Position.SMALL_BLIND, value1, suit1, value2, suit2);
    }

    public DeckBuilder bigBlind(CardValue value1, CardSuit suit1, CardValue value2, CardSuit suit2) {
        return holeCards(Position.BIG_BLIND, value1, suit1, value2, suit2);
    }

    public DeckBuilder lojack(CardValue value1, CardSuit suit1, CardValue value2, CardSuit suit2) {
        return holeCards(Position.LOJACK, value1, suit1, value2, suit2);
    }

    public DeckBuilder hijack(CardValue value1, CardSuit suit1, CardValue value2, CardSuit suit2) {
        return holeCards(Position.HIJACK, value1, suit1, value2, suit2);
    }

    public DeckBuilder cutoff(CardValue value1, CardSuit suit1, CardValue value2, CardSuit suit2) {
        return holeCards(Position.CUTOFF, value1, suit1, value2, suit2);
    }

    public DeckBuilder button(CardValue value1, CardSuit suit1, CardValue value2, CardSuit suit2) {
        return holeCards(Position.BUTTON, value1, suit1, value2, suit2);
    }

    public DeckBuilder flop(CardValue value1, CardSuit suit1, CardValue value2, CardSuit suit2, CardValue value3, CardSuit suit3) {
        Flop flop = new Flop(new Card(value1, suit1), new Card(value2, suit2), new Card(value3, suit3));
        this.communityCards = new CommunityCards(flop, communityCards.turn(), communityCards.river());
        return this;
    }

    public DeckBuilder turn(CardValue value1, CardSuit suit1) {
        this.communityCards = new CommunityCards(communityCards.flop(), new Card(value1, suit1), communityCards.river());
        return this;
    }

    public DeckBuilder river(CardValue value1, CardSuit suit1) {
        this.communityCards = new CommunityCards(communityCards.flop(), communityCards.turn(), new Card(value1, suit1));
        return this;
    }

    public List<Card> build() {
        List<Card> cards = new ArrayList<>();
        cards.addAll(this.holeCards.get(Position.SMALL_BLIND));
        cards.addAll(this.holeCards.get(Position.BIG_BLIND));
        if (this.holeCards.containsKey(Position.LOJACK)) {
            cards.addAll(this.holeCards.get(Position.LOJACK));
        }
        if (this.holeCards.containsKey(Position.HIJACK)) {
            cards.addAll(this.holeCards.get(Position.HIJACK));
        }
        if (this.holeCards.containsKey(Position.CUTOFF)) {
            cards.addAll(this.holeCards.get(Position.CUTOFF));
        }
        if (this.holeCards.containsKey(Position.BUTTON)) {
            cards.addAll(this.holeCards.get(Position.BUTTON));
        }
        if (Objects.nonNull(communityCards.flop())) {
            cards.addAll(Arrays.asList(communityCards.flop().firstCard(), communityCards.flop().secondCard(), communityCards.flop().thirdCard()));
        }
        if (Objects.nonNull(communityCards.turn())) {
            cards.add(communityCards.turn());
        }
        if (Objects.nonNull(communityCards.river())) {
            cards.add(communityCards.river());
        }
        return cards;
    }

}
