package gq.baijie.cardgame.facade.view;

import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.presenter.SpiderSolitairePresenter;

public interface SpiderSolitaireView {

  void init(SpiderSolitairePresenter presenter);

  void setDrawingCardsView(DrawingCardsView view);

  void setSortedCardsView(SortedCardsView view);

  void moveCards(int oldCardStackIndex, int oldCardIndex, int newCardStackIndex, int newCardIndex);

  void drawCards(Card[] cards);

  void undoDrawCards(Card[] drawnCards);

  void moveOutSortedCards(int cardStackIndex, int cardIndex);

  void undoMoveOutSortedCards(int movedCardStackIndex, int movedCardIndex, Card[] movedCards);

  void updateOpenIndex(int cardStackIndex, int newOpenIndex);

}
