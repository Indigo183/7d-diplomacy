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
fun Game.adjudicateBoard(board: Board, direction: TemporalFlare, moveResults: List<MoveResult>) {
    println("killing board at ${board.boardIndex}")
    board.kill()
    // Do nothing if nothing relevant happened
    if (moveResults.isEmpty()) return

    val pieces: MutableMap<Province, Player> = board.pieces.toMutableMap()
    val centres: MutableMap<Province, Player> = board.centres.toMutableMap()

    for (move in moveResults) if (move is SuccessfulMove) {
        // Add pieces mkvkng to board
        if (move.moveOrder.action.to.boardIndex == board.boardIndex) {
            pieces[move.moveOrder.action.to.province] = getBoard(move.moveOrder.piece.location.boardIndex)?.pieces[move.moveOrder.from.province] ?: throw IllegalArgumentException("order not properly validated") // TODO: correct exception
        }
        // Remove pieces moving from board
        if (move.moveOrder.from.boardIndex == board.boardIndex) {
            pieces.remove(move.moveOrder.from.province)
        }
    }

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
    if (mostRecentChild === null || (newChild.pieces == mostRecentChild.pieces && newChild.centres == mostRecentChild.centres)) {
        addChild(board, newChild)
    }
}

// Adjudicate board in all directions
/*
fun Game.fullAdjudicateBoard(board: Board) {
    println("INFO: adjudicating board:\n```\n$board\n```")
    for (flare in TemporalFlare.entries) {
        println("INFO: adjudicating with direction $flare")
        adjudicateBoard(board, flare)
    }
    board.kill()
}
 */

fun Game.adjudicateMoves() {
    val pieces = getAllPieces()
    val boards = timeplanes.flatMap { it.boards() } //  to ensure the list of boards isn't updated
    val adjudicators: MutableMap<TemporalFlare, Adjudicator> = mutableMapOf()
    for (flare in TemporalFlare.entries) {
        println("INFO: adjudicating $flare")
        val adjudicator = Adjudicator(moves.filter { it.flare == flare }, supports, pieces)
        adjudicators[flare] = adjudicator
        println("INFO: with results: ${adjudicator.moveResults}")
        for (board in boards) {
            println("INFO: ajudicating board at ${board.boardIndex}")
            adjudicateBoard(board, flare, adjudicator.moveResults.filter {
                it !is SuccessfulMove || // "it is Bounce"
                it.moveOrder.from.boardIndex == board.boardIndex ||
                it.moveOrder.action.to.boardIndex == board.boardIndex
                })
        }
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
