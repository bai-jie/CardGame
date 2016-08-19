package gq.baijie.cardgame.client.android;

import android.app.Activity;
import android.os.Bundle;

import gq.baijie.cardgame.client.android.ui.view.AndroidDrawingCardsView;
import gq.baijie.cardgame.client.android.ui.view.AndroidGameCompleteView;
import gq.baijie.cardgame.client.android.ui.view.AndroidSortedCardsView;
import gq.baijie.cardgame.client.android.ui.view.AndroidSpiderSolitaireView;
import gq.baijie.cardgame.facade.presenter.SpiderSolitairePresenter;
import gq.baijie.cardgame.testtool.TestSpiderSolitaires;

public class DebugMainActivityLifecycleCallbacks extends ActivityLifecycleCallbacksAdapter {

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    if(!(activity instanceof MainActivity)) {
      return;
    }
    ((MainActivity) activity).getCreateOptionsMenuEvents().subscribe(menu -> {
      menu.add("New Debug Game").setOnMenuItemClickListener(item -> {
        newDebugGame(activity);
        return true;
      });
    });
  }

  private void newDebugGame(Activity activity) {
    AndroidSpiderSolitaireView spiderSolitaireView = new AndroidSpiderSolitaireView(activity);
    activity.setContentView(spiderSolitaireView);
    new SpiderSolitairePresenter(
        TestSpiderSolitaires.newTestSortedCardsViewGame(),
        spiderSolitaireView,
        new AndroidDrawingCardsView(activity),
        new AndroidSortedCardsView(activity),
        new AndroidGameCompleteView(activity)
    );
  }

}
