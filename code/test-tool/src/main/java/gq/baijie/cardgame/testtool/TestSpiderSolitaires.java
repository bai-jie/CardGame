package gq.baijie.cardgame.testtool;

import java.util.ArrayList;
import java.util.List;

import gq.baijie.cardgame.business.RamEventLogger;
import gq.baijie.cardgame.business.SpiderSolitaire;
import gq.baijie.cardgame.domain.entity.Card;

import static gq.baijie.cardgame.business.SpiderSolitaires.random;

public class TestSpiderSolitaires {

  public static Card newCard(int id) {
    return new Card(Card.Suit.HEART, Card.Rank.fromId(id));
  }


  public static void setCardStack(
      SpiderSolitaire.State.CardStack cardStack, int[] cards) {
    setCardStack(cardStack, cards, 0);
  }

  public static void setCardStack(
      SpiderSolitaire.State.CardStack cardStack, int[] cards, int openIndex) {
    cardStack.cards.clear();
    for (int cardId: cards) {
      cardStack.cards.add(newCard(cardId));
    }
    cardStack.setOpenIndex(openIndex);
  }

  public static List<Card> newCards(int[] cards) {
    List<Card> result = new ArrayList<>(cards.length);
    for (int cardId : cards) {
      result.add(newCard(cardId));
    }
    return result;
  }

  public static SpiderSolitaire newEmptyGame() {
    SpiderSolitaire.State state = new SpiderSolitaire.State();
    return new SpiderSolitaire(state, new RamEventLogger<>());
  }

  public static SpiderSolitaire newTestSortedOutGame() {
    SpiderSolitaire result = newEmptyGame();
    for (int i = 0; i < 10; i++) {// i:(0..9), card:(1..10)
      result.getState().cardStacks.get(i).cards.add(newCard(i + 1));
    }
    setCardStack(result.getState().cardStacks.get(1), new int[]{5, 4, 3, 2, 1});
    setCardStack(result.getState().cardStacks.get(2), new int[]{13, 12, 11, 10, 9, 8, 7, 6});
    setCardStack(result.getState().cardStacks.get(5), new int[]{7, 2, 13, 12, 11, 10, 9, 8, 7, 6}, 2);
    result.getState().cardsForDrawing.addAll(random(newCards(new int[]{1,2,3,4,5,6,7,8,9,10})));
    return result;
  }

  private static List<List<Card>> newCardsAsSuits(final int suits) {
    final ArrayList<List<Card>> result = new ArrayList<>(suits);
    for (int i = 0; i < suits; i++) {
      final List<Card> newSuit = new ArrayList<>(13);
      for (int j = 1; j <= 13; j++) {
        newSuit.add(newCard(j));
      }
      result.add(newSuit);
    }
    return result;
  }

  public static SpiderSolitaire newTestFinishGame() {
    SpiderSolitaire result = newEmptyGame();
    setCardStack(result.getState().cardStacks.get(5), new int[]{5, 4, 3, 2, 1});
    setCardStack(result.getState().cardStacks.get(6), new int[]{13, 12, 11, 10, 9, 8, 7, 6});
    result.getState().sortedCards.addAll(newCardsAsSuits(7));
    return result;
  }

