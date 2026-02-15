package nodomain.seven.dip.adjudication

import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Moves
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.orders.TemporalFlare

fun sortOrders(orders: List<Order>): Pair<Map<TemporalFlare, List<MoveOrder>>, Map<SupportOrder, Int>> {
    val moves: MutableSet<MoveOrder> = mutableSetOf()
    val supports: MutableSet<SupportOrder> = mutableSetOf()
    for (order in orders) when (order) {
        is MoveOrder -> moves += order
        is SupportOrder -> supports += order
    }
    return Pair(moves.groupBy { it.flare !! }, supports.associateWith { 0 })
}

fun TemporalFlare.thisMethodNeedsABetterName(flag: Int): Int = flag or (1 shr ordinal)


