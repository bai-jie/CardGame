package gq.baijie.cardgame.facade.view;

public interface View {

  class Event extends gq.baijie.cardgame.facade.view.Event {
    final View origin;

    public Event(View origin) {
      this.origin = origin;
    }
  }

}
