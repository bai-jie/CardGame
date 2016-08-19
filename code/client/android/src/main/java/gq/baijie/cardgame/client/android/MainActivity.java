package gq.baijie.cardgame.client.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.Toast;

import gq.baijie.cardgame.business.SpiderSolitaires;
import gq.baijie.cardgame.client.android.ui.view.AndroidDrawingCardsView;
import gq.baijie.cardgame.client.android.ui.view.AndroidGameCompleteView;
import gq.baijie.cardgame.client.android.ui.view.AndroidSortedCardsView;
import gq.baijie.cardgame.client.android.ui.view.AndroidSpiderSolitaireView;
import gq.baijie.cardgame.facade.presenter.SpiderSolitairePresenter;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class MainActivity extends AppCompatActivity {

  private final Subject<Menu, Menu> createOptionsMenuEvents = PublishSubject.create();

  public Observable<Menu> getCreateOptionsMenuEvents() {
    return createOptionsMenuEvents.asObservable();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    newGame();
  }

  private void newGame() {
    AndroidSpiderSolitaireView spiderSolitaireView = new AndroidSpiderSolitaireView(this);
    setContentView(spiderSolitaireView);
    new SpiderSolitairePresenter(
        SpiderSolitaires.newGame(),
        spiderSolitaireView,
        new AndroidDrawingCardsView(this),
        new AndroidSortedCardsView(this),
        new AndroidGameCompleteView(this)
    );
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(R.string.action_new_game).setOnMenuItemClickListener(item -> {
      newGame();
      return true;
    });
    menu.add(Menu.NONE, Menu.NONE, Menu.CATEGORY_SECONDARY, R.string.action_exit)
        .setOnMenuItemClickListener(item -> {
      finish();
      return true;
    });
    createOptionsMenuEvents.onNext(menu);
    return true;
  }

  @Override
  public void onBackPressed() {
    // don't finish()
    // let user finish() through "Exit" menu item
    Toast.makeText(this, R.string.hint_message_exit_through_menu, Toast.LENGTH_LONG).show();
  }

}
