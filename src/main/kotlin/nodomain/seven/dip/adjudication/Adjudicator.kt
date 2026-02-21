package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.*
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.orders.TemporalFlare
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
import nodomain.seven.dip.utils.BoardIndex
import nodomain.seven.dip.utils.Location

fun Game.sortOrders(orders: List<Order>): Pair<Map<TemporalFlare, List<MoveOrder>>, List<SupportOrder>> {
    // For future optimisation
    currentOrders = orders

    for (order in orders) when (order) {
        is MoveOrder -> moves += order
        is SupportOrder -> supports += order
        else -> {}
    }
    return Pair(moves.groupBy { it.flare !! }, supports)
}

sealed interface MoveResult {
    companion object {
        val succeedIfPresent: MoveOrder.() -> MoveResult = { SuccessfulMove(this) }
        val dependentIfMoving: MoveOrder.(MoveOrder?) -> MoveResult? = { if (it is MoveOrder) DependantMove(this, it) else null }
    }
}
@JvmInline
value class SuccessfulMove(val moveOrder: MoveOrder): MoveResult
data class DependantMove(val moveOrder: MoveOrder, val dependsOn: MoveOrder): MoveResult
@JvmInline
value class Bounce(val moveOrder: Location): MoveResult

fun moveStrength(moves: List<MoveOrder>, supports: List<SupportOrder>, pieces: Map<Location, Player>): List<MoveResult> {
    class MoveAnalyse(val order: MoveOrder, var strength: Int = 1)
    val byOrigin = moves.associateBy({ it.piece.location }, { MoveAnalyse(it) })
    val byDestination = byOrigin.values.groupBy { it.order.action.to }
    val nonCutSupports = supports.asSequence().filterNot{support ->
        byDestination[support.piece.location]?.asSequence()
            ?.filter { if (support.action.order is MoveOrder) support.action.order.action.to == it.order.piece.location else true }
            ?.any { pieces[it.order.piece.location] != pieces[support.piece.location] } ?: false
    }
    nonCutSupports
        .filter { it.action.order is MoveOrder}
        .forEach { byOrigin[it.action.order.piece.location]?.strength++ }
    return byDestination.map { (destination, orders) ->
            val topStrength = orders.maxOf { it.strength }
            val presumptiveMove = if (orders.count { it.strength == topStrength } == 1)
                orders.maxBy { it.strength } else null
            when (pieces[destination]) {
                null -> presumptiveMove?.order?.(MoveResult.succeedIfPresent)()
                pieces[presumptiveMove?.order?.piece?.location] ->
                    presumptiveMove?.order?.(MoveResult.dependentIfMoving)(byOrigin[destination]?.order)
                else -> null // dependence on destination, or bounce if holds at topStrength or better //TODO
            } ?: Bounce(destination)
        }
}

// Adjudicate board in a single direction
fun Game.adjudicateBoard(board: Board, direction: TemporalFlare) {
    var pieces: Map<Province, Player> = mapOf()
    var centres: Map<Province, Player> = mapOf()
    // TODO:
    //  //////////
    //  ADJUDICATE
    //  //////////
    val newChild = Board(
        BoardIndex(board.boardIndex.coordinate + direction.direction, board.boardIndex.timeplane),
        board,
        pieces,
        centres,
    )
    val mostRecentChild = board.children.lastOrNull { limboBoard ->
        limboBoard.boardIndex.coordinate - board.boardIndex.coordinate == direction.direction }
    if (mostRecentChild === null || newChild === mostRecentChild) {
        addChild(board, newChild)
    }
}

// Adjudicate board in all directions
fun Game.fullAdjudicateBoard(board: Board) {
    for (flare in TemporalFlare.entries) {
        println("INFO: adjudicating board:\n```\n$board\n```")
        adjudicateBoard(board, flare)
    }
    board.kill()
}

fun Game.adjudicateMoves() {
    for (timeplane in timeplanes) {
        for (board in timeplane.boards()) {
            fullAdjudicateBoard(board)
        }
    }
}

fun Game.adjudicateRetreats() {}

fun Game.adjudicateBuilds() {}

fun Game.adjudicate() {
    when (gameState) {
        GameState.MOVES -> adjudicateMoves()
        GameState.RETREATS -> adjudicateRetreats()
        GameState.BUILDS -> adjudicateBuilds()
    }
    advanceState()
}