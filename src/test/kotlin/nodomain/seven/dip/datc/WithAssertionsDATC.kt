package nodomain.seven.dip.datc

import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.game.GameState
import nodomain.seven.dip.game.RequiredRetreat
import nodomain.seven.dip.orders.Build
import nodomain.seven.dip.orders.BuildOrder
import nodomain.seven.dip.orders.Disband
import nodomain.seven.dip.orders.HoldOrder
import nodomain.seven.dip.orders.Inputtable
import nodomain.seven.dip.orders.InvalidGameStateException
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Moves
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.Parser.FullNationalisedFormat.DATC
import nodomain.seven.dip.orders.Piece
import nodomain.seven.dip.orders.RetreatOrder
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.orders.Supports
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.orders.get
import nodomain.seven.dip.orders.getParser
import nodomain.seven.dip.orders.input
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
typealias CentreSetup = Map<Province, Player>

interface WithAssertionsDATC: WithAssertions {
    companion object {
        private val parser = getParser<StandardPlayer>(StandardProvince.Companion)
        val origin = T(0.c, 0)
    }

    data class ParsedDATC(val ordersByPlayer: Map<Player, List<Inputtable>>, val currentGameState: GameState)

    fun String.parse(gameState: GameState = GameState.MOVES) =
        ParsedDATC(parser.parseOrderSet(trimMargin(), DATC, gameState), gameState)

    infix fun HoldOrder.shift(num: Int): HoldOrder = (piece moveTo from + num.c).holds
    infix fun MoveOrder.shift(num: Int): MoveOrder = MoveOrder(piece moveTo from + num.c, Moves(action.to + num.c), flare)
    infix fun SupportOrder.shift(num: Int): SupportOrder =
        SupportOrder(piece moveTo from + num.c, Supports((action.order shift num) as Order))
    infix fun Build.shift(num: Int): Build = +(piece moveTo from + num.c)
    infix fun Disband.shift(num: Int): Disband = -(piece moveTo from + num.c) withFlare flare
    infix fun Inputtable.shift(num: Int): Inputtable {
        return when (this) {
            is HoldOrder -> this shift num
            is MoveOrder -> this shift num
            is SupportOrder -> this shift num
            is Build -> this shift num
            is Disband -> this shift num
        }
    }
    infix fun List<Inputtable>.shift(num: Int): List<Inputtable> = map { it shift num }
    infix fun Map<Player, List<Inputtable>>.shift(num: Int): Map<Player, List<Inputtable>> =
        mapValues { it.value shift num }

    fun ParsedDATC.adjudicateAsDATC(
        setup: ()->Setup = { ordersByPlayer.impliedSetup() },
        centreSetup: (()->CentreSetup)? = null,
        game: Game = Game(setup()),
        expectAllOrderToBeValid: Boolean = true
    ): Game {
        if (centreSetup !== null) {
            val centres = game.getBoard(origin)!!.centres
            centres.clear()
            centres += centreSetup()
        }
        assertThat(game.gameState).isEqualTo(currentGameState)
        val ordersByPlayer = ordersByPlayer shift game.turn - 1
        if (expectAllOrderToBeValid)
            ordersByPlayer.forEach { (player, orders) -> assertThat(orders).allMatch { game.isValid(it, player) } }
        ordersByPlayer.forEach { (player, orders) -> try {
            game.input(orders, player)
        } catch (e: InvalidGameStateException) {
            println("WARNING: $player has NMR'd: $e")
        } }
        game.adjudicate()
        return game
    }

    fun Map<Player, List<Inputtable>>.impliedSetup(): Setup =
        asSequence().flatMap { (player, orders) -> orders.map { it.piece to player } }.toMap()

    val Game.pieces: Map<Province, Player>? get() =
        getBoard(BoardIndex((
            if (gameState == GameState.MOVES) turn - 1 else turn
        ).c))?.pieces?.mapKeys { (piece, _) -> piece.location.province }

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
