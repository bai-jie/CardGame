package gq.baijie.cardgame.client.android.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.util.List;

import gq.baijie.cardgame.business.SpiderSolitaire;
import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.client.android.ui.widget.CardStackLayout;
import gq.baijie.cardgame.client.android.ui.widget.WidgetUtils;
import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.presenter.SpiderSolitairePresenter;
import gq.baijie.cardgame.facade.view.DrawingCardsView;
import gq.baijie.cardgame.facade.view.SortedCardsView;
import gq.baijie.cardgame.facade.view.SpiderSolitaireView;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static gq.baijie.cardgame.client.android.ui.widget.WidgetUtils.forEachChild;
import static gq.baijie.cardgame.client.android.ui.widget.WidgetUtils.moveChildViews;

public class AndroidSpiderSolitaireView extends RelativeLayout implements SpiderSolitaireView {

  private final Subject<Object, Object> internalEventBus = PublishSubject.create();

  private SpiderSolitairePresenter presenter;

  private ViewGroup cardStackListView;
  private View drawingCardsView;
  private View sortedCardsView;

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
    setSelectListener();
    setDragListener();
    show(presenter.getGame().getState());
  }

  @Override
  public void setDrawingCardsView(@NonNull DrawingCardsView view) {
    if (drawingCardsView == view) {
      return;
    }
    if (!(view instanceof AndroidDrawingCardsView)) {
      throw new UnsupportedOperationException("only support AndroidDrawingCardsView now");
    }
    AndroidDrawingCardsView drawingCardsView = (AndroidDrawingCardsView) view;
    drawingCardsView.setId(R.id.drawing_card);
    LayoutParams layoutParams = new LayoutParams(
        getResources().getDimensionPixelSize(R.dimen.default_card_width),
        getResources().getDimensionPixelSize(R.dimen.default_card_height));
    layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.default_card_margin);
    layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.default_card_margin);
    layoutParams.addRule(ALIGN_PARENT_RIGHT);
    layoutParams.addRule(ALIGN_PARENT_BOTTOM);
    if (this.drawingCardsView != null) {
      removeView(this.drawingCardsView);
    }
    this.drawingCardsView = drawingCardsView;
    addView(drawingCardsView, layoutParams);
  }

  @Override
  public void setSortedCardsView(SortedCardsView view) {
    if (sortedCardsView == view) {
      return;
    }
    if (!(view instanceof AndroidSortedCardsView)) {
      throw new UnsupportedOperationException();
    }
    AndroidSortedCardsView sortedCardsView = (AndroidSortedCardsView) view;
    LayoutParams layoutParams = new LayoutParams(
        getResources().getDimensionPixelSize(R.dimen.default_card_width),
        getResources().getDimensionPixelSize(R.dimen.default_card_height));
    layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.default_card_margin);
    layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.default_card_margin);
    layoutParams.addRule(LEFT_OF, R.id.drawing_card);
    layoutParams.addRule(ALIGN_PARENT_BOTTOM);
    if (this.sortedCardsView != null) {
      removeView(this.sortedCardsView);
    }
    this.sortedCardsView = sortedCardsView;
    addView(sortedCardsView, layoutParams);
  }

  @Override
  public void moveCards(
      int oldCardStackIndex, int oldCardIndex, int newCardStackIndex, int newCardIndex) {
    final ViewGroup from = (ViewGroup) cardStackListView.getChildAt(oldCardStackIndex);
    final ViewGroup to = (ViewGroup) cardStackListView.getChildAt(newCardStackIndex);
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
      ((ViewGroup) cardStackListView.getChildAt(i)).addView(newCardView(getContext(), cards[i]));
    }
  }

  @Override
  public void undoDrawCards(Card[] drawnCards) {
    forEachChild(cardStackListView, view -> {
      ViewGroup cardStackView = (ViewGroup) view;
      cardStackView.removeViewAt(cardStackView.getChildCount() - 1);
    });
  }

  @Override
  public void moveOutSortedCards(int cardStackIndex, int cardIndex) {
    WidgetUtils.removeViews((ViewGroup) cardStackListView.getChildAt(cardStackIndex), cardIndex);
  }

  @Override
  public void undoMoveOutSortedCards(
      int movedCardStackIndex, int movedCardIndex, Card[] movedCards) {
    final ViewGroup cardStackView = (ViewGroup) cardStackListView.getChildAt(movedCardStackIndex);
    for (Card card : movedCards) {
      cardStackView.addView(newCardView(cardStackView.getContext(), card));
    }
  }

  @Override
  public void updateOpenIndex(int cardStackIndex, int newOpenIndex) {
    //TODO
  }

  // ########## For init, drawCards ##########
  private void show(SpiderSolitaire.State state) {
    removeAllViews();//TODO do this?
    // * add CardStackListView
    LayoutParams layoutParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
    layoutParams.addRule(ALIGN_PARENT_TOP);
    layoutParams.addRule(ALIGN_PARENT_LEFT);
    layoutParams.addRule(ALIGN_PARENT_RIGHT);
    layoutParams.addRule(ALIGN_PARENT_BOTTOM);
    cardStackListView = newCardStackListView(getContext(), state.cardStacks);
    addView(cardStackListView, layoutParams);
  }

  private ViewGroup newCardStackListView(
      Context context, List<SpiderSolitaire.State.CardStack> cardStacks) {
    LinearLayout result = new LinearLayout(context);
    result.setOrientation(LinearLayout.HORIZONTAL);
    // add card stack views
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1);
    for (SpiderSolitaire.State.CardStack cardStack : cardStacks) {
      result.addView(newCardStackView(result.getContext(), cardStack), layoutParams);
    }
    return result;
  }

  private View newCardStackView(Context context, SpiderSolitaire.State.CardStack cardStack) {
    CardStackLayout result = new CardStackLayout(context);
    for (Card card : cardStack.cards) {
      result.addView(newCardView(context, card), MATCH_PARENT, WRAP_CONTENT);
    }
    internalEventBus.onNext(new NewCardStackViewEvent(result));
    return result;
  }

  private View newCardView(Context context, Card card) {
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

    internalEventBus.onNext(new NewCardViewEvent(container));
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

  private static class NewCardViewEvent {
    final View cardView;

    private NewCardViewEvent(View cardView) {
      this.cardView = cardView;
    }
  }

  private static class NewCardStackViewEvent {
    final ViewGroup cardStackView;

    private NewCardStackViewEvent(ViewGroup cardStackView) {
      this.cardStackView = cardStackView;
    }
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
    internalEventBus.ofType(NewCardViewEvent.class).map(e -> e.cardView).subscribe(cardView -> {
      cardView.setFocusableInTouchMode(true);
      cardView.setOnFocusChangeListener(focusChangeListener);
      cardView.setOnClickListener(clickListener);
    });
  }

  // ########## Event Bus End ##########

  // ########## Undo Input ##########

  //TODO recheck this method
  //reference: http://android-developers.blogspot.in/2009/12/back-and-other-hard-keys-three-stories.html
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
      return super.dispatchKeyEvent(event);
    }

    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {

      // Tell the framework to start tracking this event.
      getKeyDispatcherState().startTracking(event, this);
      return true;

    } else if (event.getAction() == KeyEvent.ACTION_UP) {
      getKeyDispatcherState().handleUpEvent(event);
      if (event.isTracking() && !event.isCanceled()) {

        // DO BACK ACTION HERE
        return onBackPressed();

      }
    }
    return super.dispatchKeyEvent(event);
  }

  private boolean onBackPressed() {
    if (presenter.canUndo()) {
      presenter.undo();
      return true;
    } else {
      return false;
    }
  }

  // ########## Undo Input End ##########

  // ########## Drag and Drop ##########

  private final Subject<Pair<? extends View, DragEvent>, Pair<? extends View, DragEvent>> dragEvents
      = PublishSubject.create();

  {
    dragEvents.groupBy(event -> ((DragInfo) event.second.getLocalState())).subscribe(scope -> {
      DragInfo state = scope.getKey();

      ConnectableObservable<Pair<? extends View, DragEvent>> publish = scope.takeWhile(e -> {
        switch (e.second.getAction()) {
          case DragEvent.ACTION_DRAG_STARTED:
            state.unendedCounter++;
            break;
          case DragEvent.ACTION_DRAG_ENDED:
            state.unendedCounter--;
            break;
        }
        return state.unendedCounter > 0;
      }).publish();


      publish.filter(e->e.second.getAction() == DragEvent.ACTION_DROP).subscribe(e->{
        final int destStackIndex = ((ViewGroup) e.first.getParent()).indexOfChild(e.first);
        if (presenter.canMoveCards(state.originCardStackIndex, state.originCardIndex, destStackIndex)) {
          forEachChild(state.cardsBeingDragged, view -> view.setVisibility(GONE));
          state.droppedCardStackIndex = destStackIndex;
        }
      });

      publish.toCompletable().subscribe(() -> {
        moveChildViews(state.cardsBeingDragged, state.originCardStackView);
        if (state.droppedCardStackIndex >= 0) {
          presenter.moveCards(state.originCardStackIndex, state.originCardIndex, state.droppedCardStackIndex);
        }
      });

      publish.connect();

    });
  }

  private void setDragListener() {
    // for every card views
    final OnTouchCardViewListener onTouchCardViewListener = new OnTouchCardViewListener();
    internalEventBus
        .ofType(NewCardViewEvent.class)
        .subscribe(e -> e.cardView.setOnTouchListener(onTouchCardViewListener));
    // subscribe drag events emitted by card stack views
    internalEventBus.ofType(NewCardStackViewEvent.class).map(e -> e.cardStackView)
        .subscribe(view -> {
          RxView.drags(view)
              .map(rawEvent -> Pair.create(view, rawEvent))
              .subscribe(dragEvents);
        });
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

    int unendedCounter = 0;
    int droppedCardStackIndex = -1;

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

  // ########## Drag and Drop End ##########

}
