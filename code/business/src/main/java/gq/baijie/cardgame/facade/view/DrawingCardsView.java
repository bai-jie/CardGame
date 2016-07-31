package gq.baijie.cardgame.facade.view;

public interface DrawingCardsView extends View {

  void setDecks(int decks);

  class DrawEvent extends Event {

    public DrawEvent(View origin) {
      super(origin);
    }

  }

}
