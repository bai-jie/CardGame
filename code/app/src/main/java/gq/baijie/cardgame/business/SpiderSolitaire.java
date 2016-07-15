package gq.baijie.cardgame.business;

import java.util.ArrayList;
import java.util.List;

import gq.baijie.cardgame.domain.entity.Card;

public class SpiderSolitaire {

  public static class State {

    final List<CardStack> cardStacks = new ArrayList<>(10);

    /** cards for drawing, initially in five piles of ten with no cards showing */
    final List<Card> cardsForDrawing = new ArrayList<>(50);

    public static class CardStack {
      List<Card> cards = new ArrayList<>();
      /** hide: [0, openIndex), open: [openIndex, cards.size()) */
      int openIndex;
    }

  }

}
