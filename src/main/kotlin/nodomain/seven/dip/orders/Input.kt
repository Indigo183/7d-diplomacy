package nodomain.seven.dip.orders

import nodomain.seven.dip.adjudication.isAdjacentTo
import nodomain.seven.dip.game.Game
import nodomain.seven.dip.provinces.Player

/** Checks that:
 * 1. the ordered unit exists
 * 2. if a player is passed, the player owns the unit
 * 3. if the order is a move, the destination exists, and a flare is present
 * 4. the origin and destination are adjacent and ignores the possibility of convoys
 * 5. no involved board indexes reference Limbo (i.e. boardIndex.timeplane is never null)
 */
fun Game.isValid(order: Order, player: Player? = null): Boolean {
    order.piece.location.boardIndex.timeplane ?: return false // 5
    val board = getBoard(order.piece.location.boardIndex) ?: return false // 1 (partly)
    val unitExistsAndOwned = when(player) {
        null -> board.pieces.any { (_, countryPieces) -> order.piece.location.province in countryPieces }
        else -> board.pieces[player]?.contains(order.piece.location.province) ?: false
    }
    if (!unitExistsAndOwned) return false // 1 & 2
    val destination = when(order) {
        is MoveOrder  -> {
            order.action.to.boardIndex.timeplane ?: return false // 5
            getBoard(order.action.to.boardIndex) ?: return false // 3
            order.flare ?: return false // 3
            order.action.to
        }
        is SupportOrder if order.action.order is MoveOrder -> {
            order.action.order.piece.location.boardIndex.timeplane ?: return false // 5
            order.action.order.action.to.boardIndex.timeplane ?: return false // 5
            order.action.order.action.to
        }
        is SupportOrder -> {
            order.action.order.piece.location.boardIndex.timeplane ?: return false // 5
            order.action.order.piece.location
        }
        is HoldOrder -> return true //hold orders do not have a destination, thus 4 is trivial
    }
    return order.piece.location.isAdjacentTo(destination) // 4
}

fun input(player: Player, orders: List<Order>) = println()

fun sandboxInput(orders: List<Order>) {
    //for (order in orders) if (order == Something) {
    //    do smth
    //} else {
    //    do smth else
    //}
}