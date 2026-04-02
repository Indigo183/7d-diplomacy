package nodomain.seven.dip.orders

import nodomain.seven.dip.provinces.isAdjacentTo
import nodomain.seven.dip.game.*
import nodomain.seven.dip.provinces.Player

/** Checks that:
 * 1. the ordered unit exists
 * 2. if a player is passed, the player owns the unit
 * 3. if the order is a move, the destination exists, and a flare is present
 * 4. the origin and destination are adjacent for the relevant unit and ignores the possibility of convoys
 * 5. no involved board indexes reference Limbo (i.e. boardIndex.timeplane is never null)
 * 6. the ordered unit is on an active board
 */
fun Game.isValid(order: Order, player: Player? = null): Boolean {
    order.from.boardIndex.timeplane ?: return false // 5
    val board = getBoard(order.from.boardIndex) ?: return false // 1 (partly)
    if (!board.isActive) return false // 6
    if(! (player?.equals(board.pieces[order.from]) // 2
            ?: (board.pieces[order.from] !== null))) return false // 1
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
    return order.piece.location.isAdjacentTo(destination, forPiece = order.piece) // 4
}

/** Checks that:
 * 1. the retreat has a temporal flare
 * 2. the retreat is on the list of required retreats
 * 3. if the retreat is a move, the destination is locally adjacent
 */
fun Game.isValid(order: RetreatOrder, player: Player? = null): Boolean {
    order.flare ?: return false // 1
    if (requiredRetreats.none { (piece, flare, retreatPlayer) ->
        // Check that the retreat is required
        piece == order.piece && flare == order.flare && (player ?: retreatPlayer) == retreatPlayer
    }) return false // 2
    return (order !is MoveOrder || (
        order.piece.location.boardIndex == order.action.to.boardIndex
            && (order.piece.location.province isAdjacentTo order.action.to.province).forUnit(order.piece)
    )) // 3
}

/** Checks that:
 * 1. the board exists
 * 2. the board is not in Limbo
 * 3. the board is in winter
 * 4. the board is active
 * 5. the adjustment is the correct type (if any) based on adjustment count
 * 6. the player owns the (centre / unit) being (built in / disbanded) respectively
 */
fun Game.isValid(order: BuildOrder, player: Player? = null): Boolean {
    val location = order.piece.location
    location.boardIndex.timeplane ?: return false // 2
    if (!location.boardIndex.coordinate.isEven()) return false // 3
    val board = getBoard(location.boardIndex) ?: return false // 1
    if (!board.isActive) return false // 4
    val player = player ?: board.pieces[location] ?: board.centres[location.province] ?: return false
    val count = board.countBuilds(player)
    return when {
        count > 0 -> order is Build // 5
                && player == board.centres[location.province] && board.pieces[location] === null // 6
        count < 0 -> order is Disband // 5
                && player == board.pieces[location] // 6
        else -> false // 5
    }
}

fun Game.input(orders: List<Order>, player: Player? = null) {
    if (gameState == GameState.MOVES) addOrders(orders.filter {
        isValid(it, player) || run {
            println("WARNING: invalid order:\n$it")
            false
        }
    }) else println("WARNING: gameState is $gameState, not moves")
}

fun Game.inputRetreats(retreats: List<RetreatOrder>, player: Player? = null) {
    if (gameState == GameState.RETREATS) addAdjustments(retreats.filter {
        isValid(it, player) || run {
            println("WARNING: invalid retreat:\n$it")
            false
        }
    }) else println("WARNING: gameState is $gameState, not retreats")
}

fun Game.inputBuilds(builds: List<BuildOrder>, player: Player? = null) {
    if (gameState == GameState.BUILDS) addAdjustments(builds.filter {
        isValid(it, player) || run {
            println("WARNING: invalid build:\n$it")
            false
        }
    }) else println("WARNING: gameState is $gameState, not builds")
}
