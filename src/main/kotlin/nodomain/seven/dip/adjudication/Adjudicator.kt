package nodomain.seven.dip.adjudication

import nodomain.seven.dip.orders.*
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.utils.Location

operator fun Map<Piece, Player>.get(location: Location): Player? {
    return get(Army(location)) ?: get(Fleet(location))
}

sealed interface ComputableMoveResult {
    val moveOrder: MoveOrder?
    val location: Location get() = moveOrder!!.action.to

    fun ifCompatibleWith(moveOrder: MoveOrder): ComputableMoveResult? =
        if (this.moveOrder?.from == moveOrder.action.to) this else null
}
sealed interface MoveResult: ComputableMoveResult {
    companion object {
        val succeed: MoveOrder.() -> MoveResult = { SuccessfulMove(this) }
        val dependentIfMoving: MoveOrder.(MoveOrder?) -> ComputableMoveResult? =
            { if (it is MoveOrder) DependantMove(this, it) else null }
    }
}
typealias PreResult = MutableMap<Location, ComputableMoveResult>

data class DependantMove(override val moveOrder: MoveOrder, val dependsOn: MoveOrder): ComputableMoveResult
@JvmInline
value class SuccessfulMove(override val moveOrder: MoveOrder): MoveResult {
    override fun toString(): String = "$moveOrder succeeds"
}
@JvmInline
value class Bounce(override val location: Location): MoveResult {
    override val moveOrder: MoveOrder? get() = null
    override fun toString(): String = "bounce in $location"
}

class Adjudicator(moves: List<MoveOrder>, supports: List<SupportOrder>, val piecesIn: Map<Piece, Player>) {
    private inner class MoveAnalysis(val order: MoveOrder) {
        var strengthExcludingVictim: Int = 1
        var strength: Int = 1
        val against: Player? = piecesIn[order.action.to]

        fun increaseStrength(withHelpFrom: Player) {
            strength++
            if (withHelpFrom != against) strengthExcludingVictim++
        }
        override fun toString(): String = "$order with strength $strength ($strengthExcludingVictim without target)"
    }

    private val byOrigin = moves.associateBy(Order::from, ::MoveAnalysis)
    private val byDestination = byOrigin.values.groupBy { it.order.action.to }.toMutableMap()
    val nonCutSupports = supports.asSequence().filterNot { support ->
        byDestination[support.from]?.asSequence()
            ?.filter {support.action.order !is MoveOrder || support.action.order.action.to != it.order.from}
            ?.any { piecesIn[it.order.from] != piecesIn[support.from] } ?: false
    }.toList()
    val dislodgements: MutableList<MoveOrder> = mutableListOf()

    /** Produces a list containing Bounces and SuccessfulMoves.
     *  1. A SuccessfulMove will be produced for every MoveOrder in moves which succeeds in accordance with the rules of diplomacy
     *  2. A Bounce will at least be produced provinces left empty by the end of the turn into which at least unit attempted to move
     *
     *  The list may contain additional bounces in occupied provinces
     */
    val moveResults = computeMovesAndBounces()

    private fun computeMovesAndBounces(): List<MoveResult> {
        val withDependantMove = initialMoveResults().updatesDueToDislodgement()
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
        return when (val dependency = this[dependantMove.dependsOn.action.to]?.ifCompatibleWith(dependantMove.moveOrder)) {
            is Bounce, null -> Bounce(origin.action.to)
            is SuccessfulMove -> origin.(MoveResult.succeed)()
            is DependantMove if (origin.from == dependantMove.dependsOn.action.to) -> origin.(MoveResult.succeed)()
            is DependantMove -> this.analyseDependency(dependency, origin)
        }
    }

    private fun PreResult.updatesDueToDislodgement(): PreResult {
        for (dislodgement in dislodgements) {
            dislodgedMovesDontEffectTheProvinceTheyWereDislodgedFrom(dislodgement)
            dislodgedSupportsDontEffectTheProvinceTheyWereDislodgedFrom(dislodgement)
        }
        return this
    }

    private fun PreResult.dislodgedMovesDontEffectTheProvinceTheyWereDislodgedFrom(dislodgingMove: MoveOrder) {
        val dislodgedMove = byOrigin[dislodgingMove.action.to] ?: return
        if (dislodgedMove.order.action.to != dislodgingMove.from) return
        dislodgedMove.strength = 0
        when (val newResult = strongestMove(dislodgingMove.from, byDestination[dislodgingMove.from]!!)) {
            null -> remove(dislodgingMove.from)
            !is Bounce -> set(dislodgingMove.from, newResult)
            else -> {}
        }
    }

    private fun PreResult.dislodgedSupportsDontEffectTheProvinceTheyWereDislodgedFrom(dislodgingMove: MoveOrder) {
        val dislodgedSupport = nonCutSupports.find { it.from == dislodgingMove.action.to } ?: return
        if (dislodgedSupport.action.order !is MoveOrder || dislodgedSupport.action.order.action.to != dislodgingMove.from) return
        byOrigin[dislodgedSupport.action.order.from]?.strength--
        val newResult = strongestMove(dislodgingMove.from, byDestination[dislodgingMove.from]!!) ?: return
        set(dislodgingMove.from, newResult)
    }

    private fun initialMoveResults(): PreResult {
        nonCutSupports
            .filter { it.action.order is MoveOrder && it.action.order == byOrigin[it.action.order.from]?.order }
            .forEach { byOrigin[it.action.order.from]?.increaseStrength(piecesIn[it.from]!!) }
        return byDestination.asSequence()
            .map { (destination, orders) -> destination to strongestMove(destination,  orders)!! }
            .toMap(mutableMapOf())
    }

    private fun strongestMove(destination: Location, orders: List<MoveAnalysis>): ComputableMoveResult? {
        val topStrength = orders.maxOf { it.strength }
        if (orders.isNotEmpty() && topStrength == 0) return null
        val presumptiveMove = if (orders.count { it.strength == topStrength } == 1)
            orders.maxBy { it.strength } else return Bounce(destination)
        return when (piecesIn[destination]) {
            null -> presumptiveMove.order.(MoveResult.succeed)()
            piecesIn[presumptiveMove.order.from] -> presumptiveMove.order.dependOnDestination()
            else if (presumptiveMove.strengthExcludingVictim == 1) -> presumptiveMove.order.dependOnDestination()
            else if (presumptiveMove.strengthExcludingVictim <= holdStrength(destination, presumptiveMove)) -> Bounce(destination)
            else if (presumptiveMove != orders.maxBy { it.strengthExcludingVictim }) -> presumptiveMove.order.dependOnDestination()
            else -> {
                dislodgements += presumptiveMove.order
                presumptiveMove.order.(MoveResult.succeed)()
            }
        }
    }

    private fun MoveOrder.dependOnDestination(): ComputableMoveResult = // this method assumes the destination to be occupied
        (MoveResult.dependentIfMoving)(byOrigin[action.to]?.order) ?: Bounce(action.to)

    private fun holdStrength(destination: Location, against: MoveAnalysis): Int {
        return when (val moveAtDestination = byOrigin[destination]) {
            null -> nonCutSupports.count { it.action.order is HoldOrder && it.action.order.from == destination } + 1
            else if (moveAtDestination.order.action.to == against.order.from) -> moveAtDestination.strength
            else -> 0 // neither holding, nor moving counter
        }
    }
}
