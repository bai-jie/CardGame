package gq.baijie.cardgame.client.android;

import android.content.Context;
import android.os.Bundle;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import gq.baijie.cardgame.business.SpiderSolitaire;
import gq.baijie.cardgame.business.SpiderSolitaires;
import gq.baijie.cardgame.client.android.ui.widget.CardStackLayout;
import gq.baijie.cardgame.domain.entity.Card;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MainActivity extends AppCompatActivity {

  private LinearLayout cardStackList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    cardStackList = (LinearLayout) findViewById(R.id.card_stack_list);

    show(SpiderSolitaires.getSampleSpiderSolitaireState());
    setSelectListener();
    setDragListener();
  }

  private void show(SpiderSolitaire.State state) {
    if (cardStackList == null) {
      return; //TODO
    }
    cardStackList.removeAllViews();//TODO do this?
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1);

    for (SpiderSolitaire.State.CardStack cardStack : state.cardStacks) {
      cardStackList.addView(newCardStackView(cardStackList.getContext(), cardStack), layoutParams);
    }
  }

  private static View newCardStackView(Context context, SpiderSolitaire.State.CardStack cardStack) {
    CardStackLayout result = new CardStackLayout(context);
    for (Card card : cardStack.cards) {
      result.addView(newCardView(context, card), MATCH_PARENT, WRAP_CONTENT);
    }
    return result;
  }

  private static View newCardView(Context context, Card card) {
    TextView content = new TextView(context);
    content.setText(toString(card));
    content.setBackgroundResource(R.drawable.card_background);

    PercentFrameLayout container = new PercentFrameLayout(context);
    container.addView(content, 0, 0);
    ((PercentFrameLayout.LayoutParams) content.getLayoutParams()).gravity = CENTER_HORIZONTAL;
    final PercentLayoutHelper.PercentLayoutInfo layoutInfo =
        ((PercentFrameLayout.LayoutParams) content.getLayoutParams()).getPercentLayoutInfo();
    // https://en.wikipedia.org/wiki/Standard_52-card_deck
    layoutInfo.aspectRatio = 0.71428571428571428571428571428571f;// 2.5 / 3.5
    layoutInfo.widthPercent = 0.9f;
    return container;
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


  // ########## Event Bus ##########

  private void setSelectListener() {
    if (cardStackList == null) {
      return; //TODO
    }
    final View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        ((CardStackLayout.LayoutParams) v.getLayoutParams()).delta =
            hasFocus ? v.getResources().getDimensionPixelSize(R.dimen.focused_card_delta)
                     : CardStackLayout.LayoutParams.NOT_SET;
        v.requestLayout();
      }
    };
    final View.OnClickListener clickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        v.requestFocusFromTouch();
      }
    };
    for (int cardStackIndex = 0; cardStackIndex < cardStackList.getChildCount(); cardStackIndex++) {
      final ViewGroup cardStackView = (ViewGroup) cardStackList.getChildAt(cardStackIndex);
      for (int cardIndex = 0; cardIndex < cardStackView.getChildCount(); cardIndex++) {
        final View cardView = cardStackView.getChildAt(cardIndex);
        cardView.setFocusableInTouchMode(true);
        cardView.setOnFocusChangeListener(focusChangeListener);
        cardView.setOnClickListener(clickListener);
      }
    }
  }

  // ########## Event Bus End ##########

  // ########## Drag and Drop ##########

  private void setDragListener() {
    if (cardStackList == null) {
      return; //TODO
    }
    // for every card views
    for (int cardStackIndex = 0; cardStackIndex < cardStackList.getChildCount(); cardStackIndex++) {
      final ViewGroup cardStackView = (ViewGroup) cardStackList.getChildAt(cardStackIndex);
      for (int cardIndex = 0; cardIndex < cardStackView.getChildCount(); cardIndex++) {
        cardStackView.getChildAt(cardIndex).setLongClickable(true);
        cardStackView.getChildAt(cardIndex).setOnLongClickListener(new View.OnLongClickListener() {
          @Override
          public boolean onLongClick(View v) {
            final ViewGroup cardStackView = (ViewGroup) v.getParent();
            final int cardIndex = cardStackView.indexOfChild(v);
            // move dragged card views to a new CardStackLayout
            final CardStackLayout draggedCards = new CardStackLayout(v.getContext());
            moveChildViews(cardStackView, cardIndex, draggedCards);
            // start drag cards
//            draggedCards.requestLayout();
            draggedCards.measure(
                View.MeasureSpec.makeMeasureSpec(cardStackView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            draggedCards.layout(0, 0, draggedCards.getMeasuredWidth(), draggedCards.getMeasuredHeight());
            cardStackView.startDrag(null, new View.DragShadowBuilder(draggedCards), new DragInfo(cardStackView, draggedCards), 0);
            return true;
          }
        });
      }
    }
    // for every card stack views
    final OnDragCardListener onDragCardListener = new OnDragCardListener();
    for (int cardStackIndex = 0; cardStackIndex < cardStackList.getChildCount(); cardStackIndex++) {
      cardStackList.getChildAt(cardStackIndex).setOnDragListener(onDragCardListener);
    }
  }

  private static class DragInfo {
    //TODO add cardStackIndex and cardIndex?
    final ViewGroup originPosition;
    final ViewGroup cardsBeingDragged;

    private DragInfo(ViewGroup originPosition, ViewGroup cardsBeingDragged) {
      this.originPosition = originPosition;
      this.cardsBeingDragged = cardsBeingDragged;
    }
  }

  private static class OnDragCardListener implements View.OnDragListener {
    @Override
    public boolean onDrag(View v, DragEvent event) {
      ViewGroup view = (ViewGroup) v;//TODO
      switch (event.getAction()) {
        case DragEvent.ACTION_DRAG_STARTED:
          return true;//TODO
        case DragEvent.ACTION_DRAG_ENTERED:
          return true;//TODO
        case DragEvent.ACTION_DRAG_LOCATION:
          return true;//TODO
        case DragEvent.ACTION_DRAG_EXITED:
          return true;//TODO
        case DragEvent.ACTION_DROP:
          moveChildViews(((DragInfo) event.getLocalState()).cardsBeingDragged, view);
          return true;
        case DragEvent.ACTION_DRAG_ENDED:
          if (!event.getResult()) {
            DragInfo dragInfo = (DragInfo) event.getLocalState();
            if (view == dragInfo.originPosition) {
              moveChildViews(dragInfo.cardsBeingDragged, view);
            }
          }
          return true;
        default:
          Log.e("OnDragCardListener", "Unknown action type: " + event.getAction());
          return false;
      }
    }
  }

  private static void moveChildViews(final ViewGroup from, final ViewGroup to) {
    moveChildViews(from, 0, to);
  }

  private static void moveChildViews(final ViewGroup from, final int startPos, final ViewGroup to) {
    while(from.getChildCount() > startPos) {
      final View draggedCardView = from.getChildAt(startPos);
      from.removeViewAt(startPos);
      to.addView(draggedCardView);
    }
  }

  // ########## Drag and Drop End ##########

}
