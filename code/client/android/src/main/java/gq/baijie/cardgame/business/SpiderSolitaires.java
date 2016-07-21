package gq.baijie.cardgame.business;

import java.util.ArrayList;
import java.util.List;

import gq.baijie.cardgame.domain.entity.Card;

public class SpiderSolitaires {

  public static List<Card> newCards(int decks, Card.Suit suit) {
    List<Card> result = new ArrayList<>(decks * 52);
    for (Card.Rank rank : Card.Rank.values()) {
      for (int i = 0; i < 4 * decks; i++) {
        result.add(new Card(suit, rank));
      }
    }
    return result;
  }

  public static List<Card> random(List<Card> cards) {
    List<Card> copy = new ArrayList<>(cards);
    List<Card> result = new ArrayList<>(cards.size());
    while (!copy.isEmpty()) {
      final int location = (int) (Math.random() * copy.size());
      result.add(copy.remove(location));
    }
    return result;
  }

  public static SpiderSolitaire.State newGame() {
    List<Card> cards = newCards(2, Card.Suit.HEART);
    cards = random(cards);

    SpiderSolitaire.State result = new SpiderSolitaire.State();
    for (int i = 0; i < 54; i++) {
      result.cardStacks.get(i%10).cards.add(cards.remove(cards.size()-1));
    }
    for(SpiderSolitaire.State.CardStack stack : result.cardStacks) {
      stack.openIndex = stack.cards.size() - 1;
    }
    result.cardsForDrawing.addAll(cards);
    return result;
  }

  public static SpiderSolitaire.State getSampleSpiderSolitaireState() {
    SpiderSolitaire.State result = newGame();
    for(SpiderSolitaire.State.CardStack stack : result.cardStacks) {
      stack.openIndex = 0;
    }
    return result;
  }

}
