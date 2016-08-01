package gq.baijie.cardgame.business;

import java.util.LinkedList;

public class RamEventLogger<E> implements EventLogger<E> {

  private LinkedList<E> events = new LinkedList<>();

  @Override
  public void push(E event) {
    events.push(event);
  }

  @Override
  public E pop() {
    return events.pop();
  }

  @Override
  public boolean isEmpty() {
    return events.isEmpty();
  }

}
