package nodomain.seven.dip.orders

import nodomain.seven.dip.adjudication.isAdjacentTo
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.provinces.Player
import java.io.InvalidClassException

// Checks that:
// 1. the ordered unit exists
// 2. if a player is passed, the player owns the unit
// 3. if the order is a move, the destination exists
// 4. the origin and destination are adjacent
// and ignores the possibility of convoys
fun Game.isValid(order: Order, player: Player? = null): Boolean {
    // 1
    val board = getBoard(order.piece.location.boardIndex) ?: return false
    // 1 & 2
    if (!if (player !== null) {
        board.pieces[player]?.contains(order.piece.location.province) ?: false
    } else {
        board.pieces.any { (_, countryPieces) -> order.piece.location.province in countryPieces }
    }) return false
    // 3
    if (order is MoveOrder && getBoard(order.action.to.boardIndex) === null) {
        return false
    }
    // 4
    if (order !is HoldOrder) {
        val destination = if (order is SupportOrder) {
            if (order.action.order is MoveOrder) order.action.order.action.to else order.action.order.piece.location
        } else if (order is MoveOrder) order.action.to
        else throw InvalidClassException("order should either be a `SupportOrder` or a `MoveOrder`")

        return order.piece.location isAdjacentTo destination
    }

    // Every test passed
    return true
}

fun input(player: Player, orders: List<Order>) = println()

fun sandboxInput(orders: List<Order>) {
    //for (order in orders) if (order == Something) {
    //    do smth
    //} else {
    //    do smth else
    //}
}