package gq.baijie.cardgame;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import gq.baijie.cardgame.ui.widget.CardStackLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final int focused_card_delta = getResources().getDimensionPixelSize(R.dimen.focused_card_delta);

    LinearLayout cardStackList = (LinearLayout) findViewById(R.id.card_stack_list);
    assert cardStackList != null;
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1);

    CardStackLayout cardStack = new CardStackLayout(cardStackList.getContext());
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStack.addView(
        newCard(cardStack.getContext()),
        new CardStackLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, focused_card_delta));
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStackList.addView(cardStack, layoutParams);

    cardStack = new CardStackLayout(cardStackList.getContext());
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStackList.addView(cardStack, layoutParams);

    cardStack = new CardStackLayout(cardStackList.getContext());
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStackList.addView(cardStack, layoutParams);

    cardStack = new CardStackLayout(cardStackList.getContext());
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStack.addView(newCard(cardStack.getContext()), MATCH_PARENT, WRAP_CONTENT);
    cardStackList.addView(cardStack, layoutParams);
  }

  private static View newCard(Context context) {
    ImageView result = new ImageView(context);
    result.setImageResource(R.mipmap.ic_launcher);
    return result;
  }

}
