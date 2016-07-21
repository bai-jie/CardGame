package gq.baijie.cardgame.client.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import gq.baijie.cardgame.client.android.R;

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
    /** ∑delta of child (0, childCount-1) + height of the last child */
    int height = 0;
    /** max width of children */
    int maxWidth = 0;
    int childState = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        // height
        height += computeChildDelta(child);
        // maxWidth
        maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
        // childState
        childState = combineMeasuredStates(childState, child.getMeasuredState());
      }
    }
    if (count > 0) {
      final View lastChild = getChildAt(count - 1);
      height -= computeChildDelta(lastChild);
      height += lastChild.getMeasuredHeight();
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
      topPos += computeChildDelta(child);
    }
  }

  private int computeChildDelta(View child) {
    final int childDelta = ((LayoutParams) child.getLayoutParams()).delta;
    return childDelta < 0 ? delta : childDelta;// see LayoutParams.delta;
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(p);
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  public static class LayoutParams extends ViewGroup.LayoutParams {

    public static final int NOT_SET = -1;

    /**
     * a pixel value which is greater than or equal to 0 or {@link #NOT_SET}
     */
    public int delta = NOT_SET;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, int delta) {
      super(width, height);
      this.delta = delta;
    }

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);
    }

  }

}
