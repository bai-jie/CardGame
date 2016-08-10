package gq.baijie.cardgame.facade.view;

public interface DrawingCardsView extends View, EventSource<DrawingCardsView.DrawEvent> {

  void setDecks(int decks);

  class DrawEvent extends View.Event {

    public DrawEvent(View origin) {
      super(origin);
    }

  }

}
