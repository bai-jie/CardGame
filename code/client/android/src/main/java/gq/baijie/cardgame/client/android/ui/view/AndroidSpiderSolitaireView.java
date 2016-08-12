package gq.baijie.cardgame.client.android.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import static gq.baijie.cardgame.client.android.ui.widget.WidgetUtils.getLastChild;
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
    setBackground(getWallpaperBackground());
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
    setClipChildren(false);
    drawingCardsView.setClipChildren(false);
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
    setClipChildren(false);
    sortedCardsView.setClipChildren(false);
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
    if (drawingCardsView != null && drawingCardsView instanceof AndroidDrawingCardsView) {
      drawCardsWhenHaveDrawingCardsView(cards, (AndroidDrawingCardsView) drawingCardsView);
    } else {
      drawCardsWhenHaventDrawingCardsView(cards);
    }
  }

  private void drawCardsWhenHaventDrawingCardsView(Card[] cards) {
    for (int i = 0; i < cards.length; i++) {
      ((ViewGroup) cardStackListView.getChildAt(i)).addView(
          newCardView(getContext(), cards[i], true),
          MATCH_PARENT,
          WRAP_CONTENT
      );
    }
  }

  private void drawCardsWhenHaveDrawingCardsView(
      Card[] cards, AndroidDrawingCardsView drawingCardsView) {
    assert cards.length == 10;
    // * create card views
    View[] cardViews = new View[10];
    for (int i = 0; i < 10; i++) {
      cardViews[i] = newCardView(getContext(), cards[i], true);
    }
    // * restore card views and add them to card stack views
    Runnable onAnimationEnd = () -> {
      for (int i = 0; i < 10; i++) {
        final View cardView = cardViews[i];
        AndroidSpiderSolitaireView.this.removeView(cardView);
        ((ViewGroup) cardStackListView.getChildAt(i)).addView(
            newCardView(getContext(), ((AndroidCardView) cardView).getCard(), true),
            MATCH_PARENT,
            WRAP_CONTENT);
      }
    };
    // * animate move card views in container view
    // ** add card views to container view
    final Rect parentBounds = new Rect();
    final Rect startBounds = new Rect();
    getGlobalVisibleRect(parentBounds);
    getLastChild(drawingCardsView).getGlobalVisibleRect(startBounds);//TODO
    startBounds.offset(-parentBounds.left, -parentBounds.top);
    LayoutParams layoutParams = new LayoutParams(startBounds.width(), startBounds.height());
    layoutParams.addRule(ALIGN_PARENT_RIGHT);
    layoutParams.addRule(ALIGN_PARENT_BOTTOM);
    layoutParams.rightMargin = parentBounds.width() - startBounds.right;
    layoutParams.bottomMargin = parentBounds.height() - startBounds.bottom;
    for (int i = 0; i < cards.length; i++) {
      cardViews[i].setAlpha(0);
      addView(cardViews[i], layoutParams);
    }
    // ** animate added card views
    post(() -> {
      AnimatorSet animator = drawCardsAnimator(cardViews);
      animator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
      animator.setInterpolator(new AccelerateDecelerateInterpolator());
      animator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          onAnimationEnd.run();
        }
      });
      animator.start();
    });
  }

  private AnimatorSet drawCardsAnimator(View[] cardViews) {
    assert cardViews.length == 10;
    assert cardStackListView.getChildCount() == 10;
    Animator[] translationAnimators = new Animator[10];
    for (int i = 0; i < 10; i++) {
      translationAnimators[i] =
          drawCardsAnimator(cardViews[i], (CardStackLayout) cardStackListView.getChildAt(i));
    }
    final int delay = getResources().getInteger(android.R.integer.config_shortAnimTime) / 10;
    for (int i = 0; i < 10; i++) {
      translationAnimators[i].setStartDelay(delay * i);
    }
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(translationAnimators);
    return animatorSet;
  }

  /**
   * drawingCardsView shouldn't be null
   */
  private static Animator drawCardsAnimator(View cardView, CardStackLayout destCardStackView) {
    // * calculate start and end positions
    final Rect parentBounds = new Rect();
    final Rect startBounds = new Rect();
    final Rect endBounds = new Rect();
    ((View) cardView.getParent()).getGlobalVisibleRect(parentBounds);
    cardView.getGlobalVisibleRect(startBounds);
    getLastChild(destCardStackView).getGlobalVisibleRect(endBounds);//TODO
    startBounds.offset(-parentBounds.left, -parentBounds.top);
    endBounds.offset(-parentBounds.left, -parentBounds.top);
    endBounds.offset(0, destCardStackView.getDelta());
    Animator animator = translationAnimator(cardView, startBounds, endBounds);
    animator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        cardView.setAlpha(1);
      }
    });
    return animator;
  }

  private static Animator translationAnimator(View view, Rect startBounds, Rect endBounds) {
    // * calculate scale ratio
    final float startScaleRatio = ((float) startBounds.width()) / view.getWidth();
    final float endScaleRatio = ((float) endBounds.width()) / view.getWidth();
    //TODO take height into account
    // * generate Animator
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(
        ObjectAnimator.ofFloat(view, View.X, startBounds.left, endBounds.left),
        ObjectAnimator.ofFloat(view, View.Y, startBounds.top, endBounds.top),
        ObjectAnimator.ofFloat(view, View.SCALE_X, startScaleRatio, endScaleRatio),
        ObjectAnimator.ofFloat(view, View.SCALE_Y, startScaleRatio, endScaleRatio)
    );
    return animatorSet;
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
    if (sortedCardsView != null) {
      moveOutSortedCardsWhenHaveSortedCardsView(cardStackIndex, cardIndex);
    } else {
      moveOutSortedCardsWhenHaventSortedCardsView(cardStackIndex, cardIndex);
    }
  }

  private void moveOutSortedCardsWhenHaventSortedCardsView(int cardStackIndex, int cardIndex) {
    WidgetUtils.removeViews((ViewGroup) cardStackListView.getChildAt(cardStackIndex), cardIndex);
  }

  private void moveOutSortedCardsWhenHaveSortedCardsView(int cardStackIndex, int cardIndex) {
    ViewGroup cardStackView = (ViewGroup) cardStackListView.getChildAt(cardStackIndex);
    // * before animate:
    // save this position of the first sorted card view
    final Rect parentBounds = new Rect();
    final Rect startBounds = new Rect();
    getGlobalVisibleRect(parentBounds);
    cardStackView.getChildAt(cardIndex).getGlobalVisibleRect(startBounds);
    startBounds.offset(-parentBounds.left, -parentBounds.top);
    // ** move cards to a temp card stack view
    View[] movingSortedCardViews = WidgetUtils.getChildren(cardStackView, cardIndex);
    WidgetUtils.removeViews(cardStackView, cardIndex);
    CardStackLayout tempCardStackView = new CardStackLayout(getContext());
    WidgetUtils.addChildren(tempCardStackView, movingSortedCardViews);
    // ** add this temp card stack view at the first sorted card view's position
    final LayoutParams layoutParams = new LayoutParams(startBounds.width(), WRAP_CONTENT);
    layoutParams.addRule(ALIGN_PARENT_LEFT);
    layoutParams.addRule(ALIGN_PARENT_TOP);
    layoutParams.leftMargin = startBounds.left;
    layoutParams.topMargin = startBounds.top;
    addView(tempCardStackView, layoutParams);
    // * after animate: remove this temp card stack view including sorted card views
    Runnable onAnimationEnd = () -> removeView(tempCardStackView);
    // * animate: move this temp card stack view to sortedCardsView
    post(()->{
      Animator animator = animatorToSortedCardsView(tempCardStackView);
      animator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
      animator.setInterpolator(new AccelerateDecelerateInterpolator());
      // * remove card views moved out from this cardStackView after animate end
      animator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          onAnimationEnd.run();
        }
      });
      animator.start();
    });
  }

  /**
   * sortedCardsView shouldn't be null
   */
  private Animator animatorToSortedCardsView(ViewGroup tempCardStackView) {
    // * calculate start and end positions
    final Rect parentBounds = new Rect();
    final Rect startBounds = new Rect();
    final Rect endBounds = new Rect();
    ((View) tempCardStackView.getParent()).getGlobalVisibleRect(parentBounds);
    tempCardStackView.getGlobalVisibleRect(startBounds);
    sortedCardsView.getGlobalVisibleRect(endBounds);
    startBounds.offset(-parentBounds.left, -parentBounds.top);
    endBounds.offset(-parentBounds.left, -parentBounds.top);
    // * calculate scale ratio
    final float startScaleRatioX = ((float) startBounds.width()) / tempCardStackView.getWidth();
    final float endScaleRatioX = ((float) endBounds.width()) / tempCardStackView.getWidth();
    final float startScaleRatioY = ((float) startBounds.height()) / tempCardStackView.getHeight();
    final float endScaleRatioY = ((float) endBounds.height()) / tempCardStackView.getHeight();
    // * generate Animator
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(
        ObjectAnimator.ofFloat(tempCardStackView, View.X, startBounds.left, endBounds.left),
        ObjectAnimator.ofFloat(tempCardStackView, View.Y, startBounds.top, endBounds.top),
        ObjectAnimator.ofFloat(tempCardStackView, View.SCALE_X, startScaleRatioX, endScaleRatioX),
        ObjectAnimator.ofFloat(tempCardStackView, View.SCALE_Y, startScaleRatioY, endScaleRatioY)
    );
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        //TODO bug in API 16: setPivotX(0) does't work
        // http://stackoverflow.com/questions/26658124/setpivotx-doesnt-work-on-android-4-1-1-nineoldandroids
        // https://github.com/JakeWharton/NineOldAndroids/issues/77
        tempCardStackView.setPivotX(0.0001f);
        tempCardStackView.setPivotY(0.0001f);
      }
    });
    return animatorSet;
  }

  @Override
  public void undoMoveOutSortedCards(
      int movedCardStackIndex, int movedCardIndex, Card[] movedCards) {
    final ViewGroup cardStackView = (ViewGroup) cardStackListView.getChildAt(movedCardStackIndex);
    for (Card card : movedCards) {
      cardStackView.addView(newCardView(cardStackView.getContext(), card, true));
    }
  }

  @Override
  public void updateOpenIndex(int cardStackIndex, int oldOpenIndex, int newOpenIndex) {
    final ViewGroup cardStackView = (ViewGroup) cardStackListView.getChildAt(cardStackIndex);
    if (oldOpenIndex >= newOpenIndex) {
      for (int i = newOpenIndex; i < oldOpenIndex; i++) {
        ((AndroidCardView) cardStackView.getChildAt(i)).setOpen(true);
      }
    } else { // undo updateOpenIndex (oldOpenIndex < newOpenIndex)
      for (int i = oldOpenIndex; i < newOpenIndex; i++) {
        ((AndroidCardView) cardStackView.getChildAt(i)).setOpen(false);
      }
    }
  }

  // ########## For init, drawCards ##########

  private Drawable getWallpaperBackground() {
    Bitmap wallpaperTile = getWallpaperTile();
    BitmapDrawable result = new BitmapDrawable(getResources(), wallpaperTile);
    result.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    return result;
  }

  @Nullable
  private Bitmap getWallpaperTile() {
    return drawableToBitmap(getResources().getDrawable(R.drawable.bg_wallpaper_tile));
  }

  // copy from http://stackoverflow.com/a/10600736/5015207
  private static Bitmap drawableToBitmap(Drawable drawable) {
    Bitmap bitmap;

    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if(bitmapDrawable.getBitmap() != null) {
        return bitmapDrawable.getBitmap();
      }
    }

    if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
      bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
    } else {
      bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

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
    int half_margin = getResources().getDimensionPixelSize(R.dimen.card_stack_list_margin_half);
    result.setPadding(half_margin, 0, half_margin, 0);
    for (int i = 0; i < cardStack.cards.size(); i++) {
      View cardView = newCardView(context, cardStack.cards.get(i), i >= cardStack.getOpenIndex());
      result.addView(cardView, MATCH_PARENT, WRAP_CONTENT);
    }
    internalEventBus.onNext(new NewCardStackViewEvent(result));
    return result;
  }

  private View newCardView(Context context, Card card, boolean open) {
    final AndroidCardView result = new AndroidCardView(context, card, open);
    internalEventBus.onNext(new NewCardViewEvent(result));
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

  {
    // set Focusable to receive KeyEvent
    setFocusable(true);
    setFocusableInTouchMode(true);
  }

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
