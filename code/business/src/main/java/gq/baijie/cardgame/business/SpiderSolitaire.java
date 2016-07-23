package gq.baijie.cardgame.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gq.baijie.cardgame.domain.entity.Card;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class SpiderSolitaire {

  private final State state;

  public SpiderSolitaire(State state) {
    this.state = state;
    init();
  }

  //TODO MoveEvent, DrawCardsEvent, MoveOutEvent maybe volatile
  private void init() {
    // * checkIsSortedOut when add cards to cardStack
    // ** checkIsSortedOut when MoveEvent
    state.getEventBus().ofType(State.MoveEvent.class).subscribe(new Action1<State.MoveEvent>() {
      @Override
      public void call(State.MoveEvent moveEvent) {
        checkIsSortedOut(moveEvent.newPosition.cardStackIndex);
      }
    });
    // ** checkIsSortedOut when DrawCardsEvent
    state.getEventBus().ofType(State.DrawCardsEvent.class).subscribe(
        new Action1<State.DrawCardsEvent>() {
          @Override
          public void call(State.DrawCardsEvent drawCardsEvent) {
            for (int i = 0; i < state.cardStacks.size(); i++) {
              checkIsSortedOut(i);
            }
          }
        });
    // * updateIndex when remove cards from cardStack
    // ** updateIndex when MoveEvent
    state.getEventBus().ofType(State.MoveEvent.class).subscribe(new Action1<State.MoveEvent>() {
      @Override
      public void call(State.MoveEvent moveEvent) {
        updateIndexIfNeed(moveEvent.oldPosition.cardStackIndex);
      }
    });
    // ** updateIndex when MoveOutEvent
    state.getEventBus().ofType(State.MoveOutEvent.class).subscribe(
        new Action1<State.MoveOutEvent>() {
          @Override
          public void call(State.MoveOutEvent moveOutEvent) {
            updateIndexIfNeed(moveOutEvent.cardStackIndex);
          }
        });
  }

  private boolean checkIsSortedOut(final int cardStackIndex) {
    if (!state.isLegalCardStackIndex(cardStackIndex)) {
      throw new IllegalArgumentException();
    }
    final State.CardStack cardStack = state.cardStacks.get(cardStackIndex);

    if (cardStack.cards.isEmpty()) {
      return false;
    }

    Card biggestCard = cardStack.cards.get(cardStack.cards.size() - 1);
    for (int i = cardStack.cards.size() - 2; i >= 0 && i >= cardStack.openIndex; i--) {
      Card currentCard = cardStack.cards.get(i);
      // * is sequential
      if (currentCard.getRank().getId() == biggestCard.getRank().getId() + 1) {
        biggestCard = currentCard;
      }
    }
    if (biggestCard.getRank() != Card.Rank.KING) {
      return false;
    }
    // do move out cards sorted out
    final List<Card> moved = new ArrayList<>(13);
    final int position = cardStack.cards.size() - 13;
    while (cardStack.cards.size() > position) {
      moved.add(cardStack.cards.remove(position));
    }
    state.sortedCards.add(moved);
    state.nextEvent(new State.MoveOutEvent(cardStackIndex, position));
    return true;
  }

  private boolean updateIndexIfNeed(final int cardStackIndex) {
    if (!state.isLegalCardStackIndex(cardStackIndex)) {
      throw new IllegalArgumentException();
    }
    final State.CardStack cardStack = state.cardStacks.get(cardStackIndex);
    // cardStack.cards is empty -> cardStack.openIndex should be [0, cardStack.cards.size] that is 0
    if (cardStack.cards.isEmpty()) {
      assert cardStack.openIndex >= 0;
      if (cardStack.openIndex != 0) {
        final int oldOpenIndex = cardStack.openIndex;
        cardStack.openIndex = 0;
        state.nextEvent(new State.UpdateOpenIndexEvent(cardStackIndex, oldOpenIndex, 0));
        return true;
      } else { // cardStack.openIndex == 0
        return false;
      }
    }
    // cardStack.cards nonempty -> cardStack.openIndex should be [0, cardStack.cards.size)
    assert cardStack.openIndex <= cardStack.cards.size();
    if (cardStack.openIndex > cardStack.cards.size() - 1) {
      final int oldOpenIndex = cardStack.openIndex;
      cardStack.openIndex = cardStack.cards.size() - 1;
      state.nextEvent(
          new State.UpdateOpenIndexEvent(cardStackIndex, oldOpenIndex, cardStack.openIndex));
      return true;
    } else {
      return false;
    }
  }

  public State getState() {
    return state;
  }

  public boolean canMove(CardPosition from, CardPosition to) {//TODO check openIndex
    try {
      to.cardIndex = state.cardStacks.get(to.cardStackIndex).cards.size();
    } catch (IndexOutOfBoundsException e) {
      //ignore
    }
    // * can move naturally
    if (!state.canMove(from, to)) {
      return false;
    }
    // * from is sequential //TODO check card's Suit
    State.CardStack cardStackOfFrom = state.cardStacks.get(from.cardStackIndex);
    Card lastCard = cardStackOfFrom.cards.get(from.cardIndex);
    for (int i = from.cardIndex + 1; i < cardStackOfFrom.cards.size(); i++) {
      final Card currentCard = cardStackOfFrom.cards.get(i);
      if (lastCard.getRank().getId() != currentCard.getRank().getId() + 1) {
        return false;
      }
      lastCard = currentCard;
    }
    // * after moved is sequential that is:
    //   - case 1: to is empty card stack
    //   - case 2:'s card + 1 == to's last card
    //TODO check card's Suit
    if (!state.cardStacks.get(to.cardStackIndex).cards.isEmpty() && (
        state.getCard(from).getRank().getId() + 1 !=
        state.cardStacks.get(to.cardStackIndex).cards.get(to.cardIndex - 1).getRank().getId()
    )) {
      return false;
    }
    return true;
  }

  public void move(CardPosition from, CardPosition to) {
    try {
      to.cardIndex = state.cardStacks.get(to.cardStackIndex).cards.size();
    } catch (IndexOutOfBoundsException e) {
      //ignore
    }
    if (!canMove(from, to)) {
      throw new IllegalArgumentException();
    }
    state.move(from, to);
  }

  public boolean canDraw() {
    // * can draw naturally
    if (!state.canDraw()) {
      return false;
    }
    //* all cardStack nonempty //TODO real check this?
    for (State.CardStack cardStack : state.cardStacks) {
      if (cardStack.cards.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public void draw() {
    if (!canDraw()) {
      throw new IllegalStateException();
    }
    state.draw();
  }

  public static class State {

    /** left to right, 0 to 9 */
    public final List<CardStack> cardStacks;

    /** cards for drawing, initially in five piles of ten with no cards showing */
    public final List<Card> cardsForDrawing = new ArrayList<>(50);

    /** cards have been sorted out */
    public final List<List<Card>> sortedCards = new LinkedList<>();

    private final Subject<Object, Object> eventbus = PublishSubject.create();
    private final Scheduler.Worker eventBusWorker = Schedulers.trampoline().createWorker();//TODO think more

    public State() {
      List<CardStack> cardStacks = new ArrayList<>(10);
      for (int i = 0; i < 10; i++) {
        cardStacks.add(new CardStack());
      }
      this.cardStacks = Collections.unmodifiableList(cardStacks);
    }

    public Observable<Object> getEventBus() {
      return eventbus.asObservable();
    }
    void nextEvent(final Object event) {
      eventBusWorker.schedule(new Action0() {
        @Override
        public void call() {
          eventbus.onNext(event);
        }
      });
    }

    public boolean isLegalCardStackIndex(int cardStackIndex) {
      return cardStackIndex >= 0 && cardStackIndex < cardStacks.size();
    }

    public boolean hasCard(CardPosition position) {
      int stackIndex = position.cardStackIndex;
      int cardIndex = position.cardIndex;
      return isLegalCardStackIndex(stackIndex)
             && cardIndex >= 0 && cardIndex < cardStacks.get(stackIndex).cards.size();
    }

    public Card getCard(CardPosition position) {
      if (!hasCard(position)) {
        return null;
      }
      return cardStacks.get(position.cardStackIndex).cards.get(position.cardIndex);
    }

    boolean canMove(CardPosition from, CardPosition to) {
      // * from has card
      if (!hasCard(from)) {
        return false;
      }
      // * to's cardIndex is the biggest index in card stack + 1 (which is cardStack.cards.size())
      if (!isLegalCardStackIndex(to.cardStackIndex)
          || to.cardIndex != cardStacks.get(to.cardStackIndex).cards.size()) {
        return false;
      }
      return true;
    }

    void move(CardPosition from, CardPosition to) {
      if (!canMove(from, to)) {
        throw new IllegalArgumentException();
      }
      final List<Card> src = cardStacks.get(from.cardStackIndex).cards;
      final List<Card> dest = cardStacks.get(to.cardStackIndex).cards;
      while (src.size() > from.cardIndex) {
        dest.add(src.remove(from.cardIndex));
      }
      nextEvent(new MoveEvent(from, to));
    }

    boolean canDraw() {
      // * state.cardsForDrawing nonempty
      return !cardsForDrawing.isEmpty();
    }

    void draw() {
      if (!canDraw()) {
        throw new IllegalArgumentException();
      }
      final Card[] cards = new Card[10];
      for (int i = 0; i < cards.length; i++) {
        final Card card = cardsForDrawing.remove(cardsForDrawing.size() - 1);
        cards[i] = card;
        cardStacks.get(i).cards.add(card);
      }
      nextEvent(new DrawCardsEvent(cards));
    }

    public static class CardStack {

      public List<Card> cards = new ArrayList<>();
      /**
       * hide: [0, openIndex), open: [openIndex, cards.size())<br>
       * == 0 if cards.isEmpty()
       */
      int openIndex;
    }

    public static class MoveEvent {
      public final CardPosition oldPosition;
      public final CardPosition newPosition;

      public MoveEvent(CardPosition oldPosition, CardPosition newPosition) {
        this.oldPosition = oldPosition;
        this.newPosition = newPosition;
      }
    }

    public static class DrawCardsEvent {
      /** index is same to index for {@link State#cardStacks} */
      public final Card[] drawnCards;//TODO make it unmodifiable?

      public DrawCardsEvent(Card[] drawnCards) {
        this.drawnCards = drawnCards;
      }
    }

    public static class MoveOutEvent {
      public final int cardStackIndex;
      public final int cardIndex;

      public MoveOutEvent(int cardStackIndex, int cardIndex) {
        this.cardStackIndex = cardStackIndex;
        this.cardIndex = cardIndex;
      }
    }

    public static class UpdateOpenIndexEvent {
      public final int cardStackIndex;
      public final int oldOpenIndex;
      public final int newOpenIndex;

      public UpdateOpenIndexEvent(int cardStackIndex, int oldOpenIndex, int newOpenIndex) {
        this.cardStackIndex = cardStackIndex;
        this.oldOpenIndex = oldOpenIndex;
        this.newOpenIndex = newOpenIndex;
      }
    }

  }

  public static class CardPosition {

    public int cardStackIndex;
    /** card index in card stack */
    public int cardIndex;

    public CardPosition() {
    }

    public CardPosition(int cardStackIndex, int cardIndex) {
      this.cardStackIndex = cardStackIndex;
      this.cardIndex = cardIndex;
    }

    public static CardPosition of(int cardStackIndex, int cardIndex) {
      return new CardPosition(cardStackIndex, cardIndex);
    }
  }

}
