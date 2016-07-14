package gq.baijie.cardgame.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import gq.baijie.cardgame.R;

public class CardStackLayout extends ViewGroup {

  private int delta;

  public CardStackLayout(Context context) {
    super(context);
    init(null, 0);
  }

  public CardStackLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  public CardStackLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(attrs, defStyle);
  }

  private void init(AttributeSet attrs, int defStyle) {
    // Load attributes
    final TypedArray a = getContext().obtainStyledAttributes(
        attrs, R.styleable.CardStackLayout, defStyle, 0);

    delta = a.getDimensionPixelSize(R.styleable.CardStackLayout_cslDelta, -1);
    if (delta == -1) {
      delta = getResources().getDimensionPixelSize(R.dimen.default_card_stack_layout_delta);
    }

    a.recycle();

  }

  public int getDelta() {
    return delta;
  }

  public void setDelta(int delta) {
    if (this.delta == delta) {
      return;
    }
    this.delta = delta;
    requestLayout();
  }

  /**
   * Any layout manager that doesn't scroll will want this.
   */
  @Override
  public boolean shouldDelayChildPressedState() {
    return false;
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    final int count = getChildCount();
    final int height =
        count > 0 ? delta * (count - 1) + getChildAt(count - 1).getMeasuredHeight() : 0;
    int maxWidth = 0;
    int childState = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
        childState = combineMeasuredStates(childState, child.getMeasuredState());
      }
    }
    setMeasuredDimension(
        resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
        resolveSizeAndState(height, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT)
    );
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int count = getChildCount();

    final int leftPos = getPaddingLeft();
    int topPos = getPaddingTop();
    final int maxRightPos = r - l - getPaddingRight();
    final int maxBottomPos = b - t - getPaddingBottom();

    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      child.layout(
          leftPos,
          topPos,
          Math.min(leftPos + child.getMeasuredWidth(), maxRightPos),
          Math.min(topPos + child.getMeasuredHeight(), maxBottomPos)
      );
      topPos += delta;
    }
  }

}
