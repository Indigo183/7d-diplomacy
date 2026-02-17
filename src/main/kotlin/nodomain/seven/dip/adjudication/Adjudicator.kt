package nodomain.seven.dip.adjudication

import nodomain.seven.dip.utils.ComplexNumber
import nodomain.seven.dip.utils.ComplexNumber.*

import nodomain.seven.dip.game.Game
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.orders.TemporalFlare
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

// Checks if boards are adjacent, but not the same
fun BoardIndex.isAdjacentTo(other: BoardIndex): Boolean {
    return if (coordinate == other.coordinate) {
        timeplane - other.timeplane == 1 || timeplane - other.timeplane == -1
    } else if (timeplane == other.timeplane) when (coordinate - other.coordinate) {
        i, -i, 1 + 0*i, -1 + 0*i -> true
        else -> false
    } else false
}

infix fun Location.isAdjacentTo(other: Location): Boolean {
    return if (boardIndex == other.boardIndex) { // adjacency is local
        province isAdjacentTo other.province
    } else { // adjacency is non-local
        boardIndex.isAdjacentTo(other.boardIndex) &&
                (province isAdjacentTo other.province || province == other.province)
    }
}