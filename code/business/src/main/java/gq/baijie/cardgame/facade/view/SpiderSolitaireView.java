package gq.baijie.cardgame.facade.view;

import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.presenter.SpiderSolitairePresenter;

public interface SpiderSolitaireView {

  void init(SpiderSolitairePresenter presenter);

  void moveCards(int oldCardStackIndex, int oldCardIndex, int newCardStackIndex, int newCardIndex);

  void drawCards(Card[] cards);

  void moveOutSortedCards(int cardStackIndex, int cardIndex);

  void updateOpenIndex(int cardStackIndex, int newOpenIndex);

}
