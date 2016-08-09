package gq.baijie.cardgame.client.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.client.android.ui.widget.CardStackLayout;
import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.view.DrawingCardsView;
import gq.baijie.cardgame.facade.view.ViewHelper;
import rx.Observable;

public class AndroidDrawingCardsView extends CardStackLayout implements DrawingCardsView {

  private final ViewHelper viewHelper = new ViewHelper();

  public AndroidDrawingCardsView(Context context) {
    super(context);
    init();
  }

  public AndroidDrawingCardsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public AndroidDrawingCardsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setDelta(getResources().getDimensionPixelSize(R.dimen.piled_cards_delta));
    super.setOnClickListener(v -> viewHelper.nextEvent(new DrawEvent(this)));
  }

  @Override
  public void setDecks(int decks) {
    setVisibility(decks > 0 ? VISIBLE : INVISIBLE);
    // * let getChildCount() == decks
    if (getChildCount() < decks) {
      // add cards
      while (getChildCount() < decks) {
        addView(new AndroidCardView(
            getContext(), new Card(Card.Suit.HEART, Card.Rank.KING), false));
      }
    } else if (getChildCount() > decks) {
      // remove cards
      removeViews(decks, getChildCount() - decks);
    } // else getChildCount() == decks, do nothing
  }

  @Override
  public Observable<Event> getEventBus() {
    return viewHelper.getEventBus();
  }

  @Override
  public void setOnClickListener(OnClickListener l) {
    throw new UnsupportedOperationException();
  }

}
