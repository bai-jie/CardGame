package gq.baijie.cardgame.client.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.client.android.ui.widget.CardStackLayout;
import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.view.SortedCardsView;
import rx.Observable;

public class AndroidSortedCardsView extends CardStackLayout implements SortedCardsView {

  public AndroidSortedCardsView(Context context) {
    super(context);
    init();
  }

  public AndroidSortedCardsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public AndroidSortedCardsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setDelta(getResources().getDimensionPixelSize(R.dimen.piled_cards_delta));
  }

  @Override
  public void setDecks(int decks) {
    setVisibility(decks > 0 ? VISIBLE : INVISIBLE);
    // * let getChildCount() == decks
    if (getChildCount() < decks) {
      // add cards
      while (getChildCount() < decks) {
        addView(new AndroidCardView(getContext(), new Card(Card.Suit.HEART, Card.Rank.ACE), true));
      }
    } else if (getChildCount() > decks) {
      // remove cards
      removeViews(decks, getChildCount() - decks);
    } // else getChildCount() == decks, do nothing
  }

  @Override
  public Observable<Event> getEventBus() {
    throw new UnsupportedOperationException();
  }

}
