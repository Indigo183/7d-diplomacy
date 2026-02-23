package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.*
import nodomain.seven.dip.orders.HoldOrder
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

sealed interface ComputableMoveResult {
    val location: Location
}
sealed interface MoveResult: ComputableMoveResult {
    companion object {
        val succeed: MoveOrder.() -> MoveResult = { SuccessfulMove(this) }
        val dependentIfMoving: MoveOrder.(MoveOrder?) -> ComputableMoveResult? =
            { if (it is MoveOrder) DependantMove(this, it) else null }
    }
}
typealias PreResult = MutableMap<Location, ComputableMoveResult>

@JvmInline
value class SuccessfulMove(val moveOrder: MoveOrder): MoveResult {
    override val location get() = moveOrder.action.to
}
data class DependantMove(val moveOrder: MoveOrder, val dependsOn: MoveOrder): ComputableMoveResult {
    override val location get() = moveOrder.action.to
}
@JvmInline
value class Bounce(override val location: Location): MoveResult

class Adjudicator(moves: List<MoveOrder>, supports: List<SupportOrder>, val pieces: Map<Location, Player>) {
    private class MoveAnalyse(val order: MoveOrder, var strength: Int = 1)

    private val byOrigin = moves.associateBy(Order::from, ::MoveAnalyse)
    private val byDestination = byOrigin.values.groupBy { it.order.action.to }
    private val nonCutSupports = supports.asSequence().filterNot { support ->
        byDestination[support.from]?.asSequence()
            ?.filter {support.action.order !is MoveOrder || support.action.order.action.to == it.order.from}
            ?.any { pieces[it.order.from] != pieces[support.from] } ?: false
    }
    private val dislodgements: MutableList<MoveOrder> = mutableListOf()

    /** Produces a list containing Bounces and SuccessfulMoves.
     *  1. A SuccessfulMove will be produced for every MoveOrder in moves which succeeds in accordance with the rules of diplomacy
     *  2. A Bounce will at least be produced provinces left empty by the end of the turn into which at least unit attempted to move
     *
     *  The list may contain additional bounces in occupied provinces
     */
    val movesAndBounces by lazy { computeMovesAndBounces() }

    private fun computeMovesAndBounces(): List<MoveResult> {
        val withDependantMove = initialMoveResults().updateBouncesDueToDislodgement()
        return withDependantMove.values.asSequence()
            .filterNot { it is DependantMove && it.moveOrder.from == it.dependsOn.action.to }
            .map {
                when(it) {
                    is DependantMove -> SuccessfulMove(it.moveOrder)
                    is MoveResult -> it
                }
            }.toList()
    }

    private fun PreResult.updateBouncesDueToDislodgement(): PreResult {
        for (dislodgement in dislodgements) {
            val dislodgedMove = byOrigin[dislodgement.action.to]
            if (dislodgedMove?.order?.action?.to != dislodgement.from || byDestination[dislodgement.from]!!.size <= 1) continue
            dislodgedMove.strength = 0
            val newResult = strongestMove(dislodgement.from, byDestination[dislodgement.from]!!)
            if (newResult !is Bounce)
                set(dislodgement.from, newResult)
        }
        return this
    }

    private fun initialMoveResults(): PreResult {
        nonCutSupports
            .filter { it.action.order is MoveOrder }
            .forEach { byOrigin[it.action.order.from]?.strength++ }
        return byDestination.asSequence()
            .map { (destination, orders) -> destination to strongestMove(destination,  orders) }
            .toMap(mutableMapOf())
    }

    private fun strongestMove(destination: Location, orders: List<MoveAnalyse>): ComputableMoveResult {
        val topStrength = orders.maxOf { it.strength }
        val presumptiveMove = if (orders.count { it.strength == topStrength } == 1)
            orders.maxBy { it.strength }.order else return Bounce(destination)
        return when (pieces[destination]) {
            null -> presumptiveMove.(MoveResult.succeed)()
            pieces[presumptiveMove.from] -> presumptiveMove.dependOnDestination()
            else if (topStrength == 1) -> presumptiveMove.dependOnDestination()
            else if (byOrigin[destination] !== null && topStrength <= holdStrength(destination)) -> Bounce(destination)
            else -> {
                dislodgements += presumptiveMove
                presumptiveMove.(MoveResult.succeed)()
            }
        }
    }

    fun holdStrength(destination: Location): Int =
        nonCutSupports.count { it.action.order is HoldOrder && it.action.order.from == destination } + 1

    fun MoveOrder.dependOnDestination(): ComputableMoveResult = // this method assumes the destination to be occupied
        (MoveResult.dependentIfMoving)(byOrigin[action.to]?.order) ?: Bounce(action.to)
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