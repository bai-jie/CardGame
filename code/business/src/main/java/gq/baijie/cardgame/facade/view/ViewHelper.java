package gq.baijie.cardgame.facade.view;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class ViewHelper {

  private Subject<View.Event, View.Event> eventBus = PublishSubject.create();

  public void nextEvent(View.Event event) {
    eventBus.onNext(event);
  }

  public Observable<View.Event> getEventBus() {
    return eventBus.asObservable();
  }

}
