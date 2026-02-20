package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.Game
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.orders.TemporalFlare
import nodomain.seven.dip.provinces.Player
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

//TODO
fun moveStrength(moves: List<MoveOrder>, supports: List<SupportOrder>, pieces: Map<Location, Player>) {
    class MoveAnalyse(val order: MoveOrder, var strength: Int = 1)
    val analyse = moves.associateWith { MoveAnalyse(it) }
    val byDestination: Map<Location, List<MoveAnalyse>> = analyse.values.groupBy { it.order.action.to }
    val nonCutSupports = supports.asSequence().filterNot{support ->
        byDestination[support.piece.location]?.asSequence()
            ?.filter { if (support.action.order is MoveOrder) support.action.order.action.to == it.order.piece.location else true }
            ?.any { pieces[it.order.piece.location] != pieces[support.piece.location] } ?: false
    }
    nonCutSupports
        .filter { it.action.order is MoveOrder}
        .forEach { analyse[it.action.order]?.strength++ }
    byDestination.mapValues { (destination, orders) ->
            val topStrength = orders.maxOf { it.strength }
            val presumptiveMove = if (orders.count { it.strength == topStrength } == 1)
                orders.maxBy { it.strength } else null
            when (pieces[destination]) {
                null -> presumptiveMove // ?: Bounce()
                pieces[presumptiveMove?.order?.piece?.location] -> {} // dependence on destination (or bounce if holds)
                else -> {} // dependence on destination, or bounce if holds at topStrength or better
            }
        }
}

