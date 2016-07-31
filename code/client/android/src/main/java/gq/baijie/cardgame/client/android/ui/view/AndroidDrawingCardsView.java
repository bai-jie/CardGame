package gq.baijie.cardgame.client.android.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.facade.view.DrawingCardsView;
import gq.baijie.cardgame.facade.view.ViewHelper;
import rx.Observable;

public class AndroidDrawingCardsView extends TextView implements DrawingCardsView {

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

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AndroidDrawingCardsView(Context context, AttributeSet attrs, int defStyleAttr,
                                 int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    setGravity(Gravity.CENTER);
    setBackgroundResource(R.drawable.card_background);
    super.setOnClickListener(v -> viewHelper.nextEvent(new DrawEvent(this)));
  }

  @Override
  public void setDecks(int decks) {
    setText(String.valueOf(decks));
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
