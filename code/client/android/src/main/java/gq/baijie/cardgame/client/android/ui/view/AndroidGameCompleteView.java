package gq.baijie.cardgame.client.android.ui.view;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import gq.baijie.cardgame.client.android.R;
import gq.baijie.cardgame.facade.view.GameCompleteView;

public class AndroidGameCompleteView implements GameCompleteView {

  private final Context context;

  public AndroidGameCompleteView(Context context) {
    this.context = context;
  }

  @Override
  public void show() {
    new AlertDialog.Builder(context)
        .setTitle(R.string.game_complete_dialog_title)
        .setMessage(R.string.game_complete_dialog_message)
        .show();
  }

}
