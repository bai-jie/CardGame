package gq.baijie.cardgame.facade.view;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class EventBusHelper<T extends Event> implements EventSource<T> {

  private Subject<T, T> eventBus = PublishSubject.create();

  public static <T extends Event> EventBusHelper<T> create() {
    return new EventBusHelper<>();
  }

  public void nextEvent(T event) {
    eventBus.onNext(event);
  }

  public Observable<T> getEventBus() {
    return eventBus.asObservable();
  }

}
