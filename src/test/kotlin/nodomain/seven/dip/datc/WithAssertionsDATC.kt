package nodomain.seven.dip.datc

import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.game.GameState
import nodomain.seven.dip.game.RequiredRetreat
import nodomain.seven.dip.orders.BuildOrder
import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.Parser.FullNationalisedFormat.DATC
import nodomain.seven.dip.orders.Piece
import nodomain.seven.dip.orders.RetreatOrder
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.orders.get
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.orders.inputBuilds
import nodomain.seven.dip.orders.inputRetreats
import nodomain.seven.dip.orders.isValid
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.provinces.StandardPlayer
import nodomain.seven.dip.provinces.StandardProvince
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c
import org.assertj.core.api.WithAssertions

typealias Setup = Map<Piece, Player>

interface WithAssertionsDATC: WithAssertions {
    companion object {
        private val parser = getParser<StandardPlayer>(StandardProvince.Companion)
        val origin = T(0.c, 0)
    }

    data class ParsedDATC(val ordersByPlayer: Map<Player, List<Inputtable>>, val currentGameState: GameState)

    fun String.parse(gameState: GameState = GameState.MOVES) =
        ParsedDATC(parser.parseOrderSet(this.trimMargin(), DATC, gameState), gameState)


    fun ParsedDATC.adjudicateAsDATC(
        setup: ()->Setup = {ordersByPlayer.impliedSetup()},
        game: Game = Game(setup()),
        expectAllOrderToBeValid: Boolean = true
    ): Game {
        assertThat(game.gameState).isEqualTo(currentGameState)
        if (expectAllOrderToBeValid) {
            ordersByPlayer.forEach { (player, orders) ->
                assertThat(orders).allMatch {
                    when (currentGameState) {
                        GameState.MOVES -> game.isValid(it as Order, player)
                        GameState.RETREATS -> game.isValid(it as RetreatOrder, player)
                        GameState.BUILDS -> game.isValid(it as BuildOrder, player)
                    }
                }
            }
        }
        ordersByPlayer.forEach { (player, orders) ->
            when (currentGameState) {
                GameState.MOVES -> game.input(orders.filterIsInstance<Order>(), player)
                GameState.RETREATS -> game.inputRetreats(orders.filterIsInstance<RetreatOrder>(), player)
                GameState.BUILDS -> game.inputBuilds(orders.filterIsInstance<BuildOrder>(), player)
            }
        }
        game.adjudicate()
        return game
    }

    fun Map<Player, List<Inputtable>>.impliedSetup(): Setup =
        asSequence().flatMap { (player, orders) -> orders.map { it.piece to player } }.toMap()

    val Game.pieces: Map<Province, Player>? get() =
        getBoard(BoardIndex(1.c))?.pieces?.mapKeys { (piece, _) -> piece.location.province }

    fun Game.andAssertThatNothingMoved(): Game {
        assertThat(pieces).satisfiesAnyOf(
            {assertThat(it).isNull()},
            {assertThat(it).isEqualTo(getBoard(T(0.c, 0))!!.pieces)}
        )
        return this
    }

    val List<RequiredRetreat>.locations get() = map { it.piece.location }

    fun retreatsIn(vararg province: Province): Array<Location> {
        return province.map { origin[it] }.toTypedArray()
    }
}
