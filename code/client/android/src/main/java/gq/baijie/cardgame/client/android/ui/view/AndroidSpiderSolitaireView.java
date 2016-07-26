package gq.baijie.cardgame.client.android.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import gq.baijie.cardgame.business.SpiderSolitaire;
import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.client.android.ui.widget.CardStackLayout;
import gq.baijie.cardgame.client.android.ui.widget.WidgetUtils;
import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.presenter.SpiderSolitairePresenter;
import gq.baijie.cardgame.facade.view.SpiderSolitaireView;
import rx.functions.Action1;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static gq.baijie.cardgame.client.android.ui.widget.WidgetUtils.forEachChild;
import static gq.baijie.cardgame.client.android.ui.widget.WidgetUtils.moveChildViews;

public class AndroidSpiderSolitaireView extends LinearLayout implements SpiderSolitaireView {

  private SpiderSolitairePresenter presenter;

  public AndroidSpiderSolitaireView(Context context) {
    super(context);
  }

  public AndroidSpiderSolitaireView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AndroidSpiderSolitaireView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AndroidSpiderSolitaireView(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public void init(SpiderSolitairePresenter presenter) {
    this.presenter = presenter;
    show(presenter.getGame().getState());
    //TODO new add Card?
    setSelectListener();
    setDragListener();
    //TODO end
  }

  @Override
  public void moveCards(
      int oldCardStackIndex, int oldCardIndex, int newCardStackIndex, int newCardIndex) {
    final ViewGroup from = (ViewGroup) getChildAt(oldCardStackIndex);
    final ViewGroup to = (ViewGroup) getChildAt(newCardStackIndex);
    forEachChild(from, oldCardIndex, new Action1<View>() {
      @Override
      public void call(View view) {
        view.setVisibility(VISIBLE);
      }
    });
    moveChildViews(from, oldCardIndex, to);
  }

  @Override
  public void drawCards(Card[] cards) {
    for (int i = 0; i < cards.length; i++) {
      ((ViewGroup) getChildAt(i)).addView(newCardView(getContext(), cards[i]));
    }
  }

  @Override
  public void moveOutSortedCards(int cardStackIndex, int cardIndex) {
    WidgetUtils.removeViews((ViewGroup) getChildAt(cardStackIndex), cardIndex);
    //TODO update SortedCardsView
  }

  @Override
  public void updateOpenIndex(int cardStackIndex, int newOpenIndex) {
    //TODO
  }

  // ########## For init, drawCards ##########
  private void show(SpiderSolitaire.State state) {
    removeAllViews();//TODO do this?
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1);

    for (SpiderSolitaire.State.CardStack cardStack : state.cardStacks) {
      addView(newCardStackView(getContext(), cardStack), layoutParams);
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
  // ########## For init End ##########

  // ########## Event Bus ##########

  private void setSelectListener() {
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
    for (int cardStackIndex = 0; cardStackIndex < getChildCount(); cardStackIndex++) {
      final ViewGroup cardStackView = (ViewGroup) getChildAt(cardStackIndex);
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
    // for every card views
    final OnTouchCardViewListener onTouchCardViewListener = new OnTouchCardViewListener();
    for (int cardStackIndex = 0; cardStackIndex < getChildCount(); cardStackIndex++) {
      final ViewGroup cardStackView = (ViewGroup) getChildAt(cardStackIndex);
      for (int cardIndex = 0; cardIndex < cardStackView.getChildCount(); cardIndex++) {
        cardStackView.getChildAt(cardIndex).setOnTouchListener(onTouchCardViewListener);
      }
    }
    // for every card stack views
    final OnDragCardListener onDragCardListener = new OnDragCardListener();
    for (int cardStackIndex = 0; cardStackIndex < getChildCount(); cardStackIndex++) {
      getChildAt(cardStackIndex).setOnDragListener(onDragCardListener);
    }
  }

  /**
   * <strong>pre-condition</strong>:
   * <ul>
   *   <li>v is card view</li>
   *   <li>card view's parent is card stack view(ViewGroup)</li>
   *   <li>card stack view's parent is card stack list view(ViewGroup)</li>
   * </ul>
   * @param v should be card view
   * @return start successfully(card view can move...)
   */
  private boolean startDrag(View v) {
    final ViewGroup cardStackView = (ViewGroup) v.getParent();
    final int cardIndex = cardStackView.indexOfChild(v);
    final int cardStackIndex =
        ((ViewGroup) cardStackView.getParent()).indexOfChild(cardStackView);
    // * check this card can move
    if (!presenter.getGame().canMove(cardStackIndex, cardIndex)) {
      return false;
    }
    // move dragged card views to a new CardStackLayout
    final CardStackLayout draggedCards = new CardStackLayout(v.getContext());
    moveChildViews(cardStackView, cardIndex, draggedCards);
    // start drag cards
//            draggedCards.requestLayout();
    draggedCards.measure(
        View.MeasureSpec.makeMeasureSpec(cardStackView.getWidth(), View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    draggedCards.layout(0, 0, draggedCards.getMeasuredWidth(), draggedCards.getMeasuredHeight());
    cardStackView.startDrag(
        null,
        new View.DragShadowBuilder(draggedCards),
        new DragInfo(cardStackIndex, cardIndex, cardStackView, draggedCards),
        0
    );
    return true;
  }

  private static class DragInfo {
    final int originCardStackIndex;
    final int originCardIndex;
    final ViewGroup originCardStackView;
    final ViewGroup cardsBeingDragged;

    int droppedCardStackIndex;

    private DragInfo(
        int originCardStackIndex,
        int originCardIndex,
        ViewGroup originCardStackView,
        ViewGroup cardsBeingDragged
    ) {
      this.originCardStackIndex = originCardStackIndex;
      this.originCardIndex = originCardIndex;
      this.originCardStackView = originCardStackView;
      this.cardsBeingDragged = cardsBeingDragged;
    }
  }

  private class OnTouchCardViewListener implements OnTouchListener {

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
        startDrag(v);
      }
      return false;
    }

  }

  private static class OnDragCardListener implements View.OnDragListener {
    @Override
    public boolean onDrag(View v, DragEvent event) {
      ViewGroup view = (ViewGroup) v;//TODO
      switch (event.getAction()) {
        case DragEvent.ACTION_DRAG_STARTED:
          AndroidSpiderSolitaireView parent = (AndroidSpiderSolitaireView) view.getParent();
          int cardStackIndexOfView = parent.indexOfChild(view);
          return parent.presenter.canMoveCards(
              ((DragInfo) event.getLocalState()).originCardStackIndex,
              ((DragInfo) event.getLocalState()).originCardIndex,
              cardStackIndexOfView
          );
        case DragEvent.ACTION_DRAG_ENTERED:
          return true;//TODO
        case DragEvent.ACTION_DRAG_LOCATION:
          return true;//TODO
        case DragEvent.ACTION_DRAG_EXITED:
          return true;//TODO
        case DragEvent.ACTION_DROP:
          forEachChild(((DragInfo) event.getLocalState()).cardsBeingDragged, new Action1<View>() {
            @Override
            public void call(View view) {
              view.setVisibility(GONE);
            }
          });
          ((DragInfo) event.getLocalState()).droppedCardStackIndex =
              ((ViewGroup) view.getParent()).indexOfChild(view);
          return true;
        case DragEvent.ACTION_DRAG_ENDED:
          DragInfo dragInfo = (DragInfo) event.getLocalState();
          if (view == dragInfo.originCardStackView) {
            moveChildViews(dragInfo.cardsBeingDragged, view);
            if (event.getResult()) {
              ((AndroidSpiderSolitaireView) view.getParent()).presenter.moveCards(
                  dragInfo.originCardStackIndex,
                  dragInfo.originCardIndex,
                  dragInfo.droppedCardStackIndex
              );
            }
          }
          return true;
        default:
          Log.e("OnDragCardListener", "Unknown action type: " + event.getAction());
          return false;
      }
    }
  }

  // ########## Drag and Drop End ##########

}
