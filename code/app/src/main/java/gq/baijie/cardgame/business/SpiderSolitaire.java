package gq.baijie.cardgame.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gq.baijie.cardgame.domain.entity.Card;

public class SpiderSolitaire {

  public static class State {

    /** left to right, 0 to 9 */
    public final List<CardStack> cardStacks;

    /** cards for drawing, initially in five piles of ten with no cards showing */
    public final List<Card> cardsForDrawing = new ArrayList<>(50);

    public State() {
      List<CardStack> cardStacks = new ArrayList<>(10);
      for (int i = 0; i < 10; i++) {
        cardStacks.add(new CardStack());
      }
      this.cardStacks = Collections.unmodifiableList(cardStacks);
    }

    public static class CardStack {
      public List<Card> cards = new ArrayList<>();
      /** hide: [0, openIndex), open: [openIndex, cards.size()) */
      int openIndex;
    }

  }

}
