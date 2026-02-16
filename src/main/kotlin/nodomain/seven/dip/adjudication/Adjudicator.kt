package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.Game
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.orders.TemporalFlare

fun Game.sortOrders(orders: List<Order>): Pair<Map<TemporalFlare, List<MoveOrder>>, List<SupportOrder>> {
    for (order in orders) when (order) {
        is MoveOrder -> moves += order
        is SupportOrder -> supports += order
    }
    return Pair(moves.groupBy { it.flare !! }, supports)
}

