package gq.baijie.cardgame.client.android.ui.widget;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import rx.functions.Action1;
import rx.functions.Func0;

public class WidgetUtils {

  public static View getLastChild(@NonNull final ViewGroup parent) {
    if (parent.getChildCount() > 0) {
      return parent.getChildAt(parent.getChildCount() - 1);
    } else {
      return null;
    }
  }

  public static View[] getChildren(@NonNull final ViewGroup parent, final int startPos) {
    if (startPos >= parent.getChildCount()) {
      return new View[0];
    }
    View[] result = new View[parent.getChildCount() - startPos];
    for (int i = startPos; i < parent.getChildCount(); i++) {
      result[i - startPos] = parent.getChildAt(i);
    }
    return result;
  }

  public static void addChildren(@NonNull final ViewGroup parent, @NonNull final View[] children) {
    for (View child : children) {
      parent.addView(child);
    }
  }

  public static void moveChildViews(@NonNull final ViewGroup from, @NonNull final ViewGroup to) {
    moveChildViews(from, 0, to);
  }

  public static void moveChildViews(
      @NonNull final ViewGroup from, final int startPos, @NonNull final ViewGroup to) {
    while(from.getChildCount() > startPos) {
      final View draggedCardView = from.getChildAt(startPos);
      from.removeViewAt(startPos);
      to.addView(draggedCardView);
    }
  }

  public static void removeViews(@NonNull final ViewGroup from, final int startPos) {
    from.removeViews(startPos, from.getChildCount() - startPos);
  }

  /**
   * let target have specific number of children
   *
   * @param target       the handled parent view
   * @param number       the number of children after this call
   * @param childFactory use if {@code target.getChildCount() < number}
   */
  public static void withNumberOfChildren(
      @NonNull final ViewGroup target, final int number, final Func0<View> childFactory) {
    if (target.getChildCount() < number) {
      // add cards
      while (target.getChildCount() < number) {
        target.addView(childFactory.call());
      }
    } else if (target.getChildCount() > number) {
      // remove cards
      removeViews(target, number);
    } // else getChildCount() == number, do nothing
  }

  public static void forEachChild(
      @NonNull final ViewGroup viewGroup, @NonNull final Action1<View> action) {
    forEachChild(viewGroup, 0, action);
  }

  public static void forEachChild(
      @NonNull final ViewGroup viewGroup, final int startPos, @NonNull final Action1<View> action) {
    if (startPos < 0 ) {
      throw new IllegalArgumentException();
    }
    for (int i = startPos; i < viewGroup.getChildCount(); i++) {
      action.call(viewGroup.getChildAt(i));
    }
  }

}
