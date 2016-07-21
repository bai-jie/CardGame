package gq.baijie.cardgame.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gq.baijie.cardgame.domain.entity.Card;

public class SpiderSolitaire {

  private final State state;

  public SpiderSolitaire(State state) {
    this.state = state;
  }

  public State getState() {
    return state;
  }

  public boolean canMove(CardPosition from, CardPosition to) {
    // * can move naturally
    if (!state.canMove(from, to)) {
      return false;
    }
    // * from is sequential //TODO check card's Suit
    State.CardStack cardStackOfFrom = state.cardStacks.get(from.cardStackIndex);
    Card lastCard = cardStackOfFrom.cards.get(from.cardIndex);
    for (int i = from.cardIndex + 1; i < cardStackOfFrom.cards.size(); i++) {
      final Card currentCard = cardStackOfFrom.cards.get(i);
      if (lastCard.getRank().getId() != currentCard.getRank().getId() + 1) {
        return false;
      }
      lastCard = currentCard;
    }
    // * after moved is sequential that is:
    //   - case 1: to is empty card stack
    //   - case 2:'s card + 1 == to's last card
    //TODO check card's Suit
    if (!state.cardStacks.get(to.cardStackIndex).cards.isEmpty() && (
        state.getCard(from).getRank().getId() + 1 !=
        state.cardStacks.get(to.cardStackIndex).cards.get(to.cardIndex - 1).getRank().getId()
    )) {
      return false;
    }
    return true;
  }

  public void move(CardPosition from, CardPosition to) {
    if (!canMove(from, to)) {
      throw new IllegalArgumentException();
    }
    state.move(from, to);
  }

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

    public boolean isLegalCardStackIndex(int cardStackIndex) {
      return cardStackIndex >= 0 && cardStackIndex < cardStacks.size();
    }

    public boolean hasCard(CardPosition position) {
      int stackIndex = position.cardStackIndex;
      int cardIndex = position.cardIndex;
      return isLegalCardStackIndex(stackIndex)
             && cardIndex >= 0 && cardIndex < cardStacks.get(stackIndex).cards.size();
    }

    public Card getCard(CardPosition position) {
      if (!hasCard(position)) {
        return null;
      }
      return cardStacks.get(position.cardStackIndex).cards.get(position.cardIndex);
    }

    boolean canMove(CardPosition from, CardPosition to) {
      // * from has card
      if (!hasCard(from)) {
        return false;
      }
      // * to's cardIndex is the biggest index in card stack + 1 (which is cardStack.cards.size())
      if (!isLegalCardStackIndex(to.cardStackIndex)
          || to.cardIndex != cardStacks.get(to.cardStackIndex).cards.size()) {
        return false;
      }
      return true;
    }

    void move(CardPosition from, CardPosition to) {
      if (!canMove(from, to)) {
        throw new IllegalArgumentException();
      }
      final List<Card> src = cardStacks.get(from.cardStackIndex).cards;
      final List<Card> dest = cardStacks.get(to.cardStackIndex).cards;
      while (src.size() > from.cardIndex) {
        dest.add(src.remove(from.cardIndex));
      }
    }

    public static class CardStack {

      public List<Card> cards = new ArrayList<>();
      /** hide: [0, openIndex), open: [openIndex, cards.size()) */
      int openIndex;
    }

  }

  public static class CardPosition {

    public int cardStackIndex;
    /** card index in card stack */
    public int cardIndex;

    public CardPosition() {
    }

    public CardPosition(int cardStackIndex, int cardIndex) {
      this.cardStackIndex = cardStackIndex;
      this.cardIndex = cardIndex;
    }

    public static CardPosition of(int cardStackIndex, int cardIndex) {
      return new CardPosition(cardStackIndex, cardIndex);
    }
  }

}
