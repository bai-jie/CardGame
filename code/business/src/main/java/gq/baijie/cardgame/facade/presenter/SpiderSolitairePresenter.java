package gq.baijie.cardgame.facade.presenter;

import gq.baijie.cardgame.business.SpiderSolitaire;
import gq.baijie.cardgame.business.SpiderSolitaire.CardPosition;
import gq.baijie.cardgame.business.SpiderSolitaire.State.DrawCardsEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.MoveEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.MoveOutEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.UndoEvent;
import gq.baijie.cardgame.business.SpiderSolitaire.State.UpdateOpenIndexEvent;
import gq.baijie.cardgame.domain.entity.Card;
import gq.baijie.cardgame.facade.view.DrawingCardsView;
import gq.baijie.cardgame.facade.view.SortedCardsView;
import gq.baijie.cardgame.facade.view.SpiderSolitaireView;
import rx.Observable;
import rx.functions.Action1;

public class SpiderSolitairePresenter {

  private final SpiderSolitaire game;

  private final SpiderSolitaireView view;

  private final DrawingCardsView drawingCardsView;

  private final SortedCardsView sortedCardsView;

  public SpiderSolitairePresenter(
      SpiderSolitaire game,
      SpiderSolitaireView view,
      DrawingCardsView drawingCardsView,
      SortedCardsView sortedCardsView
  ) {
    this.game = game;
    this.view = view;
    this.drawingCardsView = drawingCardsView;
    this.sortedCardsView = sortedCardsView;
    init();
  }

  private void init() {
    // init view
    view.init(this);
    updateDrawingCardsView();
    view.setDrawingCardsView(drawingCardsView);
    updateSortedCardsView();
    view.setSortedCardsView(sortedCardsView);
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
        updateDrawingCardsView();
      }
    });
    eventBus.ofType(MoveOutEvent.class).subscribe(new Action1<MoveOutEvent>() {
      @Override
      public void call(MoveOutEvent moveOutEvent) {
        view.moveOutSortedCards(moveOutEvent.cardStackIndex, moveOutEvent.cardIndex);
        updateSortedCardsView();
      }
    });
    eventBus.ofType(UpdateOpenIndexEvent.class).subscribe(new Action1<UpdateOpenIndexEvent>() {
      @Override
      public void call(UpdateOpenIndexEvent event) {
        view.updateOpenIndex(event.cardStackIndex, event.oldOpenIndex, event.newOpenIndex);
      }
    });
    eventBus.ofType(UndoEvent.class).subscribe(new Action1<UndoEvent>() {
      @Override
      public void call(UndoEvent undoEvent) {
        onUndo(undoEvent);
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

  private void updateDrawingCardsView() {
    drawingCardsView.setDecks(game.getState().cardsForDrawing.size() / 10);
  }

  private void updateSortedCardsView() {
    sortedCardsView.setDecks(game.getState().sortedCards.size());
  }

  private void onUndo(UndoEvent undoEvent) {
    final Object undoneEvent = undoEvent.undoneEvent;
    Class<?> eventClass = undoneEvent.getClass();
    if (eventClass.equals(MoveEvent.class)) {
      onUndoMove((MoveEvent) undoneEvent);
    } else if (eventClass.equals(DrawCardsEvent.class)) {
      onUndoDraw((DrawCardsEvent) undoneEvent);
    } else if (eventClass.equals(UpdateOpenIndexEvent.class)) {
      onUndoUpdateOpenIndex((UpdateOpenIndexEvent) undoneEvent);
    } else if (eventClass.equals(MoveOutEvent.class)) {
      onUndoMoveOutSortedCards((MoveOutEvent) undoneEvent);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private void onUndoMove(MoveEvent undoneEvent) {
    view.moveCards(
        undoneEvent.newPosition.cardStackIndex,
        undoneEvent.newPosition.cardIndex,
        undoneEvent.oldPosition.cardStackIndex,
        undoneEvent.oldPosition.cardIndex);
  }

  private void onUndoDraw(DrawCardsEvent undoneEvent) {
    view.undoDrawCards(undoneEvent.drawnCards);
    updateDrawingCardsView();
  }

  private void onUndoUpdateOpenIndex(UpdateOpenIndexEvent undoneEvent) {
    view.updateOpenIndex(
        undoneEvent.cardStackIndex, undoneEvent.newOpenIndex, undoneEvent.oldOpenIndex);
  }

  private void onUndoMoveOutSortedCards(MoveOutEvent undoneEvent) {
    final Card[] movedCards = new Card[13];
    for (int i = 0; i < 13; i++) {
      movedCards[i] = game.getState().getCard(
          CardPosition.of(undoneEvent.cardStackIndex, undoneEvent.cardIndex + i));
    }
    view.undoMoveOutSortedCards(undoneEvent.cardStackIndex, undoneEvent.cardIndex, movedCards);
    updateSortedCardsView();
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

  public boolean canUndo() {
    return game.canUndo();
  }

  public boolean undo() {
    return game.undo();
  }
}
