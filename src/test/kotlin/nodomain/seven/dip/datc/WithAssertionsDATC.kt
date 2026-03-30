package nodomain.seven.dip.datc

import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.Parser.NationalisedFormat.DATC
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.orders.TemporalFlare
import nodomain.seven.dip.orders.get
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.StandardCoast
import nodomain.seven.dip.provinces.StandardPlayer
import nodomain.seven.dip.provinces.StandardProvince
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c
import org.assertj.core.api.WithAssertions

typealias Setup = Map<Province, Player>

interface WithAssertionsDATC: WithAssertions {
    companion object {
        private val parser = getParser<StandardPlayer>(StandardProvince.Companion)
        val origin = T(0.c, 0)
    }

    fun String.parse(): Map<Player, List<Order>> =
        parser.parseOrderSet(this.trimMargin(), DATC)


    fun Map<Player, List<Order>>.adjudicateAsDOTC(setup: ()->Setup = {impliedSetup()}, game: Game = Game(setup())): Game {
        forEach { (player, orders) -> game.input(orders, player) }
        game.adjudicate()
        return game
    }

    fun Map<Player, List<Order>>.impliedSetup(): Setup =
        asSequence().flatMap { (player, orders) -> orders.map { it.from.province to player } }.toMap()

    val Game.pieces: Map<Province, Player>? get() = getBoard(BoardIndex(1.c))?.pieces

    fun Game.andAssertThatNothingMoved(): Game {
        assertThat(pieces).satisfiesAnyOf(
            {assertThat(it).isNull()},
            {assertThat(it).isEqualTo(getBoard(T(0.c, 0))!!.pieces)}
        )
        return this
    }

    fun retreatsIn(vararg province: Province): Array<Pair<Location, TemporalFlare>> {
        return province.map { origin[it] to TemporalFlare.RIGHT }.toTypedArray()
    }
}
