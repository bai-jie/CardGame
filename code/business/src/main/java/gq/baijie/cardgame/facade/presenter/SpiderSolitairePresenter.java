package gq.baijie.cardgame.facade.presenter;

import gq.baijie.cardgame.business.SpiderSolitaire;
import gq.baijie.cardgame.business.SpiderSolitaire.CardPosition;
import gq.baijie.cardgame.business.SpiderSolitaire.State.DrawCardsEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.MoveEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.MoveOutEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.UpdateOpenIndexEvent;
import gq.baijie.cardgame.facade.view.SpiderSolitaireView;
import rx.Observable;
import rx.functions.Action1;

public class SpiderSolitairePresenter {

  private final SpiderSolitaire game;

  private final SpiderSolitaireView view;

  public SpiderSolitairePresenter(SpiderSolitaire game, SpiderSolitaireView view) {
    this.game = game;
    this.view = view;
    init();
  }

  private void init() {
    // init view
    view.init(this);
    // bind game events to view
    final Observable<Object> eventBus = game.getState().getEventBus();
    eventBus.ofType(MoveEvent.class).subscribe(new Action1<MoveEvent>() {
      @Override
      public void call(MoveEvent moveEvent) {
        view.moveCards(
            moveEvent.oldPosition.cardStackIndex,
            moveEvent.oldPosition.cardIndex,
            moveEvent.newPosition.cardStackIndex,
            moveEvent.newPosition.cardIndex
        );
      }
    });
    eventBus.ofType(DrawCardsEvent.class).subscribe(new Action1<DrawCardsEvent>() {
      @Override
      public void call(DrawCardsEvent drawCardsEvent) {
        view.drawCards(drawCardsEvent.drawnCards);
      }
    });
    eventBus.ofType(MoveOutEvent.class).subscribe(new Action1<MoveOutEvent>() {
      @Override
      public void call(MoveOutEvent moveOutEvent) {
        view.moveOutSortedCards(moveOutEvent.cardStackIndex, moveOutEvent.cardIndex);
      }
    });
    eventBus.ofType(UpdateOpenIndexEvent.class).subscribe(new Action1<UpdateOpenIndexEvent>() {
      @Override
      public void call(UpdateOpenIndexEvent event) {
        view.updateOpenIndex(event.cardStackIndex, event.newOpenIndex);
      }
    });
  }

  public SpiderSolitaire getGame() {
    return game;
  }

  public SpiderSolitaireView getView() {
    return view;
  }

  public boolean canMoveCards(int oldCardStackIndex, int oldCardIndex, int newCardStackIndex) {
    return game.canMove(
        CardPosition.of(oldCardStackIndex, oldCardIndex),
        CardPosition.of(newCardStackIndex, 0)
    );
  }

  public void moveCards(int oldCardStackIndex, int oldCardIndex, int newCardStackIndex) {
    if (!canMoveCards(oldCardStackIndex, oldCardIndex, newCardStackIndex)) {
      return;
    }
    game.move(
        CardPosition.of(oldCardStackIndex, oldCardIndex),
        CardPosition.of(newCardStackIndex, 0)
    );
  }

  public boolean canDrawCards() {
    return game.canDraw();
  }

  public void drawCards() {
    if (!canDrawCards()) {
      return;
    }
    game.draw();
  }

}
