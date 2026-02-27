package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.Board
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.c
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

class DOTC: WithAssertions {

    fun Map<Player, List<Order>>.adjudicateAsDOTC(setup: Map<Province, Player> = impliedSetup()): Board? {
        forEach { (_, orders) -> orders.forEach {  if (it is MoveOrder) it i 0} }
        val game = Game(setup)
        forEach { (player, orders) -> game.input(orders, player) }
        game.adjudicate()
        return game.getBoard(BoardIndex(1.c))
    }

    fun Map<Player, List<Order>>.impliedSetup(): Map<Province,  Player> =
        asSequence().flatMap { (player, orders) -> orders.map { it.from.province to player } }.toMap()

    //6.A. TEST CASES, BASIC CHECKS

    @Test
    fun Testcase_6_A_1_MOVING_TO_AN_AREA_THAT_IS_NOT_A_NEIGHBOUR() {

    }
}
