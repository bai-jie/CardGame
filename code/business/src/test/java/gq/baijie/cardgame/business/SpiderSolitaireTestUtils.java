package gq.baijie.cardgame.business;

import gq.baijie.cardgame.domain.entity.Card;

public class SpiderSolitaireTestUtils {

  public static SpiderSolitaire newEmptyGame() {
    SpiderSolitaire.State state = new SpiderSolitaire.State();
    return new SpiderSolitaire(state, new RamEventLogger<>());
  }

  public static Card newCard(int id) {
    return new Card(Card.Suit.HEART, Card.Rank.fromId(id));
  }

}
