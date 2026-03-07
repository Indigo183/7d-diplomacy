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
    private inner class MoveAnalyse(val order: MoveOrder) {
        var strengthExcludingVictim: Int = 1
        var strength: Int = 1
        val against: Player? = piecesIn[order.action.to]

        fun increaseStrength(withHelpFrom: Player) {
            strength++
            if (withHelpFrom != against) strengthExcludingVictim++
        }
    }

    private val byOrigin = moves.associateBy(Order::from, ::MoveAnalyse)
    private val byDestination = byOrigin.values.groupBy { it.order.action.to }
    val nonCutSupports = supports.asSequence().filterNot { support ->
        byDestination[support.from]?.asSequence()
            ?.filter {support.action.order !is MoveOrder || support.action.order.action.to != it.order.from}
            ?.any { piecesIn[it.order.from] != piecesIn[support.from] } ?: false
    }.toList()
    private val dislodgements: MutableList<MoveOrder> = mutableListOf()

    /** Produces a list containing Bounces and SuccessfulMoves.
     *  1. A SuccessfulMove will be produced for every MoveOrder in moves which succeeds in accordance with the rules of diplomacy
     *  2. A Bounce will at least be produced provinces left empty by the end of the turn into which at least unit attempted to move
     *
     *  The list may contain additional bounces in occupied provinces
     */
    val moveResults = computeMovesAndBounces()

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
        return when (val dependency = this[dependantMove.dependsOn.action.to]) {
            is Bounce, null -> Bounce(origin.action.to)
            is SuccessfulMove -> origin.(MoveResult.succeed)()
            is DependantMove if (origin.from == dependantMove.dependsOn.action.to) -> return origin.(MoveResult.succeed)()
            is DependantMove -> this.analyseDependency(dependency, origin)
        }
    }

    private fun PreResult.updateBouncesDueToDislodgement(): PreResult {
        for (dislodgement in dislodgements) {
            dislodgedMovesDontEffectTheProvinceTheyWareDislodgedFrom(dislodgement)
            dislodgedSupportsDontEffectTheProvinceTheyWareDislodgedFrom(dislodgement)
        }
        return this
    }

    private fun PreResult.dislodgedMovesDontEffectTheProvinceTheyWareDislodgedFrom(dislodgingMove: MoveOrder) {
        val dislodgedMove = byOrigin[dislodgingMove.action.to] ?: return
        if (dislodgedMove.order.action.to != dislodgingMove.from || byDestination[dislodgingMove.from]!!.size <= 1) return
        dislodgedMove.strength = 0
        val newResult = strongestMove(dislodgingMove.from, byDestination[dislodgingMove.from]!!)
        if (newResult !is Bounce)
            set(dislodgingMove.from, newResult)

    }

    private fun PreResult.dislodgedSupportsDontEffectTheProvinceTheyWareDislodgedFrom(dislodgingMove: MoveOrder) {
        val dislodgedSupport = nonCutSupports.find { it.from == dislodgingMove.action.to } ?: return
        if (dislodgedSupport.action.order !is MoveOrder || dislodgedSupport.action.order.action.to != dislodgingMove.from) return
        byOrigin[dislodgedSupport.action.order.from]?.strength--
        val newResult = strongestMove(dislodgingMove.from, byDestination[dislodgingMove.from]!!)
        set(dislodgingMove.from, newResult)

    }

        private fun initialMoveResults(): PreResult {
        nonCutSupports
            .filter { it.action.order is MoveOrder && it.action.order == byOrigin[it.action.order.from]?.order }
            .forEach { byOrigin[it.action.order.from]?.increaseStrength(piecesIn[it.from]!!) }
        return byDestination.asSequence()
            .map { (destination, orders) -> destination to strongestMove(destination,  orders) }
            .toMap(mutableMapOf())
    }

    private fun strongestMove(destination: Location, orders: List<MoveAnalyse>): ComputableMoveResult {
        val topStrength = orders.maxOf { it.strength }
        val presumptiveMove = if (orders.count { it.strength == topStrength } == 1)
            orders.maxBy { it.strength } else return Bounce(destination)
        return when (piecesIn[destination]) {
            null -> presumptiveMove.order.(MoveResult.succeed)()
            piecesIn[presumptiveMove.order.from] -> presumptiveMove.order.dependOnDestination()
            else if (presumptiveMove.strengthExcludingVictim == 1) -> presumptiveMove.order.dependOnDestination()
            else if (byOrigin[destination] === null && presumptiveMove.strengthExcludingVictim <= holdStrength(destination)) -> Bounce(destination)
            else -> {
                dislodgements += presumptiveMove.order
                presumptiveMove.order.(MoveResult.succeed)()
            }
        }
    }

    fun MoveOrder.dependOnDestination(): ComputableMoveResult = // this method assumes the destination to be occupied
        (MoveResult.dependentIfMoving)(byOrigin[action.to]?.order) ?: Bounce(action.to)

    fun holdStrength(destination: Location): Int =
        nonCutSupports.count { it.action.order is HoldOrder && it.action.order.from == destination } + 1
}
