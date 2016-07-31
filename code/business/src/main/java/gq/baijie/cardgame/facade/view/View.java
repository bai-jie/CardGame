package gq.baijie.cardgame.facade.view;

import rx.Observable;

public interface View {

  Observable<Event> getEventBus();

  class Event {
    final View origin;

    public Event(View origin) {
      this.origin = origin;
    }
  }

}
