package gq.baijie.cardgame.facade.presenter;

import gq.baijie.cardgame.business.SpiderSolitaire;
import gq.baijie.cardgame.business.SpiderSolitaire.CardPosition;
import gq.baijie.cardgame.business.SpiderSolitaire.State.DrawCardsEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.MoveEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.MoveOutEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.UpdateOpenIndexEvent;
import gq.baijie.cardgame.facade.view.DrawingCardsView;
import gq.baijie.cardgame.facade.view.SpiderSolitaireView;
import rx.Observable;
import rx.functions.Action1;

public class SpiderSolitairePresenter {

  private final SpiderSolitaire game;

  private final SpiderSolitaireView view;

  private final DrawingCardsView drawingCardsView;

  public SpiderSolitairePresenter(
      SpiderSolitaire game, SpiderSolitaireView view, DrawingCardsView drawingCardsView) {
    this.game = game;
    this.view = view;
    this.drawingCardsView = drawingCardsView;
    init();
  }

  private void init() {
    // init view
    view.init(this);
    drawingCardsView.setDecks(game.getState().cardsForDrawing.size() / 10);
    view.setDrawingCardsView(drawingCardsView);
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
        drawingCardsView.setDecks(game.getState().cardsForDrawing.size() / 10);
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
    // bind events from drawingCardsView
    drawingCardsView.getEventBus().ofType(DrawingCardsView.DrawEvent.class).subscribe(
        new Action1<DrawingCardsView.DrawEvent>() {
          @Override
          public void call(DrawingCardsView.DrawEvent drawEvent) {
            drawCards();
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
