package gq.baijie.cardgame.business;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import gq.baijie.cardgame.domain.entity.Card;

import static gq.baijie.cardgame.business.SpiderSolitaire.CardPosition;
import static gq.baijie.cardgame.business.SpiderSolitaireTest.Generators.newEmptyGame;
import static org.junit.Assert.assertEquals;


public class SpiderSolitaireTest {

  @Test
  public void testMoveToEmptyCardStack() {
    // when
    SpiderSolitaire game = newEmptyGame();
    game.getState().cardStacks.get(0).cards.addAll(
        CardsBuilder.create().add(3).add(2).add(1).build());
    // if
    game.move(CardPosition.of(0, 0), CardPosition.of(1, 0));
    // then
    assertEquals(0, game.getState().cardStacks.get(0).cards.size());
    assertEquals(3, game.getState().cardStacks.get(1).cards.size());
    assertEquals(3, game.getState().getCard(CardPosition.of(1, 0)).getRank().getId());
    assertEquals(2, game.getState().getCard(CardPosition.of(1, 1)).getRank().getId());
    assertEquals(1, game.getState().getCard(CardPosition.of(1, 2)).getRank().getId());
  }

  @Test
  public void testMoveToNonemptyCardStack() {
    // when
    SpiderSolitaire game = newEmptyGame();
    game.getState().cardStacks.get(0).cards.addAll(
        CardsBuilder.create().add(3).add(2).add(1).build());
    game.getState().cardStacks.get(1).cards.add(new Card(Card.Suit.HEART, Card.Rank.FOUR));
    // if
    game.move(CardPosition.of(0, 0), CardPosition.of(1, 1));
    // then
    assertEquals(0, game.getState().cardStacks.get(0).cards.size());
    assertEquals(4, game.getState().cardStacks.get(1).cards.size());
    assertEquals(4, game.getState().getCard(CardPosition.of(1, 0)).getRank().getId());
    assertEquals(3, game.getState().getCard(CardPosition.of(1, 1)).getRank().getId());
    assertEquals(2, game.getState().getCard(CardPosition.of(1, 2)).getRank().getId());
    assertEquals(1, game.getState().getCard(CardPosition.of(1, 3)).getRank().getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCannotMoveNonsequentialCardStack() {
    // when
    SpiderSolitaire game = newEmptyGame();
    game.getState().cardStacks.get(0).cards.addAll(
        CardsBuilder.create().add(3).add(2).add(3).build());
    // if
    game.move(CardPosition.of(0, 0), CardPosition.of(1, 0));
    // should throw Exception
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCannotBeNonsequentialAfterMoved() {
    // when
    SpiderSolitaire game = newEmptyGame();
    game.getState().cardStacks.get(0).cards.addAll(
        CardsBuilder.create().add(3).add(2).add(1).build());
    game.getState().cardStacks.get(1).cards.add(new Card(Card.Suit.HEART, Card.Rank.FIVE));
    // if
    game.move(CardPosition.of(0, 0), CardPosition.of(1, 1));
    // should throw Exception
  }


  static class Generators {

    static SpiderSolitaire newEmptyGame() {
      SpiderSolitaire.State state = new SpiderSolitaire.State();
      return new SpiderSolitaire(state);
    }
  }

  static class CardsBuilder {

    List<Card> cards = new LinkedList<>();

    static CardsBuilder create() {
      return new CardsBuilder();
    }

    public CardsBuilder add(Card.Suit suit, Card.Rank rank) {
      cards.add(new Card(suit, rank));
      return this;
    }

    public CardsBuilder add(Card.Rank rank) {
      add(Card.Suit.HEART, rank);
      return this;
    }

    public CardsBuilder add(int rank) {
      add(Card.Rank.fromId(rank));
      return this;
    }

    public List<Card> build() {
      return cards;
    }
  }

}
