package gq.baijie.cardgame.business;

public interface EventLogger<E> {

  void push(E event);

  E pop();

  boolean isEmpty();

}
