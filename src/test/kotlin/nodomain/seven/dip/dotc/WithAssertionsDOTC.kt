package nodomain.seven.dip.dotc

import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.Board
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.Parser
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.StandardPlayer
import nodomain.seven.dip.provinces.StandardProvince
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.c
import org.assertj.core.api.WithAssertions

interface WithAssertionsDOTC: WithAssertions {
    companion object {
        val parser = getParser<StandardPlayer, StandardProvince>(provinceTrim = { trim().substring(0, 3).uppercase() })
    }

    fun String.parse(): Map<Player, List<Order>> =
        parser.parseOrderSet(this.trimMargin(), Parser.NationalisedFormat.DOTC)

    fun Map<Player, List<Order>>.adjudicateAsDOTC(setup: Map<Province, Player> = impliedSetup()): Board? {
        val game = Game(setup)
        forEach { (player, orders) -> game.input(orders, player) }
        game.adjudicate()
        return game.getBoard(BoardIndex(1.c))
    }

    fun Map<Player, List<Order>>.impliedSetup(): Map<Province, Player> =
        asSequence().flatMap { (player, orders) -> orders.map { it.from.province to player } }.toMap()

    fun Board?.andAssertThatNothingMoved() {
        assertThat(this).isNull()
    }
}