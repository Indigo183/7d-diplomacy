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
 * 6. the ordered unit is on an active board
 */
fun Game.isValid(order: Order, player: Player? = null): Boolean {
    order.from.boardIndex.timeplane ?: return false // 5
    val board = getBoard(order.from.boardIndex) ?: return false // 1 (partly)
    if (!board.isActive) return false // 6
    if(! (player?.equals(board.pieces[order.from.province]) // 2
            ?: (board.pieces[order.from.province] !== null))) return false // 1
    val destination = when(order) {
        is MoveOrder  -> {
            order.action.to.boardIndex.timeplane ?: return false // 5
            getBoard(order.action.to.boardIndex) ?: return false // 3
            order.flare ?: return false // 3
            order.action.to
        }
        is SupportOrder if order.action.order is MoveOrder -> {
            order.action.order.from.boardIndex.timeplane ?: return false // 5
            order.action.order.action.to.boardIndex.timeplane ?: return false // 5
            order.action.order.action.to
        }
        is SupportOrder -> {
            order.action.order.from.boardIndex.timeplane ?: return false // 5
            order.action.order.piece.location
        }
        is HoldOrder -> return true // hold orders do not have a destination, thus 4 is trivial
    }
    return order.piece.location.isAdjacentTo(destination) // 4
}

/** Checks that:
 * 1. the board exists
 * 2. the board is not in Limbo
 * 3. the board is in winter
 * 4. the board is active
 */
fun Game.isValid(order: BuildOrder, player: Player? = null): Boolean {
    order.piece.location.boardIndex.timeplane ?: return false // 2
    if (!order.piece.location.boardIndex.coordinate.isEven()) return false // 3
    val board = getBoard(order.piece.location.boardIndex) ?: return false // 1
    val player = if (player !== null) {
        // if...
        player
    } else board.centres[order.piece.location.province] ?: return false
    if (!board.isActive) return false //
    val count = board.countBuilds(player)
    // if ()
    return (player?.equals(board.pieces[order.piece.location.province])
        ?: board.pieces[order.piece.location.province]) === null
}

fun Game.input(orders: List<Order>, player: Player? = null) {
    addOrders(orders.filter {
        isValid(it, player) || run {
            println("WARNING: invalid order:\n$it")
            false
        }
    })
}

fun Game.inputBuilds(builds: List<BuildOrder>, player: Player? = null) {
    addAdjustments(builds.filter { isValid(it, player) || run {
        println("WARNING: invalid build:\n$it")
        false
    }})
}
