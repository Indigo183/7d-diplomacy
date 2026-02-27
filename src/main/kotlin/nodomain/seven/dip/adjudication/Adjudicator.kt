package nodomain.seven.dip.adjudication

import nodomain.seven.dip.orders.HoldOrder
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.utils.Location

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

class Adjudicator(moves: List<MoveOrder>, supports: List<SupportOrder>, val piecesIn: Map<Location, Player>) {
    private class MoveAnalyse(val order: MoveOrder, var strength: Int = 1)

    private val byOrigin = moves.associateBy(Order::from, ::MoveAnalyse)
    private val byDestination = byOrigin.values.groupBy { it.order.action.to }
    private val nonCutSupports = supports.asSequence().filterNot { support ->
        byDestination[support.from]?.asSequence()
            ?.filter {support.action.order !is MoveOrder || support.action.order.action.to == it.order.from}
            ?.any { piecesIn[it.order.from] != piecesIn[support.from] } ?: false
    }
    private val dislodgements: MutableList<MoveOrder> = mutableListOf()

    /** Produces a list containing Bounces and SuccessfulMoves.
     *  1. A SuccessfulMove will be produced for every MoveOrder in moves which succeeds in accordance with the rules of diplomacy
     *  2. A Bounce will at least be produced provinces left empty by the end of the turn into which at least unit attempted to move
     *
     *  The list may contain additional bounces in occupied provinces
     */
    val moveResults = computeMovesAndBounces()

    val needsRetreats: MutableMap<Location, Player> = mutableMapOf()

    val piecesOut: Map<Location, Player> = computePiecesOut()

    private fun computePiecesOut(): Map<Location, Player> {
        val piecesOut: MutableMap<Location, Player> = mutableMapOf()
        val mayNeedRetreats: MutableMap<Location, Player> = mutableMapOf()
        piecesIn.forEach { (location, occupant) ->
            if (byOrigin[location]?.order?.(MoveResult.succeed)() in moveResults)
                piecesOut += byOrigin[location]!!.order.action.to to occupant
            else
                mayNeedRetreats += location to occupant
        }
        mayNeedRetreats.forEach { (location, occupant) ->
            if (piecesOut.containsKey(location))
                needsRetreats += location to occupant
            else
                piecesOut += location to occupant
        }
        return piecesOut
    }

    private fun computeMovesAndBounces(): List<MoveResult> {
        val withDependantMove = initialMoveResults().updateBouncesDueToDislodgement()
        return withDependantMove.values.map {
            when(it) {
                is DependantMove -> withDependantMove.analyseDependency(it)
                is MoveResult -> it
            }
        }
    }

    // no optimisation is done to avoid going down the same chain multiple times
    private tailrec fun PreResult.analyseDependency(dependantMove: DependantMove, origin: MoveOrder = dependantMove.moveOrder): MoveResult {
        if (dependantMove.moveOrder.from == dependantMove.dependsOn.action.to) return Bounce(origin.action.to)
        if (origin.from == dependantMove.dependsOn.action.to) return origin.(MoveResult.succeed)()
        return when (val dependency = this[dependantMove.dependsOn.action.to]) {
            is Bounce, null -> Bounce(origin.action.to)
            is SuccessfulMove -> origin.(MoveResult.succeed)()
            is DependantMove -> this.analyseDependency(dependency, origin)
        }
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
            .filter { it.action.order is MoveOrder && it.action.order == byOrigin[it.action.order.from] }
            .forEach { byOrigin[it.action.order.from]?.strength++ }
        return byDestination.asSequence()
            .map { (destination, orders) -> destination to strongestMove(destination,  orders) }
            .toMap(mutableMapOf())
    }

    private fun strongestMove(destination: Location, orders: List<MoveAnalyse>): ComputableMoveResult {
        val topStrength = orders.maxOf { it.strength }
        val presumptiveMove = if (orders.count { it.strength == topStrength } == 1)
            orders.maxBy { it.strength }.order else return Bounce(destination)
        return when (piecesIn[destination]) {
            null -> presumptiveMove.(MoveResult.succeed)()
            piecesIn[presumptiveMove.from] -> presumptiveMove.dependOnDestination()
            else if (topStrength == 1) -> presumptiveMove.dependOnDestination()
            else if (byOrigin[destination] !== null && topStrength <= holdStrength(destination)) -> Bounce(destination)
            else -> {
                dislodgements += presumptiveMove
                presumptiveMove.(MoveResult.succeed)()
            }
        }
    }

    fun MoveOrder.dependOnDestination(): ComputableMoveResult = // this method assumes the destination to be occupied
        (MoveResult.dependentIfMoving)(byOrigin[action.to]?.order) ?: Bounce(action.to)

    fun holdStrength(destination: Location): Int =
        nonCutSupports.count { it.action.order is HoldOrder && it.action.order.from == destination } + 1
}
