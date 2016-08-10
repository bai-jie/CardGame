package gq.baijie.cardgame.client.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.client.android.ui.widget.CardStackLayout;
import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.view.SortedCardsView;

import static gq.baijie.cardgame.client.android.ui.widget.WidgetUtils.withNumberOfChildren;

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
    withNumberOfChildren(
        this,
        decks,
        () -> new AndroidCardView(getContext(), new Card(Card.Suit.HEART, Card.Rank.ACE), true)
    );
  }

}
