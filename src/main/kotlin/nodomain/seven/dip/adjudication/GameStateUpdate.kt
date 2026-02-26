package nodomain.seven.dip.adjudication

import nodomain.seven.dip.game.*
import nodomain.seven.dip.orders.MoveOrder
import nodomain.seven.dip.orders.Order
import nodomain.seven.dip.orders.SupportOrder
import nodomain.seven.dip.orders.TemporalFlare
import nodomain.seven.dip.provinces.Player
import nodomain.seven.dip.provinces.Province
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

fun Game.getAllPieces(player: Player? = null, onlyActive: Boolean = false): Map<Location, Player> {
    val pieces: MutableMap<Location, Player> = mutableMapOf()
    for (t in timeplanes) for (board in t.boards()) if (!onlyActive || board.isActive) for (piece in board.pieces) {
        if (player !== null && piece.value == player) {
            pieces[Location(piece.key, board.boardIndex)] = piece.value
        }
    }

    return pieces
}

// Adjudicate board in a single direction
fun Game.adjudicateBoard(board: Board, direction: TemporalFlare) {
    var pieces: Map<Province, Player> = mapOf()
    var centres: Map<Province, Player> = mapOf()
    // TODO:
    //  //////////
    //  ADJUDICATE
    //  //////////
    val newChild = Board(
        BoardIndex(board.boardIndex.coordinate + direction.direction, board.boardIndex.timeplane),
        board,
        pieces,
        centres,
    )
    val mostRecentChild = board.children.lastOrNull { limboBoard ->
        limboBoard.boardIndex.coordinate - board.boardIndex.coordinate == direction.direction }
    if (mostRecentChild === null || newChild === mostRecentChild) {
        addChild(board, newChild)
    }
}

// Adjudicate board in all directions
fun Game.fullAdjudicateBoard(board: Board) {
    println("INFO: adjudicating board:\n```\n$board\n```")
    for (flare in TemporalFlare.entries) {
        println("INFO: adjudicating with direction $flare")
        adjudicateBoard(board, flare)
    }
    board.kill()
}

fun Game.adjudicateMoves() {
    val pieces = getAllPieces()
    val adjudicators: MutableMap<TemporalFlare, Adjudicator> = mutableMapOf()
    for (flare in TemporalFlare.entries) {
        println("INFO: adjudicating $flare")
        adjudicators[flare] = Adjudicator(moves.filter { it.flare == flare }, supports, pieces)
        println("INFO: with results: ${adjudicators[flare]!!.movesAndBounces}")
    }
}

fun Game.adjudicateRetreats() {}

fun Game.adjudicateBuilds() {}

fun Game.adjudicate() {
    when (gameState) {
        GameState.MOVES -> adjudicateMoves()
        GameState.RETREATS -> adjudicateRetreats()
        GameState.BUILDS -> adjudicateBuilds()
    }
    advanceState()
}
