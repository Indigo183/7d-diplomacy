package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.Board
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.Parser.NationalisedFormat.DOTC
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.StandardPlayer
import nodomain.seven.dip.provinces.StandardProvince
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.c
import org.assertj.core.api.WithAssertions
import kotlin.test.Test

class DOTC: WithAssertions {
    companion object: WithAssertions {
        val parser = getParser<StandardPlayer, StandardProvince>(provinceTrim = { trim().substring(0, 3).uppercase() })

        @JvmStatic
        fun String.parse(): Map<Player, List<Order>> =
            parser.parseOrderSet(this.trimMargin(), DOTC)

        @JvmStatic
        fun Map<Player, List<Order>>.adjudicateAsDOTC(setup: Map<Province, Player> = impliedSetup()): Board? {
            val game = Game(setup)
            forEach { (player, orders) -> game.input(orders, player) }
            game.adjudicate()
            return game.getBoard(BoardIndex(1.c))
        }

        @JvmStatic
        fun Map<Player, List<Order>>.impliedSetup(): Map<Province, Player> =
            asSequence().flatMap { (player, orders) -> orders.map { it.from.province to player } }.toMap()

        @JvmStatic
        fun Board?.andAssertThatNothingMoved() {
            assertThat(this).isNull()
        }
    }

    //6.A. TEST CASES, BASIC CHECKS

    @Test
    fun Testcase_6_A_1_MOVING_TO_AN_AREA_THAT_IS_NOT_A_NEIGHBOUR() {

    }

    @Test
    fun `6_A_4 test case, move to own sector`() {
        """
        Germany:
        F Kiel - Kiel
        """.parse().adjudicateAsDOTC().andAssertThatNothingMoved()
    }

}
