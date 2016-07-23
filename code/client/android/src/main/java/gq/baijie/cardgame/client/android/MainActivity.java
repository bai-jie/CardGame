package gq.baijie.cardgame.client.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import gq.baijie.cardgame.business.SpiderSolitaires;
import gq.baijie.cardgame.client.android.ui.view.AndroidSpiderSolitaireView;
import gq.baijie.cardgame.facade.presenter.SpiderSolitairePresenter;

public class MainActivity extends AppCompatActivity {

  private AndroidSpiderSolitaireView spiderSolitaireView;
  private SpiderSolitairePresenter spiderSolitairePresenter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    spiderSolitaireView = new AndroidSpiderSolitaireView(this);
    setContentView(spiderSolitaireView);
    spiderSolitairePresenter =
        new SpiderSolitairePresenter(SpiderSolitaires.newGame(), spiderSolitaireView);
  }

}
