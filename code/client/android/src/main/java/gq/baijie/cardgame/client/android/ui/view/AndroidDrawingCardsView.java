package gq.baijie.cardgame.client.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.client.android.ui.widget.CardStackLayout;
import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.view.DrawingCardsView;
import gq.baijie.cardgame.facade.view.EventBusHelper;
import rx.Observable;

import static gq.baijie.cardgame.client.android.ui.widget.WidgetUtils.withNumberOfChildren;

public class AndroidDrawingCardsView extends CardStackLayout implements DrawingCardsView {

  private final EventBusHelper<DrawEvent> eventBusHelper = EventBusHelper.create();

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
    super.setOnClickListener(v -> eventBusHelper.nextEvent(new DrawEvent(this)));
  }

  @Override
  public void setDecks(int decks) {
    setVisibility(decks > 0 ? VISIBLE : INVISIBLE);
    withNumberOfChildren(
        this,
        decks,
        () -> new AndroidCardView(getContext(), new Card(Card.Suit.HEART, Card.Rank.KING), false)
    );
  }

  @Override
  public Observable<DrawEvent> getEventBus() {
    return eventBusHelper.getEventBus();
  }

  @Override
  public void setOnClickListener(OnClickListener l) {
    throw new UnsupportedOperationException();
  }

}
