package gq.baijie.cardgame.client.android.ui.view;

import android.content.Context;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.view.Gravity;
import android.widget.TextView;

import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.domain.entity.Card;


public class AndroidCardView extends PercentFrameLayout {

  private final TextView contentView;

  private final Card card;

  private boolean open;

  public AndroidCardView(Context context, Card card, boolean open) {
    super(context);
    contentView = new TextView(context);
    this.card = card;
    this.open = open;
    init();
  }

  private void init() {
    contentView.setBackgroundResource(R.drawable.card_background);
    updateContentView();

    addView(contentView, 0, 0);
    final LayoutParams layoutParams = (LayoutParams) contentView.getLayoutParams();
    layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
    final PercentLayoutHelper.PercentLayoutInfo layoutInfo = layoutParams.getPercentLayoutInfo();
    // https://en.wikipedia.org/wiki/Standard_52-card_deck
    layoutInfo.aspectRatio = 0.71428571428571428571428571428571f;// 2.5 / 3.5
    layoutInfo.widthPercent = 0.9f;
  }

  public Card getCard() {
    return card;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    if (this.open == open) {
      return;
    }
    this.open = open;
    updateContentView();
  }

  private void updateContentView() {
    if (open) {
      contentView.setText(toString(card));
    } else {
      contentView.setText("");
    }
  }

  private static String toString(Card card) {
    String result;
    switch (card.getSuit()) {
      case CLUB:
        result = "♣";
        break;
      case DIAMOND:
        result = "♦";
        break;
      case HEART:
        result = "♥";
        break;
      case SPADE:
        result = "♠";
        break;
      default:
        result = "";
        break;
    }
    switch (card.getRank()) {
      case ACE:
        result += "A";
        break;
      case JACK:
        result += "J";
        break;
      case QUEEN:
        result += "Q";
        break;
      case KING:
        result += "K";
        break;
      default:
        result += card.getRank().getId();
        break;
    }
    return result;
  }

}
