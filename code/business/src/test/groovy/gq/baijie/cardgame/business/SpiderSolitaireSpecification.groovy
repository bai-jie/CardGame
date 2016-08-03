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

    def "move multiple cards and undo"() {
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
        def step1StateChecker = {
            assert game.state.cardStacks[0].cards.size() == 1
            assert game.state.cardStacks[1].cards.size() == 4
            assert game.state.getCard(CardPosition.of(1, 0)).rank.id == 4
            assert game.state.getCard(CardPosition.of(1, 1)).rank.id == 3
            assert game.state.getCard(CardPosition.of(1, 2)).rank.id == 2
            assert game.state.getCard(CardPosition.of(1, 3)).rank.id == 1
            return true
        }
        step1StateChecker()
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

        when: "undo"
        game.undo()
        then:
        // -state: (to state after step1)
        step1StateChecker()
        // -event: [MoveEvent, MoveEvent, UndoEvent(MoveEvent)]
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(3)
        with(subscriber.onNextEvents[2]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.MoveEvent
            undoneEvent.oldPosition.cardStackIndex == 1
            undoneEvent.oldPosition.cardIndex == 0
            undoneEvent.newPosition.cardStackIndex == 2
            undoneEvent.newPosition.cardIndex == 0
        }
    }

    def "draw cards and undo"() {
        given: "stack: [1,2,3,4,5,6,7,8,9,10], cardsForDrawing: [1,2,3,4,5,6,7,8,9,10]"
        def game = newEmptyGame()
        (1..10).each { game.state.cardStacks[it - 1].cards.add(newCard(it)) }
        (1..10).each { game.state.cardsForDrawing.add(newCard(it)) }
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)
        // checker
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

        when:
        game.undo()
        then:
        // -state: same to given block's state
        (1..10).every { game.state.cardStacks[it - 1].cards.size() == 1 }
        (1..10).every { game.state.cardStacks[it - 1].cards[0].rank.id == it }
        game.state.cardsForDrawing.collect { it.rank.id } == (1..10)
        // -event: [DrawCardsEvent, UndoEvent(DrawCardsEvent)]
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(2)
        with(subscriber.onNextEvents[1]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.DrawCardsEvent
            (0..9).each { assert undoneEvent.drawnCards[it].rank.id == 10 - it }
        }
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

    def "sort out cards when drawing"() {
        given: "stack: [(1),(2),(3),(4),(5),(6),(7),(8),(9),(13..2)], cardsForDrawing: [1,2,3,4,5,6,7,8,9,10], sortedCards: []"
        def game = newEmptyGame()
        // set stack
        (1..9).each { game.state.cardStacks[it - 1].cards.add(newCard(it)) }
        game.state.cardStacks[9].cards.addAll((13..2).collect { newCard it })
        // set cardsForDrawing
        game.state.cardsForDrawing.addAll((1..10).collect { newCard it })
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when:
        game.draw()

        then:
        // -state stack: [(1,10),(2,9),(3,8),(4,7),(5,6),(6,5),(7,4),(8,3),(9,2),()], cardsForDrawing: [], sortedCards: [(13..1)]
        //card stack
        game.state.cardStacks[9].cards.isEmpty()
        (0..8).each { assert game.state.cardStacks[it].cards.size() == 2 }
        (0..8).each { assert game.state.getCard(CardPosition.of(it, 0)).rank.id == it + 1 }
        (0..8).each { assert game.state.getCard(CardPosition.of(it, 1)).rank.id == 10 - it }
        //cardsForDrawing
        game.state.cardsForDrawing.isEmpty()
        //sortedCards
        game.state.sortedCards.size() == 1
        game.state.sortedCards[0].collect { it.rank.id }.containsAll((13..1))
        // -event [DrawEvent, MoveOutEvent]
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(2)
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.DrawCardsEvent
        with(subscriber.onNextEvents[1]) {
            delegate instanceof SpiderSolitaire.State.MoveOutEvent
            cardStackIndex == 9
            cardIndex == 0
        }
    }

    def "don't sort out cards when last card(s) is (K) or (K,Q)"() {
        given: "stack: [(13),(2),(3),(4),(5),(6),(7),(8),(9),(10)], cardsForDrawing: [13,12,11,10,9,8,7,6,5,12], sortedCards: []"
        def game = newEmptyGame()
        // set stack
        game.state.cardStacks[0].cards.add(newCard(13))
        (2..10).each { game.state.cardStacks[it - 1].cards.add(newCard(it)) }
        // set cardsForDrawing
        game.state.cardsForDrawing.addAll((13..5).collect { newCard it })
        game.state.cardsForDrawing.add(newCard(12))
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when:
        game.draw()

        then:
        // -state stack: [(13,12),(2,5),(3,6),(4,7),(5,8),(6,9),(7,10),(8,11),(9,12),(10,13)], cardsForDrawing: [], sortedCards: []
        //card stack
        (0..9).each { assert game.state.cardStacks[it].cards.size() == 2 }
        game.state.getCard(CardPosition.of(0, 0)).rank.id == 13
        (1..9).each { assert game.state.getCard(CardPosition.of(it, 0)).rank.id == it + 1 }
        game.state.getCard(CardPosition.of(0, 1)).rank.id == 12
        (1..9).each { assert game.state.getCard(CardPosition.of(it, 1)).rank.id == it + 4 }
        //cardsForDrawing
        game.state.cardsForDrawing.isEmpty()
        //sortedCards
        game.state.sortedCards.isEmpty()
        // -event [DrawEvent]
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(1)
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.DrawCardsEvent
    }

    def "sort out cards when moving and undo"() {
        given: "stack: [(),(),(),(),(6..1),(),(),(13..7),(),()], cardsForDrawing: [], sortedCards: []"
        final def game = newEmptyGame()
        game.state.cardStacks[4].cards.addAll((6..1).collect { newCard it })
        game.state.cardStacks[7].cards.addAll((13..7).collect { newCard it })
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when: "move (4,0) to (7,7)"
        game.move(CardPosition.of(4, 0), CardPosition.of(7, 7))
        then:
        // -state stack: [(),(),(),(),(),(),(),(),(),()], cardsForDrawing: [], sortedCards: [(13..1)]
        game.state.cardStacks.each { it.cards.isEmpty() }
        game.state.cardsForDrawing.isEmpty()
        game.state.sortedCards.size() == 1
        game.state.sortedCards[0].collect { it.rank.id }.containsAll((13..1))
        // -event [DrawEvent, MoveOutEvent]
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(2)
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.MoveEvent
        with(subscriber.onNextEvents[1]) {
            delegate instanceof SpiderSolitaire.State.MoveOutEvent
            cardStackIndex == 7
            cardIndex == 0
        }

        when:
        game.undo()
        then:
        // -state: (the same to given block's state)
        game.state.cardStacks[4].cards.collect { it.rank.id } == (6..1)
        game.state.cardStacks[7].cards.collect { it.rank.id } == (13..7)
        game.state.cardsForDrawing.isEmpty()
        game.state.sortedCards.isEmpty()
        // -event [MoveEvent, MoveOutEvent, UndoEvent(MoveOutEvent), UndoEvent(MoveEvent)]
        subscriber.assertNotCompleted()
        subscriber.assertValueCount(4)
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.MoveEvent
        subscriber.onNextEvents[1] instanceof SpiderSolitaire.State.MoveOutEvent
        with(subscriber.onNextEvents[2]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.MoveOutEvent
            undoneEvent.cardStackIndex == 7
            undoneEvent.cardIndex == 0
        }
        with(subscriber.onNextEvents[3]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.MoveEvent
            undoneEvent.oldPosition.cardStackIndex == 4
            undoneEvent.oldPosition.cardIndex == 0
            undoneEvent.newPosition.cardStackIndex == 7
            undoneEvent.newPosition.cardIndex == 7
        }
    }

    def "update index when MoveEvent"() {
        given: "stack: [(),(),(),(),((5,6),openIndex:1),(),(),(7),(),()], cardsForDrawing: [], sortedCards: []"
        final def game = newEmptyGame()
        game.state.cardStacks[4].cards.addAll([5, 6].collect { newCard it })
        game.state.cardStacks[4].openIndex = 1
        game.state.cardStacks[7].cards.add(newCard(7))
        game.state.cardStacks[7].openIndex = 0 // 0 is default value
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when: "move (4, 1) to (7, 1)"
        game.move(CardPosition.of(4, 1), CardPosition.of(7, 1))
        then:
        // -state stack: [(),(),(),(),((5),openIndex change to 0),(),(),(7,6),(),()]
        game.state.cardStacks[4].cards.collect { it.rank.id } == [5]
        game.state.cardStacks[4].openIndex == 0
        // -event [MoveEvent, UpdateOpenIndexEvent]
        subscriber.assertNotCompleted()
        subscriber.valueCount == 2
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.MoveEvent
        with(subscriber.onNextEvents[1]) {
            delegate instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
            cardStackIndex == 4
            oldOpenIndex == 1
            newOpenIndex == 0
        }

        when: "move (4, 0) to (7, 2)"
        game.move(CardPosition.of(4, 0), CardPosition.of(7, 2))
        then:
        // -state stack: [(),(),(),(),((),openIndex keep 0),(),(),(7,6,5),(),()]
        game.state.cardStacks[4].cards.isEmpty()
        game.state.cardStacks[4].openIndex == 0
        // -event old events + [MoveEvent]
        subscriber.assertNotCompleted()
        subscriber.valueCount == 3
        subscriber.onNextEvents[2] instanceof SpiderSolitaire.State.MoveEvent
    }

    def "update index when MoveEvent and undo"() {
        given: "stack: [(),(),(),(),((4,5,6),openIndex:2),(),(),(7),(),()], cardsForDrawing: [], sortedCards: []"
        final def game = newEmptyGame()
        game.state.cardStacks[4].cards.addAll([4, 5, 6].collect { newCard it })
        game.state.cardStacks[4].openIndex = 2
        game.state.cardStacks[7].cards.add(newCard(7))
        game.state.cardStacks[7].openIndex = 0 // 0 is default value
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when: "move (4, 2) to (7, 1)"
        game.move(CardPosition.of(4, 2), CardPosition.of(7, 1))
        then:
        // -state stack: [(),(),(),(),((4, 5),openIndex change to 1),(),(),(7,6),(),()]
        game.state.cardStacks[4].cards.collect { it.rank.id } == [4, 5]
        game.state.cardStacks[4].openIndex == 1
        game.state.cardStacks[7].cards.collect { it.rank.id } == [7, 6]
        game.state.cardStacks[7].openIndex == 0
        // -event [MoveEvent, UpdateOpenIndexEvent]
        subscriber.assertNotCompleted()
        subscriber.valueCount == 2
        with(subscriber.onNextEvents[0]) {
            delegate instanceof SpiderSolitaire.State.MoveEvent
            oldPosition.cardStackIndex == 4
            oldPosition.cardIndex == 2
            newPosition.cardStackIndex == 7
            newPosition.cardIndex == 1
        }
        with(subscriber.onNextEvents[1]) {
            delegate instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
            cardStackIndex == 4
            oldOpenIndex == 2
            newOpenIndex == 1
        }

        when: "undo"
        game.undo()
        then:
        // -state: (same to the given block's state)
        game.state.cardStacks[4].cards.collect { it.rank.id } == [4, 5, 6]
        game.state.cardStacks[4].openIndex == 2
        game.state.cardStacks[7].cards.collect { it.rank.id } == [7]
        game.state.cardStacks[7].openIndex == 0
        // -event: [MoveEvent, UpdateOpenIndexEvent, UndoEvent(UpdateOpenIndexEvent), UndoEvent(MoveEvent)]
        subscriber.assertNotCompleted()
        subscriber.valueCount == 4
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.MoveEvent
        subscriber.onNextEvents[1] instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
        with(subscriber.onNextEvents[2]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
            undoneEvent.cardStackIndex == 4
            undoneEvent.oldOpenIndex == 2
            undoneEvent.newOpenIndex == 1
        }
        with(subscriber.onNextEvents[3]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.MoveEvent
            undoneEvent.oldPosition.cardStackIndex == 4
            undoneEvent.oldPosition.cardIndex == 2
            undoneEvent.newPosition.cardStackIndex == 7
            undoneEvent.newPosition.cardIndex == 1
        }
    }

    def "update index when MoveOutEvent and undo"() {
        given: "stack: [(1),(2),(3),(4),(5),(6),(7),(8),(9),((5,5,13..2),openIndex:2)], cardsForDrawing: [1,2,3,4,5,6,7,8,9,10], sortedCards: []"
        def game = newEmptyGame()
        // set stack
        (1..9).each { game.state.cardStacks[it - 1].cards.add(newCard(it)) }
        game.state.cardStacks[9].cards.addAll(([5, 5] + (13..2)).collect { newCard it })
        game.state.cardStacks[9].openIndex = 2
        // set cardsForDrawing
        game.state.cardsForDrawing.addAll((1..10).collect { newCard it })
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when: "draw"
        game.draw()
        then:
        // -state stack: [(1,10),(2,9),(3,8),(4,7),(5,6),(6,5),(7,4),(8,3),(9,2),((5,5),openIndex change to 1)], cardsForDrawing: [], sortedCards: [(13..1)]
        game.state.cardStacks[9].cards.collect { it.rank.id } == [5, 5]
        game.state.cardStacks[9].openIndex == 1
        // -event [DrawCardsEvent, MoveOutEvent, UpdateOpenIndexEvent]
        subscriber.assertNotCompleted()
        subscriber.valueCount == 3
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.DrawCardsEvent
        subscriber.onNextEvents[1] instanceof SpiderSolitaire.State.MoveOutEvent
        with(subscriber.onNextEvents[2]) {
            delegate instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
            cardStackIndex == 9
            oldOpenIndex == 2
            newOpenIndex == 1
        }

        when:
        game.undo()
        then:
        // -state: (the same to given block's state)
        (1..9).every { game.state.cardStacks[it - 1].cards.size() == 1 }
        (1..9).every { game.state.cardStacks[it - 1].cards[0].rank.id == it }
        game.state.cardStacks[9].cards.collect { it.rank.id } == [5, 5] + (13..2)
        game.state.cardStacks[9].openIndex == 2
        game.state.cardsForDrawing.collect { it.rank.id } == (1..10)
        game.state.sortedCards.isEmpty()
        // -event [DrawCardsEvent, MoveOutEvent, UpdateOpenIndexEvent, UndoEvent(UpdateOpenIndexEvent), UndoEvent(MoveOutEvent), UndoEvent(DrawCardsEvent)]
        subscriber.assertNotCompleted()
        subscriber.valueCount == 6
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.DrawCardsEvent
        subscriber.onNextEvents[1] instanceof SpiderSolitaire.State.MoveOutEvent
        subscriber.onNextEvents[2] instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
        with(subscriber.onNextEvents[3]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
            undoneEvent.cardStackIndex == 9
            undoneEvent.oldOpenIndex == 2
            undoneEvent.newOpenIndex == 1
        }
        with(subscriber.onNextEvents[4]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.MoveOutEvent
            undoneEvent.cardStackIndex == 9
            undoneEvent.cardIndex == 2
        }
        with(subscriber.onNextEvents[5]) {
            delegate instanceof SpiderSolitaire.State.UndoEvent
            undoneEvent instanceof SpiderSolitaire.State.DrawCardsEvent
            undoneEvent.drawnCards.collect { it.rank.id } == (10..1)
        }
    }

    def "update index when MoveEvent with MoveOutEvent"() {
        given: "stack: [(),(),(),(),((1,6..1),openIndex:1),(),(),((13,13..7),openIndex:1),(),()], cardsForDrawing: [], sortedCards: []"
        final def game = newEmptyGame()
        game.state.cardStacks[4].cards.addAll(([1] + (6..1)).collect { newCard it })
        game.state.cardStacks[4].openIndex = 1
        game.state.cardStacks[7].cards.addAll(([13] + (13..7)).collect { newCard it })
        game.state.cardStacks[7].openIndex = 1
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when: "move (4,1) to (7,8)"
        game.move(CardPosition.of(4, 1), CardPosition.of(7, 8))

        then:
        // -state stack: [(),(),(),(),((1),openIndex change to 0),(),(),(13,openIndex change to 0),(),()], cardsForDrawing: [], sortedCards: [(13..1)]
        game.state.cardStacks[4].cards.collect { it.rank.id } == [1]
        game.state.cardStacks[4].openIndex == 0
        game.state.cardStacks[7].cards.collect { it.rank.id } == [13]
        game.state.cardStacks[7].openIndex == 0
        // -event [MoveEvent, MoveOutEvent, UpdateOpenIndexEvent, UpdateOpenIndexEvent]
        subscriber.assertNotCompleted()
        subscriber.valueCount == 4
        subscriber.onNextEvents[0] instanceof SpiderSolitaire.State.MoveEvent
        subscriber.onNextEvents[1] instanceof SpiderSolitaire.State.MoveOutEvent
        with(subscriber.onNextEvents[2]) {
            delegate instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
            cardStackIndex == 4
            oldOpenIndex == 1
            newOpenIndex == 0
        }
        with(subscriber.onNextEvents[3]) {
            delegate instanceof SpiderSolitaire.State.UpdateOpenIndexEvent
            cardStackIndex == 7
            oldOpenIndex == 1
            newOpenIndex == 0
        }
    }

    def "cannot undo when just start game"() {
        given:
        final def game = newEmptyGame()
        def canUndoResult
        def undoResult
        // event logger
        def subscriber = TestSubscriber.create()
        game.state.eventBus.subscribe(subscriber)

        when:
        canUndoResult = game.canUndo()
        undoResult = game.undo()

        then:
        canUndoResult == false
        undoResult == false
        // -event []
        subscriber.assertNotCompleted()
        subscriber.valueCount == 0
    }

}