  public static SpiderSolitaire newTestSortedCardsViewGame() {
    /**
     * 13 | 12 | 11 | 10 |  9 |  8 |  7 |  6 |  5 |  4
     *  3 |  2 |  1 | 13 | 12 | 11 | 10 |  9 |  8 | 13
     *  7 |  1 |  8 |  2 |  8 |  1 |  7 | 13 |  6 | 12
     *  6 | 13 |  7 |  1 |  7 | 13 |  6 | 12 |  5 | 11
     *  5 | 12 |  6 | 13 |  6 | 12 |  5 | 11 |  4 | 10
     *  4 | 11 |  5 | 12
     * -------------------------------------------------
     * +3 | 10 |  4 | 11 |  5 | 11 |  4 | 10 |  3 |  9 |
     * +2 |  9 |  3 | 10 |  4 | 10 |  3 |  9 |  2 |  8 |
     * +1 |  8 |  2 |  9 |  3 |  9 |  2 |  8 |  1 |  7 |
     * -------------------------------------------------
     * 13 | 12 | 11 | 10 |  9 |  8 |  7 |  6 |  5 |  4
     *  3 |  2 |  1 | 13 | 12 | 11 | 10 |  9 |  8 | 13
     *  7 |  1 |  8 |  2 |  8 |  1 |  7 | 13 |  6 | 12
     *  6 | 13 |  7 |  1 |  7 | 13 |  6 | 12 |  5 | 11
     *  5 | 12 |  6 | 13 |  6 | 12 |  5 | 11 |  4 | 10
     *  4 | 11 |  5 | 12 |  5 | 11 |  4 | 10 |  3 |  9
     *  3 | 10 |  4 | 11 |  4 | 10 |  3 |  9 |  2 |  8
     *  2 |  9 |  3 | 10 |  3 |  9 |  2 |  8 |  1 |  7
     *  1 |  8 |  2 |  9
     * -------------------------------------------------
     * move cards * 8                                  |
     * -------------------------------------------------
     * 13 | 12 | 11 | 10 |  9 |  8 |  7 |  6 |  5 |  4
     *  3 |  2 |  1 | 13 | 12 | 11 | 10 |  9 |  8 |
     * -------------------------------------------------
     * +6 |  5 |  4 |  3 |  2 |  1 | 13 | 12 | 11 |  7 |
     * +9 |  8 |  7 |  6 |  5 |  4 |  3 |  2 |  1 | 10 |
     * -------------------------------------------------
     * 13 | 12 | 11 | 10 |  9 |  8 |  7 |  6 |  5 |  4
     *  3 |  2 |  1 | 13 | 12 | 11 | 10 |  9 |  8 |  7
     *  6 |  5 |  4 |  3 |  2 |  1 | 13 | 12 | 11 | 10
     *  9 |  8 |  7 |  6 |  5 |  4 |  3 |  2 |  1
     * -------------------------------------------------
     * move cards * 12 * 3                             |
     * -------------------------------------------------
     * Game Complete
     **/
    SpiderSolitaire result = newEmptyGame();
    setCardStack(result.getState().cardStacks.get(0), new int[]{13, 3, 7, 6, 5, 4}, 2);
    setCardStack(result.getState().cardStacks.get(1), new int[]{12, 2, 1, 13, 12, 11}, 2);
    setCardStack(result.getState().cardStacks.get(2), new int[]{11, 1, 8, 7, 6, 5}, 2);
    setCardStack(result.getState().cardStacks.get(3), new int[]{10, 13, 2, 1, 13, 12}, 2);
    setCardStack(result.getState().cardStacks.get(4), new int[]{9, 12, 8, 7, 6}, 2);
    setCardStack(result.getState().cardStacks.get(5), new int[]{8, 11, 1, 13, 12}, 2);
    setCardStack(result.getState().cardStacks.get(6), new int[]{7, 10, 7, 6, 5}, 2);
    setCardStack(result.getState().cardStacks.get(7), new int[]{6, 9, 13, 12, 11}, 2);
    setCardStack(result.getState().cardStacks.get(8), new int[]{5, 8, 6, 5, 4}, 2);
    setCardStack(result.getState().cardStacks.get(9), new int[]{4, 13, 12, 11, 10}, 1);
    result.getState().cardsForDrawing.addAll(newCards(new int[]{10, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    result.getState().cardsForDrawing.addAll(newCards(new int[]{7, 11, 12, 13, 1, 2, 3, 4, 5, 6}));
    result.getState().cardsForDrawing.addAll(newCards(new int[]{7, 1, 8, 2, 9, 3, 9, 2, 8, 1}));
    result.getState().cardsForDrawing.addAll(newCards(new int[]{8, 2, 9, 3, 10, 4, 10, 3, 9, 2}));
    result.getState().cardsForDrawing.addAll(newCards(new int[]{9, 3, 10, 4, 11, 5, 11, 4, 10, 3}));
    return result;
  }

}
