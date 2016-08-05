package gq.baijie.cardgame.client.android.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.facade.view.SortedCardsView;
import rx.Observable;

public class AndroidSortedCardsView extends TextView implements SortedCardsView {

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

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AndroidSortedCardsView(Context context, AttributeSet attrs, int defStyleAttr,
                                int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    setGravity(Gravity.CENTER);
    setBackgroundResource(R.drawable.card_background);
  }

  @Override
  public void setDecks(int decks) {
    setText(String.valueOf(decks));
    setVisibility(decks > 0 ? VISIBLE : INVISIBLE);
  }

  @Override
  public Observable<Event> getEventBus() {
    throw new UnsupportedOperationException();
  }

}
