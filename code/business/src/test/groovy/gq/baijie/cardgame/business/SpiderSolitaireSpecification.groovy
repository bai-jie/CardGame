package gq.baijie.cardgame.business

import gq.baijie.cardgame.business.SpiderSolitaire.CardPosition
import rx.observers.TestSubscriber
import spock.lang.Specification

import static gq.baijie.cardgame.business.SpiderSolitaireTestUtils.newCard
import static gq.baijie.cardgame.business.SpiderSolitaireTestUtils.newEmptyGame

class SpiderSolitaireSpecification extends Specification {

    def "move one card"() {
        given:
        def game = newEmptyGame()
        game.state.cardStacks[0].cards.add(newCard(1))
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when:
        game.move(CardPosition.of(0, 0), CardPosition.of(1, 0))
        then:
        game.state.cardStacks[0].cards.isEmpty()
        game.state.cardStacks[1].cards.size() == 1
        game.state.getCard(CardPosition.of(1, 0)).rank.id == 1
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(1)
        with(subscriber.onNextEvents[0]) {
            delegate instanceof SpiderSolitaire.State.MoveEvent
            oldPosition.cardStackIndex == 0
            oldPosition.cardIndex == 0
            newPosition.cardStackIndex == 1
            newPosition.cardIndex == 0
        }
    }

    def "move multiple cards"() {
        given: "cards[1, 3, 2, 1], cards[4]"
        def game = newEmptyGame()
        game.state.cardStacks[0].cards.with {
            add(newCard(1))
            add(newCard(3))
            add(newCard(2))
            add(newCard(1))
        }
        game.state.cardStacks[1].cards.add(newCard(4))
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when: "move cards[3, 2, 1] from (0, 1) to (1, 1)"
        game.move(CardPosition.of(0, 1), CardPosition.of(1, 1))
        then:
        game.state.cardStacks[0].cards.size() == 1
        game.state.cardStacks[1].cards.size() == 4
        game.state.getCard(CardPosition.of(1, 0)).rank.id == 4
        game.state.getCard(CardPosition.of(1, 1)).rank.id == 3
        game.state.getCard(CardPosition.of(1, 2)).rank.id == 2
        game.state.getCard(CardPosition.of(1, 3)).rank.id == 1
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(1)
        with(subscriber.onNextEvents[0]) {
            delegate instanceof SpiderSolitaire.State.MoveEvent
            oldPosition.cardStackIndex == 0
            oldPosition.cardIndex == 1
            newPosition.cardStackIndex == 1
            newPosition.cardIndex == 1
        }

        when: "move cards[4, 3, 2, 1] from (1, 0) to (2, 0)"
        game.move(CardPosition.of(1, 0), CardPosition.of(2, 0))
        then:
        game.state.cardStacks[0].cards.size() == 1
        game.state.cardStacks[1].cards.size() == 0
        game.state.cardStacks[2].cards.size() == 4
        game.state.getCard(CardPosition.of(2, 0)).rank.id == 4
        game.state.getCard(CardPosition.of(2, 1)).rank.id == 3
        game.state.getCard(CardPosition.of(2, 2)).rank.id == 2
        game.state.getCard(CardPosition.of(2, 3)).rank.id == 1
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(2)
        with(subscriber.onNextEvents[1]) {
            delegate instanceof SpiderSolitaire.State.MoveEvent
            oldPosition.cardStackIndex == 1
            oldPosition.cardIndex == 0
            newPosition.cardStackIndex == 2
            newPosition.cardIndex == 0
        }
    }

    def "draw cards"() {
        given: "stack: [1,2,3,4,5,6,7,8,9,10], cardsForDrawing: [1,2,3,4,5,6,7,8,9,10]"
        def game = newEmptyGame()
        (1..10).each { game.state.cardStacks[it - 1].cards.add(newCard(it)) }
        (1..10).each { game.state.cardsForDrawing.add(newCard(it)) }
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)
        // check
        // stack: [(1,10),(2,9),(3,8),(4,7),(5,6),(6,5),(7,4),(8,3),(9,2),(10,1)], cardsForDrawing: []
        def checker = {
            assert game.state.cardsForDrawing.isEmpty()
            (0..9).each { assert game.state.cardStacks[it].cards.size() == 2 }
            (0..9).each { assert game.state.getCard(CardPosition.of(it, 0)).rank.id == it + 1 }
            (0..9).each { assert game.state.getCard(CardPosition.of(it, 1)).rank.id == 10 - it }
            subscriber.assertNotCompleted()
            subscriber.assertValueCount(1)
            with(subscriber.onNextEvents[0]) {
                delegate instanceof SpiderSolitaire.State.DrawCardsEvent
                (0..9).each { assert drawnCards[it].rank.id == 10 - it }
            }
            return true
        }

        when: "draw cards"
        game.draw() // draw from the tail of cardsForDrawing
        then:
        checker()

        when: "draw again"
        game.draw()
        then:
        thrown(IllegalStateException) // because cardsForDrawing is empty
        checker()
    }

    def "draw when have empty card stack"() {
        given: "stack: [(1),(2),(3),(4),(),(6),(7),(8),(9),(10)], cardsForDrawing: [1,2,3,4,5,6,7,8,9,10]"
        def game = newEmptyGame()
        (1..10).each { game.state.cardStacks[it - 1].cards.add(newCard(it)) }
        game.state.cardStacks[4].cards.clear()
        (1..10).each { game.state.cardsForDrawing.add(newCard(it)) }
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when:
        game.draw()
        then:
        thrown(IllegalStateException)
        // assert stack: [(1),(2),(3),(4),(),(6),(7),(8),(9),(10)]
        game.state.cardStacks[4].cards.isEmpty()
        [0, 1, 2, 3, 5, 6, 7, 8, 9].each {
            assert game.state.cardStacks[it].cards.size() == 1
            assert game.state.getCard(CardPosition.of(it, 0)).rank.id == it + 1
        }
        // assert cardsForDrawing: [1,2,3,4,5,6,7,8,9,10]
        with(game.state.cardsForDrawing) {
            size() == 10
            (1..10).each { get(it - 1).rank.id == it }
        }
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(0)
    }

}
