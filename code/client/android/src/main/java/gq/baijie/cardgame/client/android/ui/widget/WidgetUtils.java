package gq.baijie.cardgame.client.android.ui.widget;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import rx.functions.Action1;

public class WidgetUtils {

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
    while(from.getChildCount() > startPos) {
      from.removeViewAt(startPos);
    }
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
